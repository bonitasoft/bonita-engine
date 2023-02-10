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

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.io.FileAndContent;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationArchive {

    private FileAndContent organization;
    private FileAndContent bdm;
    private List<FileAndContent> processes = new ArrayList<>();
    private List<FileAndContent> restAPIExtensions = new ArrayList<>();
    private List<FileAndContent> pages = new ArrayList<>();
    private List<FileAndContent> layouts = new ArrayList<>();
    private List<FileAndContent> themes = new ArrayList<>();
    private List<FileAndContent> applications = new ArrayList<>();

    public ApplicationArchive addPage(FileAndContent page) {
        pages.add(page);
        return this;
    }

    public ApplicationArchive addLayout(FileAndContent layout) {
        layouts.add(layout);
        return this;
    }

    public ApplicationArchive addTheme(FileAndContent theme) {
        themes.add(theme);
        return this;
    }

    public ApplicationArchive addRestAPIExtension(FileAndContent extension) {
        restAPIExtensions.add(extension);
        return this;
    }

    public ApplicationArchive addApplication(FileAndContent livingApplication) {
        applications.add(livingApplication);
        return this;
    }

    public ApplicationArchive addProcess(FileAndContent process) {
        processes.add(process);
        return this;
    }
}
