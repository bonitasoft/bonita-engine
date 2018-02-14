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
package org.bonitasoft.engine.core.process.instance.model.archive.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAFlowNodeInstanceBuilderFactory;

/**
 * @author Elias Ricken de Medeiros
 */
public abstract class SAFlowNodeInstanceBuilderFactoryImpl extends SAFlowElementInstanceBuilderFactoryImpl implements SAFlowNodeInstanceBuilderFactory {

    protected static final String STATE_ID_KEY = "stateId";

    protected static final String STATE_NAME_KEY = "stateName";

    protected static final String REACHED_STATE_DATE_KEY = "reachedStateDate";

    protected static final String LAST_UPDATE_KEY = "lastUpdateDate";

    protected static final String DISPLAY_NAME_KEY = "displayName";

    protected static final String TERMINAL_KEY = "terminal";

    @Override
    public String getStateIdKey() {
        return STATE_ID_KEY;
    }

    @Override
    public String getStateNameKey() {
        return STATE_NAME_KEY;
    }

    @Override
    public String getReachedStateDateKey() {
        return REACHED_STATE_DATE_KEY;
    }

    @Override
    public String getLastUpdateKey() {
        return LAST_UPDATE_KEY;
    }

    @Override
    public String getDisplayNameKey() {
        return DISPLAY_NAME_KEY;
    }

    @Override
    public String getTerminalKey() {
        return TERMINAL_KEY;
    }

}
