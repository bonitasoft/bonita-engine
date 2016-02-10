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
package org.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.builder.SFlowNodeInstanceBuilderFactory;

/**
 * @author Baptiste Mesta
 */
public abstract class SFlowNodeInstanceBuilderFactoryImpl extends SFlowElementInstanceBuilderFactoryImpl implements SFlowNodeInstanceBuilderFactory {

    protected static final String DISPLAY_DESCRIPTION = "displayDescription";

    protected static final String DISPLAY_NAME = "displayName";

    protected static final String STATE_ID_KEY = "stateId";

    protected static final String STATE_NAME_KEY = "stateName";

    protected static final String PREVIOUS_STATE_ID_KEY = "previousStateId";

    protected static final String LAST_UPDATE_KEY = "lastUpdateDate";

    protected static final String REACHED_STATE_DATE_KEY = "reachedStateDate";

    protected static final String EXECUTE_BY_KEY = "executedBy";

    protected static final String EXECUTE_FOR_KEY = "executedBySubstitute";

    protected static final String STATE_EXECUTING_KEY = "stateExecuting";


    @Override
    public String getDisplayDescriptionKey() {
        return DISPLAY_DESCRIPTION;
    }

    @Override
    public String getDisplayNameKey() {
        return DISPLAY_NAME;
    }

    @Override
    public String getStateExecutingKey() {
        return STATE_EXECUTING_KEY;
    }

    @Override
    public String getExecutedBy() {
        return EXECUTE_BY_KEY;
    }

    @Override
    public String getExecutedBySubstitute() {
        return EXECUTE_FOR_KEY;
    }

    @Override
    public String getStateIdKey() {
        return STATE_ID_KEY;
    }

    @Override
    public String getStateNameKey() {
        return STATE_NAME_KEY;
    }

    @Override
    public String getPreviousStateIdKey() {
        return PREVIOUS_STATE_ID_KEY;
    }

    @Override
    public String getLastUpdateDateKey() {
        return LAST_UPDATE_KEY;
    }

    @Override
    public String getReachStateDateKey() {
        return REACHED_STATE_DATE_KEY;
    }
}
