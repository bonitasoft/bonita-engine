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
package org.bonitasoft.engine.scheduler.model.impl;

import org.bonitasoft.engine.scheduler.model.SJobLog;

/**
 * @author Celine Souchet
 */
public class SJobLogImpl extends SPersistentObjectImpl implements SJobLog {

    private static final long serialVersionUID = 3170527651672434929L;

    private long jobDescriptorId;

    private Long retryNumber = 0L;

    private Long lastUpdateDate;

    private String lastMessage;

    public SJobLogImpl() {
        super();
    }

    public SJobLogImpl(final long jobDescriptorId) {
        super();
        this.jobDescriptorId = jobDescriptorId;
    }

    @Override
    public String getDiscriminator() {
        return SJobLogImpl.class.getName();
    }

    @Override
    public long getJobDescriptorId() {
        return jobDescriptorId;
    }

    public void setJobDescriptorId(final long jobDescriptorId) {
        this.jobDescriptorId = jobDescriptorId;
    }

    @Override
    public Long getRetryNumber() {
        return retryNumber;
    }

    public void setRetryNumber(final Long retryNumber) {
        this.retryNumber = retryNumber;
    }

    @Override
    public Long getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(final Long lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(final String lastMessage) {
        this.lastMessage = lastMessage;
    }

}
