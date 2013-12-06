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
package org.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.builder.SConnectorInstanceLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.MissingMandatoryFieldsException;

/**
 * @author Baptiste Mesta
 */
public class SConnectorInstanceLogBuilderImpl extends CRUDELogBuilder implements SConnectorInstanceLogBuilder {

    private static final String CONNECTOR_INSTANCE = "CONNECTOR_INSTANCE";

    @Override
    public SPersistenceLogBuilder objectId(final long objectId) {
        queriableLogBuilder.numericIndex(1, objectId);
        return this;
    }

    @Override
    public SConnectorInstanceLogBuilder containerId(final long containerId) {
        queriableLogBuilder.numericIndex(0, containerId);
        return this;
    }

    @Override
    protected String getActionTypePrefix() {
        return CONNECTOR_INSTANCE;
    }

    @Override
    protected void checkExtraRules(final SQueriableLog log) {
        if (log.getActionStatus() != SQueriableLog.STATUS_FAIL) {
            if (log.getNumericIndex(1) == 0L) {
                throw new MissingMandatoryFieldsException("Some mandatory fields are missing: " + "Connector Instance Id");
            }
        }
        if (log.getNumericIndex(0) == 0L) {
            throw new MissingMandatoryFieldsException("Some mandatory fields are missing: " + "Container Id");
        }
    }

}
