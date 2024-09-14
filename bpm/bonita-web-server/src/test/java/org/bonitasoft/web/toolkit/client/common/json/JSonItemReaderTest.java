/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.web.toolkit.client.common.json;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.web.rest.model.application.AbstractApplicationDefinition;
import org.bonitasoft.web.rest.model.application.ApplicationItem;
import org.bonitasoft.web.rest.model.application.ApplicationLinkItem;
import org.junit.Test;

public class JSonItemReaderTest {

    @Test
    public void should_parse_an_implicit_LegacyApplication_from_AbstractApplicationDefinition() throws Exception {
        // given
        String json = """
                {
                  "version": "1.0",
                  "profileId": "2",
                  "token": "myapp",
                  "displayName": "My app",
                  "description": "My application description"
                }
                """;
        // when
        var app = JSonItemReader.parseItem(json, AbstractApplicationDefinition.get());
        // then
        assertThat(app).isExactlyInstanceOf(ApplicationItem.class);
    }

    @Test
    public void should_parse_an_explicit_LegacyApplication_from_AbstractApplicationDefinition() throws Exception {
        // given
        String json = """
                {
                  "link": "false",
                  "version": "1.0",
                  "profileId": "2",
                  "token": "myapp",
                  "displayName": "My app",
                  "description": "My application description"
                }
                """;
        // when
        var app = JSonItemReader.parseItem(json, AbstractApplicationDefinition.get());
        // then
        assertThat(app).isExactlyInstanceOf(ApplicationItem.class);
    }

    @Test
    public void should_parse_an_ApplicationLink_from_AbstractApplicationDefinition() throws Exception {
        // given
        String json = """
                {
                  "link": "true",
                  "version": "1.0",
                  "profileId": "2",
                  "token": "myapp",
                  "displayName": "My app",
                  "description": "My application description"
                }
                """;
        // when
        var app = JSonItemReader.parseItem(json, AbstractApplicationDefinition.get());
        // then
        assertThat(app).isExactlyInstanceOf(ApplicationLinkItem.class);
    }

}
