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
package org.bonitasoft.engine.bpm.connector;

/**
 * The fields on which a search can be made for the archived connectors.
 * 
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public final class ArchiveConnectorInstancesSearchDescriptor {

    /**
     * The name of the field corresponding to the name of the connector
     */
    public static final String NAME = "name";

    /**
     * The name of the field corresponding to the state of the connector
     */
    public static final String STATE = "state";

    /**
     * The name of the field corresponding to the event to activate the connector
     */
    public static final String ACTIVATION_EVENT = "activationEvent";

    /**
     * The name of the field corresponding to the type of the container of the connector
     */
    public static final String CONTAINER_TYPE = "containerType";

    /**
     * The name of the field corresponding to the identifier of the container of the connector
     */
    public static final String CONTAINER_ID = "containerId";

    /**
     * The name of the field corresponding to the identifier of the definition of the connector
     */
    public static final String CONNECTOR_DEFINITION_ID = "connectorDefinitionId";

    /**
     * The name of the field corresponding to the version of the definition of the connector
     */
    public static final String CONNECTOR_DEFINITION_VERSION = "connectorDefinitionVersion";

    /**
     * The name of the field corresponding to the date to which the connector was archived.
     */
    public static final String ARCHIVE_DATE = "archiveDate";

    /**
     * The name of the field corresponding to the identifier of the connector (not archived)
     */
    public static final String SOURCE_OBJECT_ID = "sourceObjectId";

}
