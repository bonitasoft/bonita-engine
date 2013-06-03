/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.SIndexedLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;

/**
 * @author Elias Ricken de Medeiros
 * @author Nicolas Chabanoles
 * @author Matthieu Chaffotte
 */
public abstract class CRUDELogBuilder implements HasCRUDEAction, SLogBuilder {

    protected SIndexedLogBuilder queriableLogBuilder;

    private static final String SEPARATOR = "_";

    public CRUDELogBuilder() {
        queriableLogBuilder = new SIndexedLogBuilderImpl();
    }

    protected abstract String getActionTypePrefix();

    @Override
    public SLogBuilder setActionType(final HasCRUDEAction.ActionType actionType) {
        queriableLogBuilder.actionType(getActionTypePrefix() + SEPARATOR + actionType.name());
        return this;
    }

    @Override
    public SLogBuilder createNewInstance() {
        queriableLogBuilder.createNewInstance();
        return this;
    }

    @Override
    public SLogBuilder fromInstance(final SQueriableLog log) {
        queriableLogBuilder.fromInstance(log);
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
    public String getTimeStampKey() {
        return queriableLogBuilder.getTimeStampKey();
    }

    @Override
    public String getYearKey() {
        return queriableLogBuilder.getYearKey();
    }

    @Override
    public String getMonthKey() {
        return queriableLogBuilder.getMonthKey();
    }

    @Override
    public String getDayOfYearKey() {
        return queriableLogBuilder.getDayOfYearKey();
    }

    @Override
    public String getWeekOfYearKey() {
        return queriableLogBuilder.getWeekOfYearKey();
    }

    @Override
    public String getUserIdKey() {
        return queriableLogBuilder.getUserIdKey();
    }

    @Override
    public String getThreadNumberKey() {
        return queriableLogBuilder.getThreadNumberKey();
    }

    @Override
    public String getClusterNodeKey() {
        return queriableLogBuilder.getClusterNodeKey();
    }

    @Override
    public String getProductVersionKey() {
        return queriableLogBuilder.getProductVersionKey();
    }

    @Override
    public String getSeverityKey() {
        return queriableLogBuilder.getSeverityKey();
    }

    @Override
    public String getActionScopeKey() {
        return queriableLogBuilder.getActionScopeKey();
    }

    @Override
    public String getActionStatusKey() {
        return queriableLogBuilder.getActionStatusKey();
    }

    @Override
    public String getRawMessageKey() {
        return queriableLogBuilder.getRawMessageKey();
    }

    @Override
    public String getCallerClassNameKey() {
        return queriableLogBuilder.getCallerClassNameKey();
    }

    @Override
    public String getCallerMethodNameKey() {
        return queriableLogBuilder.getCallerMethodNameKey();
    }

    @Override
    public SQueriableLog done() {
        final SQueriableLog log = queriableLogBuilder.done();
        checkExtraRules(log);
        return log;
    }

    @Override
    public String getActionTypeKey() {
        return queriableLogBuilder.getActionTypeKey();
    }

    @Override
    public Class<? extends SQueriableLog> getModelClass() {
        return queriableLogBuilder.getModelClass();
    }

    protected abstract void checkExtraRules(final SQueriableLog log);
}
