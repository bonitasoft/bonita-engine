/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.page;

import static org.apache.commons.io.FileUtils.readFileToByteArray;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstantsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomPageDependenciesResolver {

    private static final String LIB_FOLDER_NAME = "lib";

    static final Map<String, File> PAGES_LIB_TMPDIR = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomPageDependenciesResolver.class.getName());

    private File libTempFolder;

    private final WebBonitaConstantsUtils webBonitaConstantsUtils;

    private final File pageDirectory;

    private final String pageName;

    public CustomPageDependenciesResolver(final String pageName,
            final File pageDirectory,
            final WebBonitaConstantsUtils webBonitaConstantsUtils) {
        this.pageName = pageName;
        this.pageDirectory = pageDirectory;
        this.webBonitaConstantsUtils = webBonitaConstantsUtils;
    }

    public Map<String, byte[]> resolveCustomPageDependencies() {
        final File customPageLibDirectory = new File(pageDirectory, LIB_FOLDER_NAME);
        if (customPageLibDirectory.exists()) {
            this.libTempFolder = new File(this.webBonitaConstantsUtils.getTempFolder(), pageName
                    + new Date().getTime());
            if (!this.libTempFolder.exists()) {
                this.libTempFolder.mkdirs();
            }
            removePageLibTempFolder(pageName);
            PAGES_LIB_TMPDIR.put(pageName, this.libTempFolder);
            return loadLibraries(customPageLibDirectory);
        }
        return Collections.emptyMap();
    }

    private Map<String, byte[]> loadLibraries(final File customPageLibDirectory) {
        final Map<String, byte[]> result = new HashMap<>();
        try {
            Files.walkFileTree(customPageLibDirectory.toPath(), new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                    final File currentFile = file.toFile();
                    result.put(currentFile.getName(), readFileToByteArray(currentFile));
                    return super.visitFile(file, attrs);
                }

            });
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

    public static File removePageLibTempFolder(final String pageName) {
        final File libTempFolder = PAGES_LIB_TMPDIR.remove(pageName);
        if (libTempFolder != null) {
            try {
                FileUtils.deleteDirectory(libTempFolder);
            } catch (final IOException e) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("The custom page temporary lib directory " + libTempFolder.getPath()
                            + " cannot be deleted. This is likely to be due to a JDK bug on Windows. You can safely delete it after a server restart.");
                }
            }
        }
        return libTempFolder;
    }

    public File getTempFolder() {
        if (libTempFolder == null) {
            throw new IllegalStateException("Custom page dependencies must be resolved first.");
        }
        return libTempFolder;
    }

}
