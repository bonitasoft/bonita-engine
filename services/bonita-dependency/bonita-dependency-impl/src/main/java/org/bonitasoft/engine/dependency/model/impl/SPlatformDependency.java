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

import org.bonitasoft.engine.dependency.model.SDependency;

/**
 * @author Matthieu Chaffotte
 */
public class SPlatformDependency implements SDependency {

    private static final long serialVersionUID = -5880182078631771416L;

    private long id;

    private long tenantId;

    private String name;

    private String fileName;

    private String description;

    private byte[] value_;

    public SPlatformDependency() {
        // default constructor for hibernate
    }

    public SPlatformDependency(final String name, final String fileName, final byte[] value) {
        super();
        this.name = name;
        this.fileName = fileName;
        value_ = value;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public void setTenantId(final long id) {
        this.tenantId = id;
    }

    @Override
    public long getId() {
        return id;
    }

    public byte[] getValue_() {
        return value_;
    }

    public void setValue_(final byte[] value_) {
        this.value_ = value_;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public byte[] getValue() {
        return value_;
    }

    @Override
    public String getDiscriminator() {
        return SPlatformDependency.class.getName();
    }

}
