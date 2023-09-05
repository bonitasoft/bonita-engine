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
package org.bonitasoft.engine.api.impl.application.installer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationArchive implements AutoCloseable {

    private File organization;
    private File bdm;
    private List<File> processes = new ArrayList<>();
    private List<File> restAPIExtensions = new ArrayList<>();
    private List<File> pages = new ArrayList<>();
    private List<File> layouts = new ArrayList<>();
    private List<File> themes = new ArrayList<>();
    private List<File> applications = new ArrayList<>();

    private List<File> ignoredFiles = new ArrayList<>();

    @Singular
    private Optional<InputStream> configurationFile = Optional.empty();

    public ApplicationArchive addPage(File page) {
        pages.add(page);
        return this;
    }

    public ApplicationArchive addLayout(File layout) {
        layouts.add(layout);
        return this;
    }

    public ApplicationArchive addTheme(File theme) {
        themes.add(theme);
        return this;
    }

    public ApplicationArchive addRestAPIExtension(File extension) {
        restAPIExtensions.add(extension);
        return this;
    }

    public ApplicationArchive addApplication(File livingApplication) {
        applications.add(livingApplication);
        return this;
    }

    public ApplicationArchive addProcess(File process) {
        processes.add(process);
        return this;
    }

    public ApplicationArchive addIgnoredFile(File file) {
        ignoredFiles.add(file);
        return this;
    }

    public ApplicationArchive setBdm(File bdm) {
        this.bdm = bdm;
        return this;
    }

    /**
     * @return <code>true</code> if the application archive has no artifact
     */
    public boolean isEmpty() {
        return organization == null &&
                bdm == null &&
                processes.isEmpty() &&
                restAPIExtensions.isEmpty() &&
                pages.isEmpty() &&
                layouts.isEmpty() &&
                themes.isEmpty() &&
                applications.isEmpty();
    }

    @Override
    public void close() throws Exception {
        cleanPhysicalArtifacts();
    }

    protected void cleanPhysicalArtifacts() throws IOException {
        if (organization != null) {
            Files.deleteIfExists(organization.toPath());
        }
        if (bdm != null) {
            Files.deleteIfExists(bdm.toPath());
        }
        deletePhysicalFilesFromList(processes);
        deletePhysicalFilesFromList(restAPIExtensions);
        deletePhysicalFilesFromList(pages);
        deletePhysicalFilesFromList(layouts);
        deletePhysicalFilesFromList(themes);
        deletePhysicalFilesFromList(applications);
        deletePhysicalFilesFromList(ignoredFiles);
    }

    protected void deletePhysicalFilesFromList(List<File> list) throws IOException {
        for (File f : list) {
            Files.deleteIfExists(f.toPath());
        }
    }
}
