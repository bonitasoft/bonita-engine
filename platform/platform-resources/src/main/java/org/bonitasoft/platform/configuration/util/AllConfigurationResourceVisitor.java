/**
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
 **/
package org.bonitasoft.platform.configuration.util;

import static org.bonitasoft.platform.configuration.type.ConfigurationType.*;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.platform.configuration.model.FullBonitaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Laurent Leseigneur
 */
public class AllConfigurationResourceVisitor extends SimpleFileVisitor<Path> {

    private final List<FullBonitaConfiguration> fullBonitaConfigurations;

    private static final Logger LOGGER = LoggerFactory.getLogger(AllConfigurationResourceVisitor.class);

    private static final List<String> CONFIGURATION_FOLDERS = Arrays.asList(PLATFORM_PORTAL.name().toLowerCase(),
            PLATFORM_ENGINE.name().toLowerCase(), TENANT_PORTAL.name().toLowerCase(),
            TENANT_ENGINE.name().toLowerCase(), TENANT_SECURITY_SCRIPTS.name().toLowerCase());

    public AllConfigurationResourceVisitor(List<FullBonitaConfiguration> fullBonitaConfigurations) {
        this.fullBonitaConfigurations = fullBonitaConfigurations;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        return FileVisitResult.CONTINUE;
    }

    private String getFolderName(Path dir) {
        return dir.getFileName().toString().toUpperCase();
    }

    private boolean isConfigurationFolder(Path dir) {
        return CONFIGURATION_FOLDERS.contains(dir.getFileName().toString());
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
        if (isConfigurationFile(path)) {
            final String configurationType = getFolderName(path.getParent());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("found file: {}/{}", configurationType.toLowerCase(), path.getFileName());
            }
            fullBonitaConfigurations.add(new FullBonitaConfiguration(path.getFileName().toString(),
                    Files.readAllBytes(path), configurationType));
        }
        return FileVisitResult.CONTINUE;
    }

    private boolean isConfigurationFile(Path path) {
        return path.toFile().isFile() && isConfigurationFolder(path.getParent());
    }

}
