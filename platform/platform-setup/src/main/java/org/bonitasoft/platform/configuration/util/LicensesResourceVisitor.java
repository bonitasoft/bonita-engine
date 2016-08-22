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

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Laurent Leseigneur
 */
public class LicensesResourceVisitor extends SimpleFileVisitor<Path> {

    private final List<BonitaConfiguration> bonitaConfigurations;

    private final static Logger LOGGER = LoggerFactory.getLogger(LicensesResourceVisitor.class);
    private Path dir;

    public LicensesResourceVisitor(List<BonitaConfiguration> bonitaConfigurations) {
        this.bonitaConfigurations = bonitaConfigurations;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        initRootFolder(dir);
        if (isSubFolder(dir)){
            return SKIP_SUBTREE;
        }
        return CONTINUE;
    }

    private boolean isSubFolder(Path dir) {
        return !this.dir.equals(dir);
    }

    private void initRootFolder(Path dir) {
        if (this.dir == null) {
            this.dir = dir;
        }
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(basicFileAttributes);
        final File file = path.toFile();
        if (isLicenseFile(file)) {
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                LOGGER.info("found license file: " + file.getName());
                bonitaConfigurations.add(new BonitaConfiguration(file.getName(), IOUtils.toByteArray(fileInputStream)));
            }
        }
        return CONTINUE;
    }

    private boolean isLicenseFile(File file) {
        return file.isFile() && file.getName().endsWith(".lic");
    }

}
