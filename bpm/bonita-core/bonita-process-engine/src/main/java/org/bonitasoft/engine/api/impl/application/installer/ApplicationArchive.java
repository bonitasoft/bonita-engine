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
import java.util.ArrayList;
import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationArchive {

    private File organization;
    private File bdm;
    private List<File> processes = new ArrayList<>();
    private List<File> restAPIExtensions = new ArrayList<>();
    private List<File> pages = new ArrayList<>();
    private List<File> layouts = new ArrayList<>();
    private List<File> themes = new ArrayList<>();
    private List<File> applications = new ArrayList<>();

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

    public ApplicationArchive setBdm(File bdm) {
        this.bdm = bdm;
        return this;
    }

}
