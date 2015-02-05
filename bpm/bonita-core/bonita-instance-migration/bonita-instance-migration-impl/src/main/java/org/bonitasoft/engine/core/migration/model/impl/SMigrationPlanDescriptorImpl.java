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
package org.bonitasoft.engine.core.migration.model.impl;

import java.util.Arrays;

import org.bonitasoft.engine.core.migration.model.SMigrationPlan;
import org.bonitasoft.engine.core.migration.model.SMigrationPlanDescriptor;

/**
 * @author Baptiste Mesta
 */
public class SMigrationPlanDescriptorImpl implements SMigrationPlanDescriptor {

    private static final long serialVersionUID = -6964461911864772156L;

    private byte[] content;

    private String targetProcessVersion;

    private String targetProcessName;

    private String sourceProcessVersion;

    private String sourceProcessName;

    private String description;

    private long id;

    private long tenantId;

    public SMigrationPlanDescriptorImpl() {
    }

    public SMigrationPlanDescriptorImpl(final SMigrationPlan migrationPlan, final byte[] content) {
        targetProcessName = migrationPlan.getTargetName();
        targetProcessVersion = migrationPlan.getTargetVersion();
        sourceProcessVersion = migrationPlan.getSourceVersion();
        sourceProcessName = migrationPlan.getSourceName();
        description = migrationPlan.getDescription();
        this.content = content;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getDiscriminator() {
        return this.getClass().getName();
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getSourceProcessName() {
        return sourceProcessName;
    }

    @Override
    public String getSourceProcessVersion() {
        return sourceProcessVersion;
    }

    @Override
    public String getTargetProcessName() {
        return targetProcessName;
    }

    @Override
    public String getTargetProcessVersion() {
        return targetProcessVersion;
    }

    @Override
    public byte[] getMigrationPlanContent() {
        return content;
    }

    /**
     * @return the content
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * @param content
     *            the content to set
     */
    public void setContent(final byte[] content) {
        this.content = content;
    }

    /**
     * @param targetProcessVersion
     *            the targetProcessVersion to set
     */
    public void setTargetProcessVersion(final String targetProcessVersion) {
        this.targetProcessVersion = targetProcessVersion;
    }

    /**
     * @param targetProcessName
     *            the targetProcessName to set
     */
    public void setTargetProcessName(final String targetProcessName) {
        this.targetProcessName = targetProcessName;
    }

    /**
     * @param sourceProcessVersion
     *            the sourceProcessVersion to set
     */
    public void setSourceProcessVersion(final String sourceProcessVersion) {
        this.sourceProcessVersion = sourceProcessVersion;
    }

    /**
     * @param sourceProcessName
     *            the sourceProcessName to set
     */
    public void setSourceProcessName(final String sourceProcessName) {
        this.sourceProcessName = sourceProcessName;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "SMigrationPlanDescriptorImpl [content=" + Arrays.toString(content) + ", targetProcessVersion=" + targetProcessVersion + ", targetProcessName="
                + targetProcessName + ", sourceProcessVersion=" + sourceProcessVersion + ", sourceProcessName=" + sourceProcessName + ", description="
                + description + ", id=" + id + "]";
    }

    /**
     * @return the tenantId
     */
    public long getTenantId() {
        return tenantId;
    }

    /**
     * @param tenantId
     *            the tenantId to set
     */
    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(content);
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (int) (id ^ id >>> 32);
        result = prime * result + (sourceProcessName == null ? 0 : sourceProcessName.hashCode());
        result = prime * result + (sourceProcessVersion == null ? 0 : sourceProcessVersion.hashCode());
        result = prime * result + (targetProcessName == null ? 0 : targetProcessName.hashCode());
        result = prime * result + (targetProcessVersion == null ? 0 : targetProcessVersion.hashCode());
        result = prime * result + (int) (tenantId ^ tenantId >>> 32);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SMigrationPlanDescriptorImpl other = (SMigrationPlanDescriptorImpl) obj;
        if (!Arrays.equals(content, other.content)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (sourceProcessName == null) {
            if (other.sourceProcessName != null) {
                return false;
            }
        } else if (!sourceProcessName.equals(other.sourceProcessName)) {
            return false;
        }
        if (sourceProcessVersion == null) {
            if (other.sourceProcessVersion != null) {
                return false;
            }
        } else if (!sourceProcessVersion.equals(other.sourceProcessVersion)) {
            return false;
        }
        if (targetProcessName == null) {
            if (other.targetProcessName != null) {
                return false;
            }
        } else if (!targetProcessName.equals(other.targetProcessName)) {
            return false;
        }
        if (targetProcessVersion == null) {
            if (other.targetProcessVersion != null) {
                return false;
            }
        } else if (!targetProcessVersion.equals(other.targetProcessVersion)) {
            return false;
        }
        if (tenantId != other.tenantId) {
            return false;
        }
        return true;
    }

}
