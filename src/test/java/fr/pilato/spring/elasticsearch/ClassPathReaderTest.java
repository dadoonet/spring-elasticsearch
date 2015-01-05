package fr.pilato.spring.elasticsearch;

import fr.pilato.elasticsearch.tools.SettingsReader;
import org.junit.Test;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

public class ClassPathReaderTest {

    @Test
    public void testReadFileInClasspath_ExpectFileReadOk() throws Exception {
        String url = "classpath-reader-test.txt";
        String contents = SettingsReader.readFileFromClasspath(url);
        assertThat(contents, startsWith("This file is here for testing purposes"));
    }

    @Test
    public void testReadFileInClasspath_ExpectFileNotFound_ReturnsNull() throws Exception {
        String url = "__unknown_file_path_____";
        String contents = SettingsReader.readFileFromClasspath(url);
        assertThat(contents, nullValue());
    }
}
