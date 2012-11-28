/*
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.search;

import java.util.List;

import org.bonitasoft.engine.businesslogger.model.SBusinessLog;
import org.bonitasoft.engine.search.AbstractSearchEntity;
import org.bonitasoft.engine.search.SearchEntityDescriptor;
import org.bonitasoft.engine.search.SearchOptions;

import com.bonitasoft.engine.log.Log;
import com.bonitasoft.engine.service.SPModelConvertor;

/**
 * @author Julien Mege
 */
public abstract class AbstractLogSearchEntity extends AbstractSearchEntity<Log, SBusinessLog> {

    public AbstractLogSearchEntity(final SearchEntityDescriptor searchDescriptor, final SearchOptions options) {
        super(searchDescriptor, options);
    }

    @Override
    public List<Log> convertToClientObjects(final List<SBusinessLog> logs) {
        return SPModelConvertor.toLogs(logs);
    }

}
