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
package org.bonitasoft.platform.configuration.impl;

import static org.bonitasoft.platform.configuration.impl.ConfigurationFields.RESOURCE_CONTENT;
import static org.bonitasoft.platform.configuration.impl.ConfigurationFields.RESOURCE_NAME;
import static org.bonitasoft.platform.configuration.model.BonitaConfigurationAssert.assertThat;

import java.sql.ResultSet;

import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Laurent Leseigneur
 */
@RunWith(MockitoJUnitRunner.class)
public class BonitaConfigurationRowMapperTest {

    @Mock
    private ResultSet rs;

    @Before
    public void setup() throws Exception {
        Mockito.doReturn("my resource").when(rs).getString(RESOURCE_NAME);
        Mockito.doReturn("my content".getBytes()).when(rs).getBytes(RESOURCE_CONTENT);
    }

    @Test
    public void testMapRow() throws Exception {
        //given
        BonitaConfigurationRowMapper bonitaConfigurationRowMapper = new BonitaConfigurationRowMapper();

        //when
        final BonitaConfiguration bonitaConfiguration = bonitaConfigurationRowMapper.mapRow(rs, 5);

        //then
        assertThat(bonitaConfiguration)
                .hasResourceName("my resource")
                .hasResourceContent("my content".getBytes());
    }
}
