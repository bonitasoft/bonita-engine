/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.businesslogger.model.SBusinessLog;
import org.bonitasoft.engine.businesslogger.model.builder.SIndexedLogBuilder;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Zhang Bole
 */
public class SearchLogDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> searchEntityKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> logAllFields;

    SearchLogDescriptor(final SIndexedLogBuilder sIndexedLogBuilder) {
        searchEntityKeys = new HashMap<String, FieldDescriptor>(3);
        searchEntityKeys.put(LogSearchDescriptor.ACTION_SCOPE, new FieldDescriptor(SBusinessLog.class, sIndexedLogBuilder.getActionScopeKey()));
        searchEntityKeys.put(LogSearchDescriptor.ACTION_TYPE, new FieldDescriptor(SBusinessLog.class, sIndexedLogBuilder.getActionTypeKey()));
        searchEntityKeys.put(LogSearchDescriptor.CREATED_BY, new FieldDescriptor(SBusinessLog.class, sIndexedLogBuilder.getUserIdKey()));
        searchEntityKeys.put(LogSearchDescriptor.MESSAGE, new FieldDescriptor(SBusinessLog.class, sIndexedLogBuilder.getRawMessageKey()));
        searchEntityKeys.put(LogSearchDescriptor.SEVERITY, new FieldDescriptor(SBusinessLog.class, sIndexedLogBuilder.getSeverityKey()));

        logAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(2);
        final Set<String> logFields = new HashSet<String>();
        logFields.add(sIndexedLogBuilder.getUserIdKey());
        logFields.add(sIndexedLogBuilder.getClientIpKey());
        logFields.add(sIndexedLogBuilder.getClusterNodeKey());
        logFields.add(sIndexedLogBuilder.getApplicationNameKey());
        logFields.add(sIndexedLogBuilder.getClientApplicationNameKey());
        logFields.add(sIndexedLogBuilder.getDomainKey());
        logFields.add(sIndexedLogBuilder.getProductVersionKey());
        logFields.add(sIndexedLogBuilder.getActionTypeKey());
        logFields.add(sIndexedLogBuilder.getActionScopeKey());
        logFields.add(sIndexedLogBuilder.getRawMessageKey());
        logFields.add(sIndexedLogBuilder.getCallerClassNameKey());
        logFields.add(sIndexedLogBuilder.getCallerMethodNameKey());
        logFields.add(sIndexedLogBuilder.getDayOfWeekKey());
        logAllFields.put(SBusinessLog.class, logFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return searchEntityKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return logAllFields;
    }

}
