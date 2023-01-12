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

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import org.bonitasoft.engine.io.FileAndContent;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationArchive {

    // class must be present for the javadoc generation
    public static class ApplicationArchiveBuilder {
    }

    private FileAndContent bdm;
    @Singular
    private List<FileAndContent> processes;
    @Singular
    private List<FileAndContent> restAPIExtensions;
    @Singular
    private List<FileAndContent> pages;
    @Singular
    private List<FileAndContent> layouts;
    @Singular
    private List<FileAndContent> themes;
    @Singular
    private List<FileAndContent> applications;

}
