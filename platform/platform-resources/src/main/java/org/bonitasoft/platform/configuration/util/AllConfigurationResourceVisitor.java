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

    private final static Logger LOGGER = LoggerFactory.getLogger(AllConfigurationResourceVisitor.class);

    private static final List<String> PLATFORM_FOLDERS = Arrays.asList(PLATFORM_PORTAL.name().toLowerCase(),
            PLATFORM_INIT_ENGINE.name().toLowerCase(), PLATFORM_ENGINE.name().toLowerCase(), TENANT_TEMPLATE_ENGINE.name().toLowerCase(),
            TENANT_TEMPLATE_SECURITY_SCRIPTS.name().toLowerCase(), TENANT_TEMPLATE_PORTAL.name().toLowerCase());

    private static final List<String> TENANT_FOLDERS = Arrays.asList(TENANT_PORTAL.name().toLowerCase(), TENANT_ENGINE.name().toLowerCase(),
            TENANT_SECURITY_SCRIPTS.name().toLowerCase());

    public AllConfigurationResourceVisitor(List<FullBonitaConfiguration> fullBonitaConfigurations) {
        this.fullBonitaConfigurations = fullBonitaConfigurations;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    private String getFolderName(Path dir) {
        return dir.getFileName().toString().toUpperCase();
    }

    private boolean isTenantFolder(Path dir) {
        return TENANT_FOLDERS.contains(dir.getFileName().toString());
    }

    private Long getTenantId(Path dir) {
        try {
            return Long.parseLong(dir.getParent().getFileName().toString());
        } catch (NumberFormatException e) {
            return 0L;
        }

    }

    private boolean isPlatformFolder(Path dir) {
        return PLATFORM_FOLDERS.contains(dir.getFileName().toString());

    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
        if (isConfigurationFile(path)) {
            final Long tenantId = getTenantId(path.getParent());
            final String configurationType = getFolderName(path.getParent());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(buildMessage(path, tenantId, configurationType));
            }
            fullBonitaConfigurations.add(new FullBonitaConfiguration(path.getFileName().toString(), Files.readAllBytes(path), configurationType, tenantId));
        }
        return FileVisitResult.CONTINUE;
    }

    private String buildMessage(Path path, Long tenantId, String configurationType) {
        final StringBuilder message = new StringBuilder("found file: ");
        if (tenantId > 0) {
            message.append("tenants/").append(tenantId).append("/").append(configurationType.toLowerCase());
        } else {
            message.append(configurationType.toLowerCase());
        }
        message.append("/").append(path.getFileName());
        return message.toString();
    }

    private boolean isConfigurationFile(Path path) {
        final Path parentFolder = path.getParent();
        return path.toFile().isFile() && (isTenantFolder(parentFolder) || isPlatformFolder(parentFolder));
    }

}
