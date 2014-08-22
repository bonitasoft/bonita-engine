package org.bonitasoft.engine.xml;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.Test;

public class SAXValidatorTest {

    private static final String SERVEUR_PROCESS_SCHEMA = "org/bonitasoft/engine/core/process/definition/model/builder/impl/SProcessDefinition.xsd";
    private static final String CLIENT_PROCESS_SCHEMA = "org/bonitasoft/engine/bpm/bar/ProcessDefinition.xsd";
    private XMLSchemaValidator validator;

    @Before
    public void before() throws Exception {
        final SAXValidatorFactory saxValidatorFactory = new SAXValidatorFactory();
        validator = saxValidatorFactory.createValidator();
    }

    @Test
    public void should_validate_client_xml_with_contract() throws Exception {
        check_xml_file_is_valid(CLIENT_PROCESS_SCHEMA, "client-process-design.xml");
    }

    @Test
    public void should_validate_serveur_xml_with_contract() throws Exception {
        check_xml_file_is_valid(SERVEUR_PROCESS_SCHEMA, "server-process-definition.xml");
    }

    private void check_xml_file_is_valid(final String xsdResource, final String xmlResource) throws URISyntaxException, SInvalidSchemaException,
    SValidationException, IOException {
        //given
        final File processClientXml = new File(Thread.currentThread().getContextClassLoader().getResource(xmlResource).getFile());
        final File processClientXsd = new File(Thread.currentThread().getContextClassLoader().getResource(xsdResource).getFile());
        final Source source = new StreamSource(processClientXsd);
        assertThat(processClientXml).as("file should exists:" + processClientXml.getAbsolutePath()).canRead();
        assertThat(processClientXsd).as("file should exists:" + processClientXml.getAbsolutePath()).canRead();

        //when
        validator.setSchemaSource(source);
        validator.validate(processClientXml);

        //then no SValidationException
    }


}
