/**
 * Copyright (C) 2013-2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.test.wait;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.test.WaitUntil;

/**
 * @author Sébastien Chevassu
 * @author Celine Souchet
 */
public class WaitForVariableValue extends WaitUntil {

    private final ProcessAPI processAPI;

    private final long processInstanceId;

    private final String variableName;

    private final String valueExpected;

    public WaitForVariableValue(final ProcessAPI processAPI, final long processInstanceId, final String variableName, final String valueExpected) {
        super(500, 7 * 60 * 1000, false);
        this.processAPI = processAPI;
        this.processInstanceId = processInstanceId;
        this.variableName = variableName;
        this.valueExpected = valueExpected;
    }

    @Override
    protected boolean check() throws Exception {
        final String value = (String) processAPI.getProcessDataInstance(variableName, processInstanceId).getValue();
        return value.equals(valueExpected);
    }

}
