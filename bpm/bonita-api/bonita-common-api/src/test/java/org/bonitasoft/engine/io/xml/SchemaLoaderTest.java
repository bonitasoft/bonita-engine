package org.bonitasoft.engine.io.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

import javax.xml.validation.Schema;

import org.junit.Before;
import org.junit.Test;

public class SchemaLoaderTest {

    private SchemaLoader loader;

    @Before
    public void setUp() {
        loader = new SchemaLoader();
    }

    @Test
    public void can_load_schema_with_spaces_in_the_path() throws Exception {
        //given
        final URL resource = SchemaLoader.class.getResource("/folder with space/ProcessDefinition.xsd");
        //when
        final Schema schema = loader.loadSchema(resource);

        //then
        assertThat(schema).isNotNull();
    }

}
