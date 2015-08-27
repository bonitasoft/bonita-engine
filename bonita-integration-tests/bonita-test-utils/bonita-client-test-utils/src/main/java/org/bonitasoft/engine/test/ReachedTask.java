/*
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
 */

package org.bonitasoft.engine.test;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author mazourd
 */
public class ReachedTask {


    private long taskId;

    ProcessAPI processAPI;


    public ReachedTask(long taskId,ProcessAPI processAPI) {
        this.taskId = taskId;
        this.processAPI = processAPI;
    }

    public ProcessAPI getProcessAPI() {
        return processAPI;
    }

    public void hasSameNameAs(String name) throws ActivityInstanceNotFoundException {
        assertThat(processAPI.getActivityInstance(taskId).getName()).isEqualTo(name);
    }
    public void hasSize (int size) {
        final List<DataInstance> processDataInstances = getProcessAPI().getActivityDataInstances(taskId, 0, 100);
         assertThat(processDataInstances).hasSize(size);
    }
    public ReachedDataInstance hasOnlyActivityDataInstance(String name) {
        final List<DataInstance> processDataInstances = getProcessAPI().getActivityDataInstances(taskId, 0, 10);

        assertThat(processDataInstances).hasSize(1);
        assertThat(processDataInstances.get(0).getName()).isEqualTo(name);
        return  new ReachedDataInstance(processDataInstances.get(0));
    }


}
