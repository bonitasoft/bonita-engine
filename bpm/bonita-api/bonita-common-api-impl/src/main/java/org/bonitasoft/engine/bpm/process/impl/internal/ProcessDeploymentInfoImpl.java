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
package org.bonitasoft.engine.bpm.process.impl.internal;

import java.util.Date;

import org.bonitasoft.engine.bpm.internal.NamedElementImpl;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;

/**
 * @author Baptiste Mesta
 * @author Hongwen Zang
 * @author Celine Souchet
 */
public class ProcessDeploymentInfoImpl extends NamedElementImpl implements ProcessDeploymentInfo {

    private static final long serialVersionUID = 1138141364845684630L;

    private final String version;

    private final String displayDescription;

    private final Date deploymentDate;

    private final long deployedBy;

    private final ConfigurationState configurationState;

    private final ActivationState activationState;

    private final long processId;

    private final String displayName;

    private final Date lastUpdateDate;

    private final String iconPath;

    private final String description;

    public ProcessDeploymentInfoImpl(final long id, final long processId, final String name, final String version, final String description,
            final Date deploymentDate, final long deployedBy, final ActivationState activationState, final ConfigurationState configurationState,
            final String displayName, final Date lastUpdateDate, final String iconPath,
            final String displayDescription) {
        super(name);
        this.description = description;
        this.displayDescription = displayDescription;
        setId(id);
        this.processId = processId;
        this.version = version;
        this.deploymentDate = deploymentDate;
        this.deployedBy = deployedBy;
        this.configurationState = configurationState;
        this.activationState = activationState;
        this.displayName = displayName;
        this.lastUpdateDate = lastUpdateDate;
        this.iconPath = iconPath;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getDisplayDescription() {
        return displayDescription;
    }

    @Override
    public Date getDeploymentDate() {
        return deploymentDate;
    }

    @Override
    public long getDeployedBy() {
        return deployedBy;
    }

    @Override
    public long getProcessId() {
        return processId;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    @Override
    public String getIconPath() {
        return iconPath;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public ConfigurationState getConfigurationState() {
        return configurationState;
    }

    @Override
    public ActivationState getActivationState() {
        return activationState;
    }

    @Override
    public String toString() {
        return "ProcessDeploymentInfoImpl{" +
                "version='" + version + '\'' +
                ", displayDescription='" + displayDescription + '\'' +
                ", deploymentDate=" + deploymentDate +
                ", deployedBy=" + deployedBy +
                ", configurationState=" + configurationState +
                ", activationState=" + activationState +
                ", processId=" + processId +
                ", displayName='" + displayName + '\'' +
                ", lastUpdateDate=" + lastUpdateDate +
                ", iconPath='" + iconPath + '\'' +
                ", description='" + description + '\'' +
                ", namedElement='" + super.toString() + '\'' +
                '}';
    }
}
