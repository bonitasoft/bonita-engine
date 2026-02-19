/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.IOException;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.security.ForbiddenClassException;
import org.bonitasoft.engine.bpm.actor.impl.ActorDefinitionImpl;
import org.bonitasoft.engine.bpm.businessdata.impl.BusinessDataDefinitionImpl;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.junit.Before;
import org.junit.Test;

public class XmlConverterTest {

    private XmlConverter xmlConverter;

    @Before
    public void setUp() {
        xmlConverter = new XmlConverter();
    }

    @Test
    public void should_fromXML_serialize_entity() {
        // given:
        XmlConverter xmlConverter = new XmlConverter();
        String xml = new StringBuilder("<root>")
                .append("<org.bonitasoft.engine.bpm.actor.impl.ActorDefinitionImpl>")
                .append("<name>test</name>")
                .append("</org.bonitasoft.engine.bpm.actor.impl.ActorDefinitionImpl></root>").toString();
        // when:
        ActorDefinitionImpl actor = xmlConverter.fromXML(xml);

        // then:
        assertThat(actor.getName()).isEqualTo("test");
    }

    @Test
    public void should_fromXML_serialize_entity_when_xml_contains_unknown_elements() {
        // given:

        String xml = new StringBuilder("<root>")
                .append("<org.bonitasoft.engine.bpm.actor.impl.ActorDefinitionImpl>")
                .append("<name>test with unknown xml node</name>")
                .append("<unknown_property_in_xml>test</unknown_property_in_xml>")
                .append("</org.bonitasoft.engine.bpm.actor.impl.ActorDefinitionImpl></root>").toString();
        // when:
        ActorDefinitionImpl actor = xmlConverter.fromXML(xml);

        // then:
        assertThat(actor.getName()).isEqualTo("test with unknown xml node");
    }

    @Test
    public void should_fromXML_always_throws_a_bonita_exception_when_the_xml_is_malformed() {
        // given:
        XmlConverter xmlConverter = new XmlConverter();
        String xml = new StringBuilder("<malformed>")
                .append("<org.bonitasoft.engine.bpm.actor.impl.ActorDefinitionImpl>")
                .append("<name>test")
                .append("</org.bonitasoft.engine.bpm.actor.impl.ActorDefinitionImpl></root>").toString();
        // when:
        Throwable thrown = catchThrowable(() -> xmlConverter.fromXML(xml));

        // then:
        assertThat(thrown)
                .isInstanceOf(BonitaRuntimeException.class)
                .hasMessage("Unable to deserialize object")
                .hasCauseInstanceOf(ConversionException.class);
    }

    @Test
    public void should_reject_deserialization_of_gadget_chain_types() {
        // given: XML payload referencing a denied gadget chain type
        String maliciousXml = "<root>"
                + "<javax.script.ScriptEngineManager/>"
                + "</root>";

        // when:
        Throwable thrown = catchThrowable(() -> xmlConverter.fromXML(maliciousXml));

        // then:
        assertThat(thrown)
                .isInstanceOf(BonitaRuntimeException.class)
                .hasCauseInstanceOf(ForbiddenClassException.class);
    }

    @Test
    public void should_use_custom_deny_list_from_system_property() {
        try {
            // given: custom deny list via system property
            XmlConverter.reset();
            System.setProperty("bonita.xstream.deny.packages", "java.awt.**");

            // when: attempting to deserialize a type matching the custom deny pattern
            Throwable thrown = catchThrowable(() -> new XmlConverter().fromXML("<root><java.awt.Color/></root>"));

            // then:
            assertThat(thrown)
                    .isInstanceOf(BonitaRuntimeException.class)
                    .hasCauseInstanceOf(ForbiddenClassException.class);
        } finally {
            System.clearProperty("bonita.xstream.deny.packages");
            XmlConverter.reset();
        }
    }

    @Test
    public void should_toXml_deserialize_entity() throws IOException {
        //given:
        BusinessDataDefinitionImpl businessDataDefinition = new BusinessDataDefinitionImpl("my name", null);
        businessDataDefinition.setId(12L);
        businessDataDefinition.setDescription("my description");

        //when:
        String xml = xmlConverter.toXML(businessDataDefinition);

        //then:
        assertThat(xml).isEqualTo("<object-stream>\n" +
                "  <org.bonitasoft.engine.bpm.businessdata.impl.BusinessDataDefinitionImpl>\n" +
                "    <id>12</id>\n" +
                "    <name>my name</name>\n" +
                "    <description>my description</description>\n" +
                "    <multiple>false</multiple>\n" +
                "  </org.bonitasoft.engine.bpm.businessdata.impl.BusinessDataDefinitionImpl>\n" +
                "</object-stream>");
    }

}
