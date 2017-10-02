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
package org.bonitasoft.engine.queriablelogger.model.builder.impl;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.SQueriableLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.impl.SQueriableLogImpl;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class SQueriableLogBuilderImpl implements SQueriableLogBuilder {

    private final SQueriableLogImpl entity;

    public SQueriableLogBuilderImpl() {
        this.entity = new SQueriableLogImpl();
    }
    
    public SQueriableLogBuilderImpl(final SQueriableLogImpl entity) {
        this.entity = entity;
    }
    
    public SQueriableLogBuilderImpl(final SQueriableLog log) {
        // FIXME cannot be the same object. This method can be removed when all SQueriableLog was replaced
        // by SQueriableLogBuilder where it's necessary
        SQueriableLogImpl local = null;
        if (log instanceof SQueriableLogImpl) {
            local = (SQueriableLogImpl) log;
        }
        if (local == null) {
            local = new SQueriableLogImpl();
        }
        this.entity = local;
    }
    
    @Override
    public SQueriableLogBuilder userId(final String userId) {
        entity.setUserId(userId);
        return this;
    }

    @Override
    public SQueriableLogBuilder clusterNode(final String clusterNode) {
        entity.setClusterNode(clusterNode);
        return this;
    }

    @Override
    public SQueriableLogBuilder productVersion(final String productVersion) {
        entity.setProductVersion(productVersion);
        return this;
    }

    @Override
    public SQueriableLogBuilder severity(final SQueriableLogSeverity severity) {
        entity.setSeverity(severity);
        return this;
    }

    @Override
    public SQueriableLogBuilder actionType(final String actionType) {
        entity.setActionType(actionType);
        return this;
    }

    @Override
    public SQueriableLogBuilder actionScope(final String scope) {
        entity.setActionScope(scope);
        return this;
    }

    @Override
    public SQueriableLogBuilder actionStatus(final int status) {
        entity.setActionStatus(status);
        return this;
    }

    @Override
    public SQueriableLogBuilder rawMessage(final String rawMessage) {
        entity.setRawMessage(rawMessage);
        return this;
    }

    @Override
    public SQueriableLogBuilder callerClassName(final String callerClassName) {
        entity.setCallerClassName(callerClassName);
        return this;
    }

    @Override
    public SQueriableLogBuilder callerMethodName(final String callerMethodName) {
        entity.setCallerMethodName(callerMethodName);
        return this;
    }

    @Override
    public SQueriableLogBuilder numericIndex(final int pos, final long value) {
        entity.setNumericIndex(pos, value);
        return this;
    }

    @Override
    public SQueriableLog done() {
        final List<String> problems = checkMandatoryFields();
        if (problems.size() > 0) {
            throw new MissingMandatoryFieldsException("Some mandatoryFildes are missing: " + problems);
        }
        return entity;
    }

    private List<String> checkMandatoryFields() {
        final List<String> problems = new ArrayList<String>();
        if (entity.getSeverity() == null) {
            problems.add("severity");
        }
        if (entity.getActionType() == null) {
            problems.add("actionType");
        }
        if (SQueriableLog.STATUS_FAIL != entity.getActionStatus() && entity.getActionScope() == null) {
            problems.add("actionScope");
        }
        if (entity.getActionStatus() != 0 && entity.getActionStatus() != 1) {
            problems.add("actionStatus (must be 0, for fail or 1, for ok)");
        }
        if (entity.getRawMessage() == null) {
            problems.add("rawMessage");
        }
        return problems;
    }

}
