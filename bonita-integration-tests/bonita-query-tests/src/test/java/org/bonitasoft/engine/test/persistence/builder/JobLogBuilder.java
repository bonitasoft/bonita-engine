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
package org.bonitasoft.engine.test.persistence.builder;

import org.bonitasoft.engine.scheduler.model.impl.SJobLogImpl;

/**
 * @author Emmanuel Duchastenier
 */
public class JobLogBuilder extends PersistentObjectBuilder<SJobLogImpl, JobLogBuilder> {

    long jobDescriptorId;

    public static JobLogBuilder aJobLog() {
        return new JobLogBuilder();
    }

    @Override
    JobLogBuilder getThisBuilder() {
        return this;
    }

    @Override
    SJobLogImpl _build() {
        // lastUpdateDate must not be null for the SFailedJobImpl constructor to be called normally:
        SJobLogImpl sJobLogImpl = new SJobLogImpl(jobDescriptorId);
        sJobLogImpl.setLastUpdateDate(System.currentTimeMillis());
        return sJobLogImpl;
    }

    public JobLogBuilder withJobDescriptorId(final long jobDescriptorId) {
        this.jobDescriptorId = jobDescriptorId;
        return this;
    }

}
