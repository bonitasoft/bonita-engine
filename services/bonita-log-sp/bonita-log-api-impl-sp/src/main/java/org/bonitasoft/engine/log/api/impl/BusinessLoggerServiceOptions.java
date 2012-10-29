/**
 * Copyright (C) 2012 BonitaSoft S.A.
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

import java.util.List;

import org.bonitasoft.engine.businesslogger.model.SBusinessLogSeverity;
import org.bonitasoft.engine.services.BusinessLoggerServiceConfiguration;

public class BusinessLoggerServiceOptions implements BusinessLoggerServiceConfiguration {

    private final List<String> loggableLevels;

    public BusinessLoggerServiceOptions(final List<String> loggableLevels) {
        this.loggableLevels = loggableLevels;
    }

    @Override
    public boolean isLoggable(final String actionType, final SBusinessLogSeverity severity) {
        if (System.getProperty("org.bonitasoft.engine.services.queryable.disable") != null) {
            return false;
        }
        if (loggableLevels == null || loggableLevels.isEmpty()) {
            return false;
        }
        return loggableLevels.contains(actionType + ":" + severity);
    }

}
