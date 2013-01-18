/*
 * Copyright (C) 2012-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.builder.SIndexedLogBuilder;
import org.bonitasoft.engine.search.FieldDescriptor;
import org.bonitasoft.engine.search.LogSearchDescriptor;
import org.bonitasoft.engine.search.SearchEntityDescriptor;

/**
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 */
public class SearchLogDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> searchEntityKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> logAllFields;

    public SearchLogDescriptor(final SIndexedLogBuilder sIndexedLogBuilder) {
        searchEntityKeys = new HashMap<String, FieldDescriptor>(5);
        searchEntityKeys.put(LogSearchDescriptor.ACTION_SCOPE, new FieldDescriptor(SQueriableLog.class, sIndexedLogBuilder.getActionScopeKey()));
        searchEntityKeys.put(LogSearchDescriptor.ACTION_TYPE, new FieldDescriptor(SQueriableLog.class, sIndexedLogBuilder.getActionTypeKey()));
        searchEntityKeys.put(LogSearchDescriptor.CREATED_BY, new FieldDescriptor(SQueriableLog.class, sIndexedLogBuilder.getUserIdKey()));
        searchEntityKeys.put(LogSearchDescriptor.MESSAGE, new FieldDescriptor(SQueriableLog.class, sIndexedLogBuilder.getRawMessageKey()));
        searchEntityKeys.put(LogSearchDescriptor.SEVERITY, new FieldDescriptor(SQueriableLog.class, sIndexedLogBuilder.getSeverityKey()));

        logAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(2);
        final Set<String> logFields = new HashSet<String>();
        logFields.add(sIndexedLogBuilder.getUserIdKey());
        logFields.add(sIndexedLogBuilder.getClusterNodeKey());
        logFields.add(sIndexedLogBuilder.getProductVersionKey());
        logFields.add(sIndexedLogBuilder.getActionTypeKey());
        logFields.add(sIndexedLogBuilder.getActionScopeKey());
        logFields.add(sIndexedLogBuilder.getRawMessageKey());
        logFields.add(sIndexedLogBuilder.getCallerClassNameKey());
        logFields.add(sIndexedLogBuilder.getCallerMethodNameKey());
        logFields.add(sIndexedLogBuilder.getDayOfWeekKey());
        logAllFields.put(SQueriableLog.class, logFields);
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
