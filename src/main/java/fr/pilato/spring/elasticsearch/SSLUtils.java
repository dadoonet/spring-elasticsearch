/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package fr.pilato.spring.elasticsearch;

import org.apache.commons.io.IOUtils;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class SSLUtils {
    private static final Logger logger = LoggerFactory.getLogger(SSLUtils.class);
    private static SSLContext yesCtx = null;
    private static SSLContext checkCertificatesSSLContext = null;

    /**
     * Returns an SSLContext that will accept any server certificate.
     * Use with great care and in limited situations, as it allows MITM attacks.
     */
    public static SSLContext yesSSLContext() {
        logger.warn("You disabled checking the https certificate. This could lead to " +
                "'Man in the middle' attacks. This setting is only intended for tests.");

        if (yesCtx == null) {
            X509TrustManager yesTm = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    // Accept anything
                }

                @Override
                public void  checkServerTrusted(X509Certificate[] certs, String authType) {
                    // Accept anything
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            };

            try {
                SSLContext ctx = SSLContext.getInstance("SSL");
                ctx.init(null, new X509TrustManager[] { yesTm }, null);
                yesCtx = ctx;
            } catch (Exception e) {
                // An exception here means SSL is not supported, which is unlikely
                throw new RuntimeException(e);
            }
        }

        return yesCtx;
    }

    /**
     *
     * @param trustStore
     * @param keyStoreType  pkcs12
     * @return
     */
    public static SSLContext checkCertificatesSSLContext(String trustStore, String keyStoreType, String keyStorePass) {
        if (checkCertificatesSSLContext == null) {
            try {
                Path trustStorePath = Paths.get(trustStore);
                KeyStore truststore = KeyStore.getInstance(keyStoreType);
                try (InputStream is = Files.newInputStream(trustStorePath)) {
                    truststore.load(is, keyStorePass.toCharArray());
                }
                SSLContextBuilder sslBuilder = SSLContexts.custom().loadTrustMaterial(truststore, null);
                checkCertificatesSSLContext = sslBuilder.build();
            } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
                logger.error("Can not load the SSLContext for {}, {}", trustStore, keyStoreType);
            }
        }
        return checkCertificatesSSLContext;
    }

    /**
     * A SSL context based on the self signed CA, so that using this SSL Context allows to connect to the Elasticsearch service
     * @return a customized SSL Context
     */
    public SSLContext createSslContextFromCa(String certPath) {
        try {
            byte[] bytes = IOUtils.resourceToByteArray(certPath);
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            Certificate trustedCa = factory.generateCertificate(new ByteArrayInputStream(bytes));
            KeyStore trustStore = KeyStore.getInstance("pkcs12");
            trustStore.load(null, null);
            trustStore.setCertificateEntry("ca", trustedCa);

            final SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
            TrustManagerFactory tmfactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmfactory.init(trustStore);
            sslContext.init(null, tmfactory.getTrustManagers(), null);
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
