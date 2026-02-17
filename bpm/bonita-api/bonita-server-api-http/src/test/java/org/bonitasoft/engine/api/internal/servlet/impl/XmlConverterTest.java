/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.internal.servlet.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.thoughtworks.xstream.security.ForbiddenClassException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;

public class XmlConverterTest {

    @Rule
    public RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    private final XmlConverter xmlConverter = new XmlConverter();

    @After
    public void resetXStream() {
        XmlConverter.reset();
    }

    @Test
    public void should_reject_deserialization_of_gadget_chain_types() {
        // given: XML payload referencing a known gadget chain type
        String maliciousXml = "<root>"
                + "<org.apache.commons.collections4.comparators.TransformingComparator>"
                + "<decorated/><transformer/>"
                + "</org.apache.commons.collections4.comparators.TransformingComparator>"
                + "</root>";

        // when:
        Throwable thrown = catchThrowable(() -> xmlConverter.fromXML(maliciousXml));

        // then:
        assertThat(thrown)
                .isInstanceOf(BonitaRuntimeException.class)
                .hasCauseInstanceOf(ForbiddenClassException.class);
    }

    @Test
    public void should_reject_deserialization_of_javax_script_gadget_chain_types() {
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
    public void should_allow_deserialization_of_bonita_types() {
        // given: XML payload with a legitimate Bonita type
        String xml = "<root>"
                + "<org.bonitasoft.engine.bpm.actor.impl.ActorDefinitionImpl>"
                + "<name>test</name>"
                + "</org.bonitasoft.engine.bpm.actor.impl.ActorDefinitionImpl>"
                + "</root>";

        // when:
        Object result = xmlConverter.fromXML(xml);

        // then:
        assertThat(result).isNotNull();
    }

    @Test
    public void should_use_custom_deny_list_from_system_property() {
        // given: custom deny list via system property
        XmlConverter.reset();
        System.setProperty("bonita.xstream.deny.packages", "java.awt.**");

        // when: attempting to deserialize a type matching the custom deny pattern
        Throwable thrown = catchThrowable(() -> new XmlConverter().fromXML("<root><java.awt.Color/></root>"));

        // then:
        assertThat(thrown)
                .isInstanceOf(BonitaRuntimeException.class)
                .hasCauseInstanceOf(ForbiddenClassException.class);
    }
}
