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
