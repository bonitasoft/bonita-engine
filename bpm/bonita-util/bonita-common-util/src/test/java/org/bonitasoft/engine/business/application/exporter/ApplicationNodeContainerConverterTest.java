/**
 * Copyright (C) 2016 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.business.application.exporter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.bonitasoft.engine.business.application.xml.ApplicationNodeBuilder.newApplication;
import static org.bonitasoft.engine.business.application.xml.ApplicationNodeBuilder.newApplicationContainer;

import java.io.ByteArrayInputStream;

import javax.xml.bind.UnmarshalException;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.business.application.xml.ApplicationNodeContainer;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xml.sax.InputSource;

public class ApplicationNodeContainerConverterTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void should_marshall_applicationContainer_to_xml() throws Exception {
        final ApplicationNodeContainerConverter converter = new ApplicationNodeContainerConverter();

        final byte[] xml = converter.marshallToXML(newApplicationContainer()
                .havingApplications(newApplication("myApp", "My App", "1.0")).create());

        XMLAssert.assertXMLEqual(XMLUnit.buildControlDocument(new InputSource(new ByteArrayInputStream(xml))),
                XMLUnit.buildTestDocument(new InputSource(
                        ApplicationNodeContainerConverterTest.class.getResourceAsStream("/application.xml"))));
    }

    @Test
    public void should_unmarshall_xml_into_applicationContainer() throws Exception {
        final ApplicationNodeContainerConverter converter = new ApplicationNodeContainerConverter();

        final ApplicationNodeContainer container = converter.unmarshallFromXML(
                IOUtils.toByteArray(ApplicationNodeContainerConverterTest.class.getResourceAsStream("/application.xml")));

        assertThat(container).isNotNull();
        assertThat(container.getApplications()).extracting("token", "displayName", "version")
                .contains(tuple("myApp", "My App", "1.0"));

    }

    @Test
    public void should_fail_unmarshall_xml_into_applicationContainer() throws Exception {
        final ApplicationNodeContainerConverter converter = new ApplicationNodeContainerConverter();

        expectedException.expect(UnmarshalException.class);

        converter.unmarshallFromXML(
                IOUtils.toByteArray(ApplicationNodeContainerConverterTest.class.getResourceAsStream("/badApplication.xml")));
    }

}
