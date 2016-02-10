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
import org.bonitasoft.engine.queriablelogger.model.builder.SQueriableLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SQueriableLogBuilderFactory;
import org.bonitasoft.engine.queriablelogger.model.impl.SQueriableLogImpl;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class SQueriableLogBuilderFactoryImpl implements SQueriableLogBuilderFactory {

    private static final String TIME_STAMP = "timeStamp";

    private static final String YEAR = "year";

    private static final String MONTH = "month";

    private static final String DAY_OF_YEAR = "dayOfYear";

    private static final String WEEK_OF_YEAR = "weekOfYear";

    private static final String USERID = "userId";

    private static final String THREAD_NUMBER = "threadNumber";

    private static final String CLUSTER_NODE = "clusterNode";

    private static final String PRODUCT_VERSION = "productVersion";

    private static final String SEVERTITY = "severity";

    private static final String ACTION_TYPE = "actionType";

    private static final String ACTION_SCOPE = "actionScope";

    private static final String ACTION_STATUS = "actionStatus";

    private static final String RAW_MESSAGE = "rawMessage";

    private static final String CALLER_CLASS_NAME = "callerClassName";

    private static final String CALLER_METHOD_NAME = "callerMethodName";

    private static final String NUMERIC_INDEX1 = "numericIndex1";

    private static final String NUMERIC_INDEX2 = "numericIndex2";

    private static final String NUMERIC_INDEX3 = "numericIndex3";

    private static final String NUMERIC_INDEX4 = "numericIndex4";

    private static final String NUMERIC_INDEX5 = "numericIndex5";

    @Override
    public SQueriableLogBuilder createNewInstance() {
        return new SQueriableLogBuilderImpl(new SQueriableLogImpl());
    }

    @Override
    public SQueriableLogBuilder fromInstance(final SQueriableLog log) {
        return new SQueriableLogBuilderImpl(log);
    }

    @Override
    public Class<? extends SQueriableLog> getModelClass() {
        return SQueriableLogImpl.class;
    }

    @Override
    public String getTimeStampKey() {
        return TIME_STAMP;
    }

    @Override
    public String getActionStatusKey() {
        return ACTION_STATUS;
    }

    @Override
    public String getYearKey() {
        return YEAR;
    }

    @Override
    public String getMonthKey() {
        return MONTH;
    }

    @Override
    public String getDayOfYearKey() {
        return DAY_OF_YEAR;
    }

    @Override
    public String getWeekOfYearKey() {
        return WEEK_OF_YEAR;
    }

    @Override
    public String getUserIdKey() {
        return USERID;
    }

    @Override
    public String getThreadNumberKey() {
        return THREAD_NUMBER;
    }

    @Override
    public String getClusterNodeKey() {
        return CLUSTER_NODE;
    }

    @Override
    public String getProductVersionKey() {
        return PRODUCT_VERSION;
    }

    @Override
    public String getSeverityKey() {
        return SEVERTITY;
    }

    @Override
    public String getActionTypeKey() {
        return ACTION_TYPE;
    }

    @Override
    public String getActionScopeKey() {
        return ACTION_SCOPE;
    }

    @Override
    public String getRawMessageKey() {
        return RAW_MESSAGE;
    }

    @Override
    public String getCallerClassNameKey() {
        return CALLER_CLASS_NAME;
    }

    @Override
    public String getCallerMethodNameKey() {
        return CALLER_METHOD_NAME;
    }

    @Override
    public String getNumericIndexKey(final int pos) {
        switch (pos) {
            case 1:
                return NUMERIC_INDEX1;
            case 2:
                return NUMERIC_INDEX2;
            case 3:
                return NUMERIC_INDEX3;
            case 4:
                return NUMERIC_INDEX4;
            case 5:
                return NUMERIC_INDEX5;
            default:
                return null;
        }
    }

}
