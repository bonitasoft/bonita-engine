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
package org.bonitasoft.engine.test.persistence.builder;

import org.bonitasoft.engine.scheduler.model.SJobLog;

/**
 * @author Emmanuel Duchastenier
 */
public class JobLogBuilder extends PersistentObjectBuilder<SJobLog, JobLogBuilder> {

    long jobDescriptorId;

    public static JobLogBuilder aJobLog() {
        return new JobLogBuilder();
    }

    @Override
    JobLogBuilder getThisBuilder() {
        return this;
    }

    @Override
    SJobLog _build() {
        // lastUpdateDate must not be null for the SFailedJobImpl constructor to be called normally:
        SJobLog sJobLog = new SJobLog(jobDescriptorId);
        sJobLog.setLastUpdateDate(System.currentTimeMillis());
        return sJobLog;
    }

    public JobLogBuilder withJobDescriptorId(final long jobDescriptorId) {
        this.jobDescriptorId = jobDescriptorId;
        return this;
    }

}
