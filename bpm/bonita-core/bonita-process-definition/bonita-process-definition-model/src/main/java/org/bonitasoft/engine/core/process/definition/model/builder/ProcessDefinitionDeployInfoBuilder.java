/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.definition.model.builder;

import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;

/**
 * @author Baptiste Mesta
 * @author Yanyan Liu
 * @author Hongwen Zang
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public interface ProcessDefinitionDeployInfoBuilder {

    ProcessDefinitionDeployInfoBuilder createNewInstance(String name, String version);

    ProcessDefinitionDeployInfoBuilder setProcessId(long processId);

    ProcessDefinitionDeployInfoBuilder setDescription(String description);

    ProcessDefinitionDeployInfoBuilder setDeploymentDate(long deploymentDate);

    ProcessDefinitionDeployInfoBuilder setDeployedBy(long deployedBy);

    ProcessDefinitionDeployInfoBuilder setConfigurationState(String configurationState);

    ProcessDefinitionDeployInfoBuilder setActivationState(String activationState);

    ProcessDefinitionDeployInfoBuilder setDisplayName(String displayName);

    ProcessDefinitionDeployInfoBuilder setDisplayDescription(String displayDescription);

    ProcessDefinitionDeployInfoBuilder setLastUpdateDate(long lastUpdateDate);

    ProcessDefinitionDeployInfoBuilder setId(long id);

    ProcessDefinitionDeployInfoBuilder setIconPath(String iconPath);

    SProcessDefinitionDeployInfo done();

    String getNameKey();

    String getVersionKey();

    String getProcessIdKey();

    String getIdKey();

    String getDeploymentDateKey();

    String getDeployedByKey();

    String getDisplayNameKey();

    String getDescriptionKey();

    String getDisplayDescriptionKey();

    String getLastUpdateDateKey();

    String getIconPathKey();

    String getActivationStateKey();

    String getConfigurationStateKey();

    String getLabelStateKey();

}
