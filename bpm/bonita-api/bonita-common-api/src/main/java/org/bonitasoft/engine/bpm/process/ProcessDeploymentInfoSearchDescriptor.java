/**
 * Copyright (C) 2012-2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.process;

/**
 * @author Zhao Na
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class ProcessDeploymentInfoSearchDescriptor {

    public static final String ID = "id";

    public static final String NAME = "name";

    public static final String VERSION = "version";

    public static final String DEPLOYMENT_DATE = "deploymentDate";

    public static final String DEPLOYED_BY = "deployedBy";

    /**
     * To filter on this field, example :
     * {@code searchOptionsBuilder.filter(ProcessDeploymentInfoSearchDescriptor.ACTIVATION_STATE, ActivationState.ENABLED.name());}
     */
    public static final String ACTIVATION_STATE = "activationState";

    public static final String CONFIGURATION_STATE = "configurationState";

    public static final String PROCESS_ID = "processId";

    public static final String DISPLAY_NAME = "displayName";

    public static final String LAST_UPDATE_DATE = "lastUpdateDate";

    public static final String CATEGORY_ID = "categoryId";

    public static final String LABEL = "label";

}
