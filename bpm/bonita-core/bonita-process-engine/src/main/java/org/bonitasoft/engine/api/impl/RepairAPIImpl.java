/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.api.RepairAPI;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.operation.Operation;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by Vincent Elcrin
 * Date: 11/12/13
 * Time: 09:17
 */
public class RepairAPIImpl implements RepairAPI {

    @Override
    public ProcessInstance startProcess(long startedBy, List<String> activityNames, List<Operation> operations, Map<String, Serializable> context) {
        return null;
    }
}
