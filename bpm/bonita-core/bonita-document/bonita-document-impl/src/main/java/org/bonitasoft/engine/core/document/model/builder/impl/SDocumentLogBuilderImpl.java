/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.core.document.model.builder.impl;

import org.bonitasoft.engine.core.document.model.builder.SDocumentLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilder;

/**
 * @author Emmanuel Duchastenier
 * @author Nicolas Chabanoles
 */
public class SDocumentLogBuilderImpl extends CRUDELogBuilder implements SDocumentLogBuilder {

    private static final String PREFIX = "DOCUMENTMAPPING";

    @Override
    public SPersistenceLogBuilder objectId(final long objectId) {
        this.queriableLogBuilder.numericIndex(SDocumentMappingLogIndexesMapper.DOCUMENTMAPPING_INDEX, objectId);
        return this;
    }

    @Override
    protected String getActionTypePrefix() {
        return PREFIX;
    }

    @Override
    protected void checkExtraRules(final SQueriableLog log) {
    }

    @Override
    public void setProcessInstanceId(final long procInstanceId) {
        this.queriableLogBuilder.numericIndex(SDocumentMappingLogIndexesMapper.DOCUMENTMAPPING_INDEX_PROC_INSTANCE_ID, procInstanceId);
    }

}
