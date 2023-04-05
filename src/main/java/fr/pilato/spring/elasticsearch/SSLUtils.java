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

import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class SSLUtils {
    private static SSLContext yesCtx = null;

    /**
     * Returns an SSLContext that will accept any server certificate.
     * Use with great care and in limited situations, as it allows MITM attacks.
     */
    public static SSLContext yesSSLContext() {
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
}
