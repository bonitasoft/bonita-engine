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
package org.bonitasoft.engine.identity.model.builder.impl;

import org.bonitasoft.engine.identity.model.builder.SUserMembershipLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.MissingMandatoryFieldsException;

/**
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 */
public class SUserMembershipLogBuilderImpl extends CRUDELogBuilder implements SUserMembershipLogBuilder {

    private static final String PREFIX = "IDENTITY_USER_MEMBERSHIP";

    @Override
    public SPersistenceLogBuilder objectId(final long objectId) {
        queriableLogBuilder.numericIndex(SUserMembershipLogBuilderFactoryImpl.USER_MEMBERSHIP_INDEX, objectId);
        return this;
    }

    

    @Override
    protected String getActionTypePrefix() {
        return PREFIX;
    }

    @Override
    protected void checkExtraRules(final SQueriableLog log) {
        if (log.getActionStatus() != SQueriableLog.STATUS_FAIL && log.getNumericIndex(SUserMembershipLogBuilderFactoryImpl.USER_MEMBERSHIP_INDEX) == 0L) {
            throw new MissingMandatoryFieldsException("Some mandatoryFildes are missing: " + "UserMembership Id");
        }

        if (log.getNumericIndex(SUserMembershipLogBuilderFactoryImpl.ROLE_INDEX) == 0L) {
            throw new MissingMandatoryFieldsException("Some mandatoryFildes are missing: " + "Role Id");
        }

        if (log.getNumericIndex(SUserMembershipLogBuilderFactoryImpl.USER_INDEX) == 0L) {
            throw new MissingMandatoryFieldsException("Some mandatoryFildes are missing: " + "User Id");
        }
    }

    @Override
    public SUserMembershipLogBuilder roleID(final long roleId) {
        queriableLogBuilder.numericIndex(SUserMembershipLogBuilderFactoryImpl.ROLE_INDEX, roleId);
        return this;
    }

    @Override
    public SUserMembershipLogBuilder identityUserId(final long userId) {
        queriableLogBuilder.numericIndex(SUserMembershipLogBuilderFactoryImpl.USER_INDEX, userId);
        return this;
    }

    @Override
    public SUserMembershipLogBuilder groupId(final long groupId) {
        queriableLogBuilder.numericIndex(SUserMembershipLogBuilderFactoryImpl.GROUP_INDEX, groupId);
        return this;
    }

}
