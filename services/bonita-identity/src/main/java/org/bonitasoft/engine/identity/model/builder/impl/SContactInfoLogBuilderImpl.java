/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.identity.model.builder.impl;

import org.bonitasoft.engine.identity.model.builder.SContactInfoLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.MissingMandatoryFieldsException;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class SContactInfoLogBuilderImpl extends CRUDELogBuilder implements SContactInfoLogBuilder {

    private static final String PREFIX = "IDENTITY_USER_CONTACT_INFO";

    @Override
    public SPersistenceLogBuilder objectId(final long objectId) {
        queriableLogBuilder.numericIndex(SContactInfoLogBuilderFactoryImpl.USER_CONTACT_INFO_INDEX, objectId);
        return this;
    }

    @Override
    public SPersistenceLogBuilder setContactInfoUserId(final long userId) {
        queriableLogBuilder.numericIndex(SContactInfoLogBuilderFactoryImpl.USER_CONTACT_INFO_USERID_INDEX, userId);
        return this;
    }

    @Override
    protected String getActionTypePrefix() {
        return PREFIX;
    }

    @Override
    protected void checkExtraRules(final SQueriableLog log) {
        if (log.getActionStatus() != SQueriableLog.STATUS_FAIL
                && log.getNumericIndex(SContactInfoLogBuilderFactoryImpl.USER_CONTACT_INFO_USERID_INDEX) == 0L) {
            throw new MissingMandatoryFieldsException(
                    "Some mandatory fields are missing: Identity Contact info User Id");
        }
    }

    public static SContactInfoLogBuilder getInstance() {
        return new SContactInfoLogBuilderImpl();
    }

}
