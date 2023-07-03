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
package org.bonitasoft.engine.bpm.bar;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

/**
 * @author Laurent Leseigneur
 */
class BarResourceVisitor extends SimpleFileVisitor<Path> {

    private BusinessArchive businessArchive;
    private final Path barRootFolder;
    private Path barResourceFolder;
    private int resourcesCount;

    public BarResourceVisitor(BusinessArchive businessArchive, Path barRootFolder) {
        this.businessArchive = businessArchive;
        this.barRootFolder = barRootFolder;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (dir.equals(Paths.get(barRootFolder + "/" + ExternalResourceContribution.EXTERNAL_RESOURCE_FOLDER))) {
            this.barResourceFolder = dir;
            return FileVisitResult.CONTINUE;
        }
        if (dir.equals(barRootFolder)) {
            return FileVisitResult.CONTINUE;
        }
        if (barResourceFolder != null && dir.startsWith(barResourceFolder)) {
            return FileVisitResult.CONTINUE;
        }
        return FileVisitResult.SKIP_SUBTREE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(attrs);
        businessArchive.addResource(barRootFolder.relativize(file).toString().replace(File.separator, "/"),
                Files.readAllBytes(file));
        resourcesCount++;
        return FileVisitResult.CONTINUE;
    }

    public int getResourcesCount() {
        return resourcesCount;
    }

}
