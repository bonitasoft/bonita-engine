/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.scheduler;

import java.io.Serializable;
import java.util.Map;

public interface BonitaJobListener extends Serializable {

    String BOS_JOB = "bosJob";

    String JOB_DESCRIPTOR_ID = "jobDescriptorId";

    String TENANT_ID = "tenantId";

    String JOB_TYPE = "jobType";

    String JOB_NAME = "jobName";

    String JOB_GROUP = "jobGroup";

    String TRIGGER_NAME = "triggerName";

    String TRIGGER_GROUP = "triggerGroup";

    String TRIGGER_PREVIOUS_FIRE_TIME = "triggerPreviousFireTime";

    String TRIGGER_NEXT_FIRE_TIME = "triggerNextFireTime";

    String REFIRE_COUNT = "refireCount";

    String JOB_DATAS = "jobDatas";

    String JOB_RESULT = "jobResult";

    void jobToBeExecuted(Map<String, Serializable> context);

    void jobExecutionVetoed(Map<String, Serializable> Context);

    void jobWasExecuted(Map<String, Serializable> context, Exception jobException);

}
