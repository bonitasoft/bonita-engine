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
package org.bonitasoft.engine.job.impl;

import java.util.Date;

import org.bonitasoft.engine.job.FailedJob;

/**
 * @author Matthieu Chaffotte
 */
public class FailedJobImpl implements FailedJob {

    private static final long serialVersionUID = 8739476111495272543L;

    private final long jobDescriptorId;

    private final String jobName;

    private String description;

    private long retryNumber;

    private Date lastUpdateDate;

    private String lastMessage;

    public FailedJobImpl(final long jobDescriptorId, final String jobName) {
        this.jobDescriptorId = jobDescriptorId;
        this.jobName = jobName;
    }

    public void setLastUpdateDate(final Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;

    }

    public void setDescription(final String description) {
        this.description = description;

    }

    public void setLastMessage(final String lastMessage) {
        this.lastMessage = lastMessage;

    }

    public void setRetryNumber(final long retryNumber) {
        this.retryNumber = retryNumber;
    }

    @Override
    public long getJobDescriptorId() {
        return jobDescriptorId;
    }

    @Override
    public String getJobName() {
        return jobName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getLastMessage() {
        return lastMessage;
    }

    @Override
    public long getRetryNumber() {
        return retryNumber;
    }

    @Override
    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

}
