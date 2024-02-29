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
package org.bonitasoft.engine.queriablelogger.model.builder;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.MissingMandatoryFieldsException;

/**
 * @author Elias Ricken de Medeiros
 * @author Nicolas Chabanoles
 */
public class SQueriableLogBuilder implements SLogBuilder {

    private final SQueriableLog.SQueriableLogBuilder builder;

    public SQueriableLogBuilder() {
        this.builder = SQueriableLog.builder().initializeNow();
    }

    @Override
    public SQueriableLogBuilder userId(final String userId) {
        builder.userId(userId);
        return this;
    }

    @Override
    public SQueriableLogBuilder clusterNode(final String clusterNode) {
        builder.clusterNode(clusterNode);
        return this;
    }

    @Override
    public SQueriableLogBuilder productVersion(final String productVersion) {
        builder.productVersion(productVersion);
        return this;
    }

    @Override
    public SQueriableLogBuilder severity(final SQueriableLogSeverity severity) {
        builder.severity(severity);
        return this;
    }

    public SQueriableLogBuilder actionType(final String actionType) {
        builder.actionType(actionType);
        return this;
    }

    @Override
    public SQueriableLogBuilder actionScope(final String scope) {
        builder.actionScope(scope);
        return this;
    }

    @Override
    public SQueriableLogBuilder actionStatus(final int status) {
        builder.actionStatus(status);
        return this;
    }

    @Override
    public SQueriableLogBuilder rawMessage(final String rawMessage) {
        builder.rawMessage(rawMessage);
        return this;
    }

    @Override
    public SQueriableLogBuilder callerClassName(final String callerClassName) {
        builder.callerClassName(callerClassName);
        return this;
    }

    @Override
    public SQueriableLogBuilder callerMethodName(final String callerMethodName) {
        builder.callerMethodName(callerMethodName);
        return this;
    }

    public SQueriableLogBuilder numericIndex(final int pos, final long value) {
        switch (pos) {
            case 0:
                builder.numericIndex1(value);
                break;
            case 1:
                builder.numericIndex2(value);
                break;
            case 2:
                builder.numericIndex3(value);
                break;
            case 3:
                builder.numericIndex4(value);
                break;
            case 4:
                builder.numericIndex5(value);
                break;
            default:
                throw new IllegalStateException();
        }
        return this;
    }

    @Override
    public SQueriableLog build() {
        final List<String> problems = checkMandatoryFields();
        if (problems.size() > 0) {
            throw new MissingMandatoryFieldsException("Some mandatoryFildes are missing: " + problems);
        }
        return builder.build();
    }

    private List<String> checkMandatoryFields() {
        SQueriableLog entity = builder.build();
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
