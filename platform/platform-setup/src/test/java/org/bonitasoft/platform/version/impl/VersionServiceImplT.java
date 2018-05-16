/*
 * Copyright (C) 2016 Bonitasoft S.A.
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
 */
package org.bonitasoft.platform.version.impl;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.bonitasoft.platform.setup.PlatformSetupApplication;
import org.bonitasoft.platform.setup.ScriptExecutor;
import org.bonitasoft.platform.setup.jndi.MemoryJNDISetup;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author laurent Leseigneur
 */
@RunWith(SpringRunner.class)

//keep order
@SpringBootTest(classes = {
        PlatformSetupApplication.class })
@ComponentScan(basePackages = { "org.bonitasoft.platform.setup", "org.bonitasoft.platform.configuration", "org.bonitasoft.platform.version" })
@PropertySource("classpath:/application.properties")
@Component
public class VersionServiceImplT {

    @Autowired
    MemoryJNDISetup memoryJNDISetup;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${db.vendor}")
    String dbVendor;

    @Autowired
    VersionServiceImpl versionService;

    @Autowired
    DataSource dataSource;

    @Autowired
    ScriptExecutor scriptExecutor;

    @Before
    public void setUpDb() throws Exception {
        scriptExecutor.createAndInitializePlatformIfNecessary();
    }

    @After
    public void cleanUpDB() throws Exception {
        scriptExecutor.deleteTables();
    }

    @Test
    public void should_insert_version_in_database() throws Exception {
        //when
        final String platformVersion = versionService.getPlatformVersion();

        //then
        assertThat(platformVersion).as("should return same version").isEqualTo(versionService.getPlatformSetupVersion());
    }

    @Test
    public void should_have_same_version() throws Exception {
        //then
        assertThat(versionService.isValidPlatformVersion()).as("should insert valid version").isTrue();
    }

    @Test
    public void should_not_have_same_version() throws Exception {
        //given
        jdbcTemplate.execute("UPDATE platform set version='a.b.c' ");

        //then
        assertThat(versionService.isValidPlatformVersion()).as("should insert valid version").isFalse();
    }

}
