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
package org.bonitasoft.engine.test.wait;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.test.WaitUntil;

/**
 * @author Sébastien Chevassu
 * @author Celine Souchet
 */
public class WaitForDataValue extends WaitUntil {

    private final ProcessAPI processAPI;

    private final long processInstanceId;

    private final String dataName;

    private final String valueExpected;

    public WaitForDataValue(final int repeatEach, final int timeout, final long processInstanceId, final String dataName,
            final String valueExpected, final ProcessAPI processAPI) {
        super(repeatEach, timeout, false);
        this.processAPI = processAPI;
        this.processInstanceId = processInstanceId;
        this.dataName = dataName;
        this.valueExpected = valueExpected;
    }

    @Override
    protected boolean check() throws Exception {
        final String value = (String) processAPI.getProcessDataInstance(dataName, processInstanceId).getValue();
        return value.equals(valueExpected);
    }

}
