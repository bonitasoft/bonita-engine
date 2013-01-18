/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.log.api.impl;

import java.util.Map;

import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.services.QueriableLoggerStrategy;

/**
 * @author Matthieu Chaffotte
 */
public class MapQueriableLoggerStrategy implements QueriableLoggerStrategy {

    private final Map<String, Boolean> loggableLevels;

    public MapQueriableLoggerStrategy(final Map<String, Boolean> loggableLevels) {
        this.loggableLevels = loggableLevels;
    }

    @Override
    public boolean isLoggable(final String actionType, final SQueriableLogSeverity severity) {
        if (System.getProperty("org.bonitasoft.engine.services.queryablelog.disable") != null) {
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
