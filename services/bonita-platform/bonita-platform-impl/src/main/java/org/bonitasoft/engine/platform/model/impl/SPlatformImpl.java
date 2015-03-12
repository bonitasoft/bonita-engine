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
package org.bonitasoft.engine.platform.model.impl;

import org.bonitasoft.engine.platform.model.SPlatform;

/**
 * @author Charles Souillard
 */
public class SPlatformImpl implements SPlatform {

    private static final long serialVersionUID = 1L;

    private long tenantId;

    private long id;

    private long created;

    private String createdBy;

    private String initialVersion;

    private String previousVersion;

    private String version;

    private SPlatformImpl() {
        // for mybatis
    }

    public SPlatformImpl(final String version, final String previousVersion, final String initialVersion, final String createdBy, final long created) {
        this.version = version;
        this.previousVersion = previousVersion;
        this.initialVersion = initialVersion;
        this.createdBy = createdBy;
        this.created = created;
    }

    public long getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public long getId() {
        return this.id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public long getCreated() {
        return this.created;
    }

    public void setCreated(final long created) {
        this.created = created;
    }

    @Override
    public String getCreatedBy() {
        return this.createdBy;
    }

    public void setCreatedBy(final String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String getInitialVersion() {
        return this.initialVersion;
    }

    public void setInitialVersion(final String initialVersion) {
        this.initialVersion = initialVersion;
    }

    @Override
    public String getPreviousVersion() {
        return this.previousVersion;
    }

    public void setPreviousVersion(final String previousVersion) {
        this.previousVersion = previousVersion;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    @Override
    public String getDiscriminator() {
        return SPlatform.class.getName();
    }

    @Override
    public String toString() {
        return "SPlatformImpl [created=" + this.created + ", createdBy=" + this.createdBy + ", id=" + this.id + ", initialVersion=" + this.initialVersion
                + ", previousVersion=" + this.previousVersion + ", version=" + this.version + "]";
    }

}
