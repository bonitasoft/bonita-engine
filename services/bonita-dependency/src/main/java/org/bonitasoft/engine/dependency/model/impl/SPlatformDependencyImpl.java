/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.dependency.model.impl;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.dependency.model.SPlatformDependency;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
@Data
@NoArgsConstructor
public class SPlatformDependencyImpl implements SPlatformDependency {

    private long id;
    private long tenantId;
    private String name;
    private String fileName;
    private String description;
    private byte[] value_;

    public SPlatformDependencyImpl(final String name, final String fileName, final byte[] value) {
        super();
        this.name = name;
        this.fileName = fileName;
        value_ = value;
    }

    @Override
    public byte[] getValue() {
        return value_;
    }

}
