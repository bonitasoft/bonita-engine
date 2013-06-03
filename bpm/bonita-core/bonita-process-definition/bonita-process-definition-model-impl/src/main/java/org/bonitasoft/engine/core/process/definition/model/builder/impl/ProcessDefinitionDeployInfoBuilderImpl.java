/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.definition.model.builder.impl;

import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.definition.model.builder.ProcessDefinitionDeployInfoBuilder;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionDeployInfoImpl;

/**
 * @author Baptiste Mesta
 * @author Yanyan Liu
 * @author Hongwen Zang
 * @author Celine Souchet
 */
public class ProcessDefinitionDeployInfoBuilderImpl implements ProcessDefinitionDeployInfoBuilder {

    private SProcessDefinitionDeployInfoImpl entity;

    private static final String DESCRIPTION = "description";

    private static final String ID_KEY = "id";

    private static final String NAME_KEY = "name";

    private static final String VERSION_KEY = "version";

    private static final String DEPLOYMENT_DATE_KEY = "deploymentDate";

    private static final String DEPLOYED_BY_KEY = "deployedBy";

    private static final String ACTIVATION_STATE_KEY = "activationState";

    private static final String CONFIGURATION_STATE_KEY = "configurationState";

    private static final String PROCESS_ID_KEY = "processId";

    private static final String DISPLAY_NAME_KEY = "displayName";

    private static final String DISPLAY_DESCRIPTION_KEY = "displayDescription";

    private static final String LAST_UPDATE_DATE_KEY = "lastUpdateDate";

    private static final String ICON_PATH = "iconPath";

    private static final String LABEL = "label";

    @Override
    public ProcessDefinitionDeployInfoBuilder createNewInstance(final String name, final String version) {
        entity = new SProcessDefinitionDeployInfoImpl();
        entity.setName(name);
        entity.setVersion(version);
        entity.setDisplayName(name); // default value for the displayName (when the process deployment info object is created) should be the process name
        final long now = System.currentTimeMillis();
        entity.setLastUpdateDate(now);
        return this;
    }

    @Override
    public ProcessDefinitionDeployInfoBuilder setProcessId(final long processId) {
        entity.setProcessId(processId);
        return this;
    }

    @Override
    public ProcessDefinitionDeployInfoBuilder setDescription(final String description) {
        entity.setDescription(description);
        return this;
    }

    @Override
    public ProcessDefinitionDeployInfoBuilder setDeploymentDate(final long deploymentDate) {
        entity.setDeploymentDate(deploymentDate);
        return this;
    }

    @Override
    public ProcessDefinitionDeployInfoBuilder setDeployedBy(final long deployedBy) {
        entity.setDeployedBy(deployedBy);
        return this;
    }

    @Override
    public ProcessDefinitionDeployInfoBuilder setConfigurationState(final String state) {
        entity.setConfigurationState(state);
        return this;
    }

    @Override
    public ProcessDefinitionDeployInfoBuilder setActivationState(final String state) {
        entity.setActivationState(state);
        return this;
    }

    @Override
    public ProcessDefinitionDeployInfoBuilder setDisplayName(final String displayName) {
        entity.setDisplayName(displayName);
        return this;
    }

    @Override
    public ProcessDefinitionDeployInfoBuilder setLastUpdateDate(final long lastUpdateDate) {
        entity.setLastUpdateDate(lastUpdateDate);
        return this;
    }

    @Override
    public ProcessDefinitionDeployInfoBuilder setDisplayDescription(final String displayDescription) {
        entity.setDisplayDescription(displayDescription);
        return this;
    }

    @Override
    public ProcessDefinitionDeployInfoBuilder setId(final long id) {
        entity.setId(id);
        return this;
    }

    @Override
    public ProcessDefinitionDeployInfoBuilder setIconPath(final String iconPath) {
        entity.setIconPath(iconPath);
        return this;
    }

    @Override
    public SProcessDefinitionDeployInfo done() {
        return entity;
    }

    @Override
    public String getNameKey() {
        return NAME_KEY;
    }

    @Override
    public String getDescriptionKey() {
        return DESCRIPTION;
    }

    @Override
    public String getVersionKey() {
        return VERSION_KEY;
    }

    @Override
    public String getIdKey() {
        return ID_KEY;
    }

    @Override
    public String getDeploymentDateKey() {
        return DEPLOYMENT_DATE_KEY;
    }

    @Override
    public String getActivationStateKey() {
        return ACTIVATION_STATE_KEY;
    }

    @Override
    public String getConfigurationStateKey() {
        return CONFIGURATION_STATE_KEY;
    }

    @Override
    public String getDeployedByKey() {
        return DEPLOYED_BY_KEY;
    }

    @Override
    public String getProcessIdKey() {
        return PROCESS_ID_KEY;
    }

    @Override
    public String getDisplayNameKey() {
        return DISPLAY_NAME_KEY;
    }

    @Override
    public String getLastUpdateDateKey() {
        return LAST_UPDATE_DATE_KEY;
    }

    @Override
    public String getDisplayDescriptionKey() {
        return DISPLAY_DESCRIPTION_KEY;
    }

    @Override
    public String getIconPathKey() {
        return ICON_PATH;
    }

    @Override
    public String getLabelStateKey() {
        return LABEL;
    }

}
