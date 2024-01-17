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
package org.bonitasoft.platform.configuration.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Laurent Leseigneur
 */
public class ConfigurationResourceVisitorTest {

    public static final int CURRENT_NUMBER_OF_CONFIGURATION_FILES = 2;

    private final static Logger LOGGER = LoggerFactory.getLogger(ConfigurationResourceVisitor.class);

    @Test
    public void should_read_configuration_folder() throws Exception {
        //given
        Path rootFolder = Paths.get(getClass().getResource("/conf").toURI());
        LOGGER.error("folder:" + rootFolder);
        final List<BonitaConfiguration> bonitaConfigurations = new ArrayList<>();

        //when
        final ConfigurationResourceVisitor resourceVisitor = new ConfigurationResourceVisitor(bonitaConfigurations);
        Files.walkFileTree(rootFolder, resourceVisitor);

        //then
        Assertions.assertThat(bonitaConfigurations).hasSize(CURRENT_NUMBER_OF_CONFIGURATION_FILES);
    }
}
