/**
 * Copyright (C) 2026 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.business.data;

import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilder;

/**
 * Builds the {@code DATA_RETENTION_STATE_*} queriable log entries, recording when data retention is
 * enabled or disabled by the engine configuration.
 */
public class SDataRetentionStateLogBuilder extends CRUDELogBuilder {

    public static final String ACTION_TYPE_PREFIX = "DATA_RETENTION_STATE";

    @Override
    protected String getActionTypePrefix() {
        return ACTION_TYPE_PREFIX;
    }

    @Override
    protected void checkExtraRules(final SQueriableLog log) {
        // no rules here
    }
}
