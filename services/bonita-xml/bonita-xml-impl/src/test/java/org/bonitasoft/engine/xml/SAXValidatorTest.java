/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Laurent Leseigneur
 */
public class SAXValidatorTest {

    private static final String SERVEUR_PROCESS_SCHEMA = "/org/bonitasoft/engine/core/process/definition/model/builder/impl/SProcessDefinition.xsd";
    private static final String CLIENT_PROCESS_SCHEMA = "/org/bonitasoft/engine/bpm/bar/ProcessDefinition.xsd";
    private XMLSchemaValidator validator;

    @Before
    public void before() throws Exception {
        final SAXValidatorFactory saxValidatorFactory = new SAXValidatorFactory();
        validator = saxValidatorFactory.createValidator();
    }

    @Test
    public void should_validate_client_xml_with_contract() throws Exception {
        check_xml_file_is_valid(CLIENT_PROCESS_SCHEMA, "/client-process-design.xml");
    }

    @Test
    public void should_validate_serveur_xml_with_contract() throws Exception {
        check_xml_file_is_valid(SERVEUR_PROCESS_SCHEMA, "/server-process-definition.xml");
    }

    private void check_xml_file_is_valid(final String xsdResource, final String xmlResource) throws URISyntaxException, SInvalidSchemaException,
    SValidationException, IOException {
        //given
        final InputStream xslStream = this.getClass().getResourceAsStream(xmlResource);
        final InputStream xsdStream = this.getClass().getResourceAsStream(xsdResource);
        final StreamSource source = new StreamSource(xsdStream);

        //when then no SValidationException
        validator.setSchema(source);
        validator.validate(xslStream);
    }


}
