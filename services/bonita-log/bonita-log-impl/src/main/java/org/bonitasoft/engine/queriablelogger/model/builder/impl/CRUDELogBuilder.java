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

import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SQueriableLogBuilder;

/**
 * @author Elias Ricken de Medeiros
 * @author Nicolas Chabanoles
 * @author Matthieu Chaffotte
 */
public abstract class CRUDELogBuilder implements HasCRUDEAction, SLogBuilder {

    protected SQueriableLogBuilder queriableLogBuilder;

    private static final String SEPARATOR = "_";

    protected CRUDELogBuilder() {
        queriableLogBuilder = new SQueriableLogBuilderImpl();
    }

    protected abstract String getActionTypePrefix();

    @Override
    public SLogBuilder setActionType(final ActionType actionType) {
        queriableLogBuilder.actionType(getActionTypePrefix() + SEPARATOR + actionType.name());
        return this;
    }

    @Override
    public SLogBuilder userId(final String userId) {
        queriableLogBuilder.userId(userId);
        return this;
    }

    @Override
    public SLogBuilder clusterNode(final String clusterNode) {
        queriableLogBuilder.clusterNode(clusterNode);
        return this;
    }

    @Override
    public SLogBuilder productVersion(final String productVersion) {
        queriableLogBuilder.productVersion(productVersion);
        return this;
    }

    @Override
    public SLogBuilder severity(final SQueriableLogSeverity severity) {
        queriableLogBuilder.severity(severity);
        return this;
    }

    @Override
    public SLogBuilder actionScope(final String scope) {
        queriableLogBuilder.actionScope(scope);
        return this;
    }

    @Override
    public SLogBuilder actionStatus(final int status) {
        queriableLogBuilder.actionStatus(status);
        return this;
    }

    @Override
    public SLogBuilder rawMessage(final String rawMessage) {
        queriableLogBuilder.rawMessage(rawMessage);
        return this;
    }

    @Override
    public SLogBuilder callerClassName(final String callerClassName) {
        queriableLogBuilder.callerClassName(callerClassName);
        return this;
    }

    @Override
    public SLogBuilder callerMethodName(final String callerMethodName) {
        queriableLogBuilder.callerMethodName(callerMethodName);
        return this;
    }

    @Override
    public SQueriableLog done() {
        final SQueriableLog log = queriableLogBuilder.done();
        checkExtraRules(log);
        return log;
    }

    protected abstract void checkExtraRules(final SQueriableLog log);
}
