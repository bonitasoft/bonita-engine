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
package org.bonitasoft.engine.scheduler;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Celine Souchet
 */
public abstract class AbstractBonitaJobListener implements Serializable {

    private static final long serialVersionUID = -8439776331387778894L;

    public static final String BOS_JOB = "bosJob";

    public static final String JOB_DESCRIPTOR_ID = "jobDescriptorId";

    public static final String TENANT_ID = "tenantId";

    public static final String JOB_TYPE = "jobType";

    public static final String JOB_NAME = "jobName";

    public static final String JOB_GROUP = "jobGroup";

    public static final String TRIGGER_NAME = "triggerName";

    public static final String TRIGGER_GROUP = "triggerGroup";

    public static final String TRIGGER_PREVIOUS_FIRE_TIME = "triggerPreviousFireTime";

    public static final String TRIGGER_NEXT_FIRE_TIME = "triggerNextFireTime";

    public static final String REFIRE_COUNT = "refireCount";

    public static final String JOB_DATAS = "jobDatas";

    public static final String JOB_RESULT = "jobResult";

    public abstract String getName();

    public abstract void jobToBeExecuted(Map<String, Serializable> context);

    public abstract void jobExecutionVetoed(Map<String, Serializable> Context);

    public abstract void jobWasExecuted(Map<String, Serializable> context, Exception jobException);

}
