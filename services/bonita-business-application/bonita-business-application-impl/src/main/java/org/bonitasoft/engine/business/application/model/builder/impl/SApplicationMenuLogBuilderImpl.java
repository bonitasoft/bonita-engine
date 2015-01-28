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
package org.bonitasoft.engine.business.application.model.builder.impl;

import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.model.builder.SApplicationMenuLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.MissingMandatoryFieldsException;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SApplicationMenuLogBuilderImpl extends CRUDELogBuilder implements SApplicationMenuLogBuilder {

    @Override
    public SPersistenceLogBuilder objectId(final long objectId) {
        queriableLogBuilder.numericIndex(SApplicationMenuLogBuilderFactoryImpl.APPLICATION_MENU_INDEX, objectId);
        return this;
    }

    @Override
    protected String getActionTypePrefix() {
        return ApplicationService.APPLICATION_MENU;
    }

    @Override
    protected void checkExtraRules(final SQueriableLog log) {
        if (log.getActionStatus() != SQueriableLog.STATUS_FAIL && log.getNumericIndex(SApplicationMenuLogBuilderFactoryImpl.APPLICATION_MENU_INDEX) == 0L) {
            throw new MissingMandatoryFieldsException("Some mandatoryFields are missing: application menu identifier");
        }
    }


}
