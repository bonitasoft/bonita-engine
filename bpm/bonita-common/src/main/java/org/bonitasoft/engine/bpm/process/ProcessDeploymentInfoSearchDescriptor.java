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
package org.bonitasoft.engine.bpm.process;

/**
 * Search descriptors are used to filter / sort results of a generic search. <br>
 * ProcessDeploymentInfoSearchDescriptor defines the fields that can be used as filters or sort fields on <code>List&lt;ProcessDeploymentInfo&gt;</code>
 * returning methods.
 *
 * @author Zhao Na
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 * @see org.bonitasoft.engine.api.ProcessAPI
 * @see ProcessDeploymentInfo
 * @see ProcessDefinition
 * @version 6.3.5
 * @since 6.0.0
 */
public class ProcessDeploymentInfoSearchDescriptor {

    /**
     * The field corresponding to the identifier of the process in the database.
     */
    public static final String ID = "id";

    /**
     * The field corresponding to the name of the process.
     */
    public static final String NAME = "name";

    /**
     * The field corresponding to the version of the process.
     */
    public static final String VERSION = "version";

    /**
     * The field corresponding to the date of the deployment of the process.
     */
    public static final String DEPLOYMENT_DATE = "deploymentDate";

    /**
     * The field corresponding to the identifier of the user who deployed the process.
     */
    public static final String DEPLOYED_BY = "deployedBy";

    /**
     * The field corresponding to the activation state of the process.
     * To filter on this field, example :
     * {@code searchOptionsBuilder.filter(ProcessDeploymentInfoSearchDescriptor.ACTIVATION_STATE, ActivationState.ENABLED.name());}
     */
    public static final String ACTIVATION_STATE = "activationState";

    /**
     * The field corresponding to the configuration state of the process.
     */
    public static final String CONFIGURATION_STATE = "configurationState";

    /**
     * The field corresponding to the identifier of the process definition (in the bonita home).
     */
    public static final String PROCESS_ID = "processId";

    /**
     * The field corresponding to the display name of the process.
     */
    public static final String DISPLAY_NAME = "displayName";

    /**
     * The field corresponding to the last date of the updating of the process.
     */
    public static final String LAST_UPDATE_DATE = "lastUpdateDate";

    /**
     * The field corresponding to the identifier of the category of the process.
     */
    public static final String CATEGORY_ID = "categoryId";

    /**
     * This field doesn't exist on a process definition.
     * 
     * @deprecated since 6.3.5
     */
    @Deprecated
    public static final String LABEL = "label";

}
