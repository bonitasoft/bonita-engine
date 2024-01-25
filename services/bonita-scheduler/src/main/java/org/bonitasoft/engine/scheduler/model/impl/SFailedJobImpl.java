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
package org.bonitasoft.engine.scheduler.model.impl;

import lombok.Data;
import org.bonitasoft.engine.scheduler.model.SFailedJob;

/**
 * @author Matthieu Chaffotte
 */
@Data
public class SFailedJobImpl implements SFailedJob {

    private final long jobDescriptorId;
    private final String jobName;
    private final String description;
    private final int retryNumber;
    private final long lastUpdateDate;
    private final String lastMessage;

    public SFailedJobImpl(final long jobDescriptorId, final String jobName, final String description, long retryNumber,
            final long lastUpdateDate,
            final String lastMessage) {
        this.jobDescriptorId = jobDescriptorId;
        this.jobName = jobName;
        this.description = description;
        this.retryNumber = (int) retryNumber;
        this.lastUpdateDate = lastUpdateDate;
        this.lastMessage = lastMessage;
    }

    @Override
    public int getNumberOfFailures() {
        // we want the number of failures, not the number of retry
        return retryNumber + 1;
    }
}
