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
package org.bonitasoft.engine.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;

import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Baptiste Mesta
 */
public class APITypeManagerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    @After
    public void before() throws Exception {
        APITypeManager.refresh();
        System.clearProperty("bonita.home");
        System.clearProperty("org.bonitasoft.engine.api-type");
        System.clearProperty("org.bonitasoft.engine.api-type.server.url");
        System.clearProperty("org.bonitasoft.engine.api-type.application.name");
        System.clearProperty("org.bonitasoft.engine.api-type.connections.max");
        System.clearProperty("org.bonitasoft.engine.api-type.basicAuthentication.active");
        System.clearProperty("org.bonitasoft.engine.api-type.basicAuthentication.username");
        System.clearProperty("org.bonitasoft.engine.api-type.basicAuthentication.password");
    }

    @Test
    public void should_getAPIType_when_not_set_return_local() throws Exception {
        ApiAccessType apiType = APITypeManager.getAPIType();

        assertThat(apiType).isEqualTo(ApiAccessType.LOCAL);
    }

    @Test
    public void should_getAPIType_when_not_set_but_have_other_properties_return_local() throws Exception {
        System.setProperty("org.bonitasoft.engine.api-type.server.url", "localhost");

        ApiAccessType apiType = APITypeManager.getAPIType();

        assertThat(apiType).isEqualTo(ApiAccessType.LOCAL);
    }

    @Test
    public void should_getAPIType_when_set_with_system_properties_works() throws Exception {
        //given
        System.setProperty("org.bonitasoft.engine.api-type", "HTTP");
        //when
        ApiAccessType apiType = APITypeManager.getAPIType();
        //then
        assertThat(apiType).isEqualTo(ApiAccessType.HTTP);
    }

    @Test
    public void should_getAPIType_when_set_with_bonita_home_community_properties_works() throws Exception {
        //given
        File bonitaHome = temporaryFolder.newFolder();
        System.setProperty("bonita.home", bonitaHome.getAbsolutePath());
        writePropertiesFile(bonitaHome, "work" + File.separator
                + "bonita-client-community.properties", "org.bonitasoft.engine.api-type = HTTP");
        //when
        ApiAccessType apiType = APITypeManager.getAPIType();
        //then
        assertThat(apiType).isEqualTo(ApiAccessType.HTTP);
    }

    @Test
    public void should_getAPIType_when_set_with_bonita_home_having_spaces_and_slash_works() throws Exception {
        //given
        File bonitaHome = temporaryFolder.newFolder();
        System.setProperty("bonita.home", "      " + bonitaHome.getAbsolutePath() + File.separator + "       ");
        writePropertiesFile(bonitaHome, "work" + File.separator
                + "bonita-client-community.properties", "org.bonitasoft.engine.api-type = HTTP");
        //when
        ApiAccessType apiType = APITypeManager.getAPIType();
        //then
        assertThat(apiType).isEqualTo(ApiAccessType.HTTP);
    }

    @Test
    public void should_getAPIType_when_set_with_bonita_home_custom_properties_works() throws Exception {
        //given
        File bonitaHome = temporaryFolder.newFolder();
        System.setProperty("bonita.home", bonitaHome.getAbsolutePath());
        writePropertiesFile(bonitaHome, "conf" + File.separator
                + "bonita-client-custom.properties", "org.bonitasoft.engine.api-type = HTTP");
        //when
        ApiAccessType apiType = APITypeManager.getAPIType();
        //then
        assertThat(apiType).isEqualTo(ApiAccessType.HTTP);
    }

    @Test
    public void should_getAPIType_when_set_with_bonita_home_community_and_custom_properties_works() throws Exception {
        //given
        File bonitaHome = temporaryFolder.newFolder();
        System.setProperty("bonita.home", bonitaHome.getAbsolutePath());
        writePropertiesFile(bonitaHome, "work" + File.separator
                + "bonita-client-community.properties", "org.bonitasoft.engine.api-type = LOCAL");
        writePropertiesFile(bonitaHome, "conf" + File.separator
                + "bonita-client-custom.properties", "org.bonitasoft.engine.api-type = HTTP");
        //when
        ApiAccessType apiType = APITypeManager.getAPIType();
        //then
        assertThat(apiType).isEqualTo(ApiAccessType.HTTP);
    }

    @Test
    public void should_getAPIType_when_set_with_bonita_home_custom_properties_and_system_properties_works()
            throws Exception {
        //given
        System.setProperty("org.bonitasoft.engine.api-type.server.url", "localhost");
        File bonitaHome = temporaryFolder.newFolder();
        System.setProperty("bonita.home", bonitaHome.getAbsolutePath());
        writePropertiesFile(bonitaHome, "conf" + File.separator
                + "bonita-client-custom.properties", "org.bonitasoft.engine.api-type = HTTP");
        //when
        ApiAccessType apiType = APITypeManager.getAPIType();
        //then
        assertThat(apiType).isEqualTo(ApiAccessType.HTTP);
    }

    @Test
    public void should_getAPIType_when_set_programmatically_should_work() throws Exception {
        //given
        APITypeManager.setAPITypeAndParams(ApiAccessType.HTTP, null);
        //when
        ApiAccessType apiType = APITypeManager.getAPIType();
        //then
        assertThat(apiType).isEqualTo(ApiAccessType.HTTP);
    }

    @Test
    public void should_getAPITypeParameters_when_not_set_return_empty() throws Exception {
        Map<String, String> apiTypeParameters = APITypeManager.getAPITypeParameters();
        assertThat(apiTypeParameters).isEmpty();
    }

    @Test
    public void should_getAPITypeParameters_when_set_with_system_properties_works() throws Exception {
        //given
        System.setProperty("org.bonitasoft.engine.api-type.server.url", "localhost");
        System.setProperty("org.bonitasoft.engine.api-type.application.name", "bonita");
        System.setProperty("org.bonitasoft.engine.api-type.connections.max", "12");
        System.setProperty("org.bonitasoft.engine.api-type.basicAuthentication.active", "true");
        System.setProperty("org.bonitasoft.engine.api-type.basicAuthentication.username", "someUser");
        System.setProperty("org.bonitasoft.engine.api-type.basicAuthentication.password", "secret");
        //when
        Map<String, String> apiTypeParameters = APITypeManager.getAPITypeParameters();
        //then
        assertThat(apiTypeParameters).containsOnly(entry("server.url", "localhost"),
                entry("application.name", "bonita"), entry("connections.max", "12"),
                entry("basicAuthentication.active", "true"), entry("basicAuthentication.username", "someUser"),
                entry("basicAuthentication.password", "secret"));
    }

    @Test
    public void should_getAPITypeParameters_when_set_with_bonita_home_community_properties_works() throws Exception {
        //given
        File bonitaHome = temporaryFolder.newFolder();
        System.setProperty("bonita.home", bonitaHome.getAbsolutePath());
        writePropertiesFile(bonitaHome, "work" + File.separator
                + "bonita-client-community.properties", "server.url = localhost");
        //when
        Map<String, String> apiTypeParameters = APITypeManager.getAPITypeParameters();
        //then
        assertThat(apiTypeParameters).containsOnly(entry("server.url", "localhost"));
    }

    @Test
    public void should_getAPITypeParameters_when_set_with_bonita_home_custom_properties_works() throws Exception {
        //given
        File bonitaHome = temporaryFolder.newFolder();
        System.setProperty("bonita.home", bonitaHome.getAbsolutePath());
        writePropertiesFile(bonitaHome, "conf" + File.separator
                + "bonita-client-custom.properties", "server.url = localhost");
        //when
        Map<String, String> apiTypeParameters = APITypeManager.getAPITypeParameters();
        //then
        assertThat(apiTypeParameters).containsOnly(entry("server.url", "localhost"));
    }

    @Test
    public void should_getAPITypeParameters_when_set_with_bonita_home_community_and_custom_properties_works()
            throws Exception {
        //given
        File bonitaHome = temporaryFolder.newFolder();
        System.setProperty("bonita.home", bonitaHome.getAbsolutePath());
        writePropertiesFile(bonitaHome, "work" + File.separator
                + "bonita-client-community.properties", "server.url = other");
        writePropertiesFile(bonitaHome, "conf" + File.separator
                + "bonita-client-custom.properties", "server.url = localhost");
        //when
        Map<String, String> apiTypeParameters = APITypeManager.getAPITypeParameters();
        //then
        assertThat(apiTypeParameters).containsOnly(entry("server.url", "localhost"));
    }

    @Test
    public void should_getAPITypeParameters_when_set_programmatically_should_work() throws Exception {
        //given
        APITypeManager.setAPITypeAndParams(ApiAccessType.HTTP, Collections.singletonMap("server.url", "localhost"));
        //when
        Map<String, String> apiTypeParameters = APITypeManager.getAPITypeParameters();
        //then
        assertThat(apiTypeParameters).containsOnly(entry("server.url", "localhost"));
    }

    private void writePropertiesFile(File bonitaHome, String fileName, String content) throws IOException {
        File file = new File(
                bonitaHome.getAbsolutePath() + File.separator + "engine-client" + File.separator + fileName);
        file.getParentFile().mkdirs();
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
    }

    @Test(expected = UnknownAPITypeException.class)
    public void should_getAPIType_when_set_with_bad_system_properties_throw_exception() throws Exception {
        //given
        System.setProperty("org.bonitasoft.engine.api-type", "BAD");
        //when
        APITypeManager.getAPIType();
    }

    @Test(expected = UnknownAPITypeException.class)
    public void should_getAPIType_when_set_with_bad_bonita_home_community_properties_throw_exception()
            throws Exception {
        //given
        File bonitaHome = temporaryFolder.newFolder();
        System.setProperty("bonita.home", bonitaHome.getAbsolutePath());
        writePropertiesFile(bonitaHome, "work" + File.separator
                + "bonita-client-community.properties", "org.bonitasoft.engine.api-type = BAD");
        //when
        APITypeManager.getAPIType();
    }

}
