/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.log.api.impl;

import java.util.Map;

import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.services.QueriableLoggerStrategy;

/**
 * @author Matthieu Chaffotte
 */
public class MapQueriableLoggerStrategy implements QueriableLoggerStrategy {

    private final boolean disable = System.getProperty("org.bonitasoft.engine.services.queryablelog.disable") != null;

    private final Map<String, Boolean> loggableLevels;

    public MapQueriableLoggerStrategy(final Map<String, Boolean> loggableLevels) {
        this.loggableLevels = loggableLevels;
    }

    @Override
    public boolean isLoggable(final String actionType, final SQueriableLogSeverity severity) {
        if (disable) {
            return false;
        }

        final String actionSeverity = actionType + ":" + severity;
        final Boolean isLoggable = loggableLevels.get(actionSeverity);
        if (isLoggable == null) {
            throw new RuntimeException("The action type '" + actionType + "' with the severity '" + severity.name() + "' is not known as loggable.");
        }
        return isLoggable;
    }

}
