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
package org.bonitasoft.engine.core.category.model.builder.impl;

import org.bonitasoft.engine.core.category.model.builder.SCategoryLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.MissingMandatoryFieldsException;

/**
 * @author Yanyan Liu
 */
public class SCategoryLogBuilderImpl extends CRUDELogBuilder implements SCategoryLogBuilder {

    private static final String PREFIX = "CATEGORY";
    
    public SCategoryLogBuilderImpl() {
        super();
    }
    
    @Override
    protected String getActionTypePrefix() {
        return PREFIX;
    }
    
    @Override
    public SPersistenceLogBuilder objectId(final long objectId) {
        this.queriableLogBuilder.numericIndex(SCategoryLogIndexesMapper.CATEGORY_INDEX, objectId);
        return this;
    }

    @Override
    protected void checkExtraRules(final SQueriableLog log) {
        if (log.getActionStatus() != SQueriableLog.STATUS_FAIL) {
            if (log.getNumericIndex(SCategoryLogIndexesMapper.CATEGORY_INDEX) == 0L) {
                throw new MissingMandatoryFieldsException("Some mandatory fields are missing: " + "Category Id");
            }
        }
    }

}
