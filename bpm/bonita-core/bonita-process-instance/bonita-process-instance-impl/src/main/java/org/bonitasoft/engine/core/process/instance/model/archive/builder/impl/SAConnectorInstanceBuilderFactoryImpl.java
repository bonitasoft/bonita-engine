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
package org.bonitasoft.engine.core.process.instance.model.archive.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAConnectorInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAConnectorInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.SAConnectorInstanceImpl;

/**
 * @author Elias Ricken de Medeiros
 */
public class SAConnectorInstanceBuilderFactoryImpl extends SANamedElementBuilderFactoryImpl implements SAConnectorInstanceBuilderFactory {

    private static final String CONTAINER_ID_KEY = "containerId";

    private static final String CONTAINER_TYPE_KEY = "containerType";

    private static final String CONNECTOR_ID_KEY = "connectorId";

    private static final String VERSION_KEY = "version";

    private static final String ACTIVATION_EVENT_KEY = "activationEvent";

    private static final String STATE_KEY = "state";

    @Override
    public SAConnectorInstanceBuilder createNewArchivedConnectorInstance(final SConnectorInstance connectorInstance) {
        return new SAConnectorInstanceBuilderImpl(new SAConnectorInstanceImpl(connectorInstance));
    }

    @Override
    public String getContainerIdKey() {
        return CONTAINER_ID_KEY;
    }

    @Override
    public String getContainerTypeKey() {
        return CONTAINER_TYPE_KEY;
    }

    @Override
    public String getConnectorIdKey() {
        return CONNECTOR_ID_KEY;
    }

    @Override
    public String getVersionKey() {
        return VERSION_KEY;
    }

    @Override
    public String getActivationEventKey() {
        return ACTIVATION_EVENT_KEY;
    }

    @Override
    public String getStateKey() {
        return STATE_KEY;
    }
}
