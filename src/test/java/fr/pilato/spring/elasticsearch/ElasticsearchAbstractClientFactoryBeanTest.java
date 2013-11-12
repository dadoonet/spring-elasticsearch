package fr.pilato.spring.elasticsearch;

import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 *
 */
public class ElasticsearchAbstractClientFactoryBeanTest {

    Log mockLogger;

    @Before
    public void setup() {
        mockLogger = mock(Log.class);
        ElasticsearchAbstractClientFactoryBean.logger = mockLogger;
    }

    @Test
    public void testReadFileInClasspath_ExpectFileReadOk() throws Exception {
        String url = "classpath-reader-test.txt";
        String contents = ElasticsearchAbstractClientFactoryBean.readFileInClasspath(url);
        assertTrue("Expect to find file on classpath and read contents.", contents.startsWith("This file is here for testing purposes"));
    }

    @Test
    public void testReadFileInClasspath_ExpectFileNotFound_ReturnsNull() throws Exception {
        String url = "__unknown_file_path_____";
        String contents = ElasticsearchAbstractClientFactoryBean.readFileInClasspath(url);
        verify(mockLogger).error(eq( "Failed to load file from url: " + url), any(Exception.class));
        assertTrue("Expect file not found, return null.", contents == null);
    }
}
