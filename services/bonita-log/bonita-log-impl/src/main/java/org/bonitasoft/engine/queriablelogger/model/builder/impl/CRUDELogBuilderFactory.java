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
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEActionFactory;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilderFactory;

/**
 * @author Elias Ricken de Medeiros
 * @author Nicolas Chabanoles
 * @author Matthieu Chaffotte
 */
public abstract class CRUDELogBuilderFactory implements HasCRUDEActionFactory, SLogBuilderFactory {
    
    private SQueriableLogBuilderFactoryImpl sIndexedLogBuilderFactory;

    public CRUDELogBuilderFactory() {
        sIndexedLogBuilderFactory = new SQueriableLogBuilderFactoryImpl();
    }
    
    @Override
    public SLogBuilder createNewInstance() {
        return new SQueriableLogBuilderImpl();
    }

    @Override
    public SLogBuilder fromInstance(final SQueriableLog log) {
        return new SQueriableLogBuilderImpl(log);
    }

    @Override
    public String getTimeStampKey() {
        return sIndexedLogBuilderFactory.getTimeStampKey();
    }

    @Override
    public String getYearKey() {
        return sIndexedLogBuilderFactory.getYearKey();
    }

    @Override
    public String getMonthKey() {
        return sIndexedLogBuilderFactory.getMonthKey();
    }

    @Override
    public String getDayOfYearKey() {
        return sIndexedLogBuilderFactory.getDayOfYearKey();
    }

    @Override
    public String getWeekOfYearKey() {
        return sIndexedLogBuilderFactory.getWeekOfYearKey();
    }

    @Override
    public String getUserIdKey() {
        return sIndexedLogBuilderFactory.getUserIdKey();
    }

    @Override
    public String getThreadNumberKey() {
        return sIndexedLogBuilderFactory.getThreadNumberKey();
    }

    @Override
    public String getClusterNodeKey() {
        return sIndexedLogBuilderFactory.getClusterNodeKey();
    }

    @Override
    public String getProductVersionKey() {
        return sIndexedLogBuilderFactory.getProductVersionKey();
    }

    @Override
    public String getSeverityKey() {
        return sIndexedLogBuilderFactory.getSeverityKey();
    }

    @Override
    public String getActionScopeKey() {
        return sIndexedLogBuilderFactory.getActionScopeKey();
    }

    @Override
    public String getActionStatusKey() {
        return sIndexedLogBuilderFactory.getActionStatusKey();
    }

    @Override
    public String getRawMessageKey() {
        return sIndexedLogBuilderFactory.getRawMessageKey();
    }

    @Override
    public String getCallerClassNameKey() {
        return sIndexedLogBuilderFactory.getCallerClassNameKey();
    }

    @Override
    public String getCallerMethodNameKey() {
        return sIndexedLogBuilderFactory.getCallerMethodNameKey();
    }

    @Override
    public String getActionTypeKey() {
        return sIndexedLogBuilderFactory.getActionTypeKey();
    }

    @Override
    public Class<? extends SQueriableLog> getModelClass() {
        return sIndexedLogBuilderFactory.getModelClass();
    }

}
