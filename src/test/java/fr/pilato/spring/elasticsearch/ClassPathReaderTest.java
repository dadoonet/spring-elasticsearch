package fr.pilato.spring.elasticsearch;

import org.junit.Test;

import static junit.framework.Assert.assertTrue;

/**
 *
 */
public class ClassPathReaderTest {

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
        assertTrue("Expect file not found, return null.", contents == null);
    }
}
