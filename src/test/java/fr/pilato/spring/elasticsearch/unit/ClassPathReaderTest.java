package fr.pilato.spring.elasticsearch.unit;

import fr.pilato.elasticsearch.tools.SettingsReader;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

class ClassPathReaderTest {

    @Test
    void testReadFileInClasspath_ExpectFileReadOk() throws Exception {
        String url = "classpath-reader-test.txt";
        String contents = SettingsReader.readFileFromClasspath(url);
        assertThat(contents, startsWith("This file is here for testing purposes"));
    }

    @Test
    void testReadFileInClasspath_ExpectFileNotFound_ReturnsNull() throws Exception {
        String url = "__unknown_file_path_____";
        String contents = SettingsReader.readFileFromClasspath(url);
        assertThat(contents, nullValue());
    }
}
