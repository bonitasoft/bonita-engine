/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.data.instance;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.data.DataSourceConfiguration;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Elias Ricken de Medeiros
 */
public class PersistentDataInstanceDataSourceConfiguration implements DataSourceConfiguration {

    public static final String RECORDER_KEY = "recorder";

    public static final String PERSISTENCE_READ_KEY = "persistenceRead";

    public static final String QUERIABLE_LOGGER_SERVICE = "queriableLoggerService";

    private final Map<String, Object> resources;

    public PersistentDataInstanceDataSourceConfiguration(final ReadPersistenceService persistenceRead, final Recorder recorder,
            final QueriableLoggerService queriableLoggerService) {
        resources = new HashMap<String, Object>();
        resources.put(PERSISTENCE_READ_KEY, persistenceRead);
        resources.put(RECORDER_KEY, recorder);
        resources.put(QUERIABLE_LOGGER_SERVICE, queriableLoggerService);
    }

    @Override
    public Map<String, Object> getResources() {
        return resources;
    }

}
