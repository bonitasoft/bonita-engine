/**
 * Copyright (C) 2017 Bonitasoft S.A.
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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Laurent Leseigneur
 */
public class AutoUpdateConfigurationVisitor extends SimpleFileVisitor<Path> {

    private final List<BonitaConfiguration> bonitaConfigurations;

    private final static Logger LOGGER = LoggerFactory.getLogger(AutoUpdateConfigurationVisitor.class);

    public AutoUpdateConfigurationVisitor(List<BonitaConfiguration> bonitaConfigurations) {
        this.bonitaConfigurations = bonitaConfigurations;
    }

    private static final List<String> AUTO_UPDATE_CONFIGURATION_FILES = Arrays.asList("compound-permissions-mapping.properties",
            "dynamic-permissions-checks.properties", "resources-permissions-mapping.properties");

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attributes) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    private String getFolderName(Path dir) {
        return dir.getFileName().toString().toUpperCase();
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
        if (isAutoUpdateConfigurationFile(path)) {
            final String configurationType = getFolderName(path.getParent());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(buildMessage(path, configurationType));
            }
            bonitaConfigurations.add(new BonitaConfiguration(path.getFileName().toString(), Files.readAllBytes(path)));
        }
        return FileVisitResult.CONTINUE;
    }

    private String buildMessage(Path path, String configurationType) {
        return "found file: " + configurationType.toLowerCase() + "/" + path.getFileName();
    }

    boolean isAutoUpdateConfigurationFile(Path path) {
        return path.toFile().isFile() && AUTO_UPDATE_CONFIGURATION_FILES.contains(path.getFileName().toString());
    }

}
