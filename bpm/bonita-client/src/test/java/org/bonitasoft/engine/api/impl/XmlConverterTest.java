package org.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.IOException;

import org.bonitasoft.engine.bpm.actor.impl.ActorDefinitionImpl;
import org.bonitasoft.engine.bpm.businessdata.impl.BusinessDataDefinitionImpl;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.junit.Before;
import org.junit.Test;

import com.thoughtworks.xstream.converters.ConversionException;

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
                .hasMessageStartingWith("Unable to deserialize object <malformed><org.bonitasoft.engine.")
                .hasCauseInstanceOf(ConversionException.class);
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
