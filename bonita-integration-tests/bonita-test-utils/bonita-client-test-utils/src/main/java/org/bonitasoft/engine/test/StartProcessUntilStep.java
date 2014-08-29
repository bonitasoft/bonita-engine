/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.test;

import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstance;

public class StartProcessUntilStep {

    private final ProcessInstance processInstance;

    private final ActivityInstance activityInstance;

    public StartProcessUntilStep(final ProcessInstance processInstance, final ActivityInstance activityInstance) {
        super();
        this.processInstance = processInstance;
        this.activityInstance = activityInstance;
    }

    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    public ActivityInstance getActivityInstance() {
        return activityInstance;
    }

}
