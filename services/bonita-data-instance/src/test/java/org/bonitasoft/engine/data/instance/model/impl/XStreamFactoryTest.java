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
package org.bonitasoft.engine.data.instance.model.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.thoughtworks.xstream.XStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemErrRule;

public class XStreamFactoryTest {

    @Test
    public void should_provide_xstream() {
        //when
        final XStream xStream = XStreamFactory.getXStream();

        //then
        assertThat(xStream).as("should provide xstream instance").isNotNull();
    }

    @Test
    public void should_create_xstream_only_once_in_same_classLoader() {
        //when
        final XStream xStream = XStreamFactory.getXStream();
        final XStream xStream2 = XStreamFactory.getXStream();

        //then
        assertThat(xStream2).as("should provide the xstream instance when we are in the same classloader")
                .isSameAs(xStream);
    }

    @Rule
    public SystemErrRule systemErrRule = new SystemErrRule().enableLog();

    @Test
    public void xStream_should_not_warn_about_security() throws Exception {
        // given:
        final XStream xStream = XStreamFactory.getXStream();

        // when:
        xStream.fromXML(
                "<org.bonitasoft.engine.commons.Container><id>17</id><type>executable</type></org.bonitasoft.engine.commons.Container>");

        // then:
        assertThat(systemErrRule.getLog()).doesNotContain("XStream is probably vulnerable");
    }
}
