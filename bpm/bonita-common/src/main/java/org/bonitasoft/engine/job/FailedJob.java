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
package org.bonitasoft.engine.job;

import java.io.Serializable;
import java.util.Date;

/**
 * Represent failure(s) that happened to a certain job (e.g. Timer execution)
 */
public interface FailedJob extends Serializable {

    long getJobDescriptorId();

    String getJobName();

    String getDescription();

    /**
     * @return the exception thrown by the last failing execution
     */
    String getLastMessage();

    /**
     * @return the number of times a job failed before replaying it manually
     */
    int getNumberOfFailures();

    /**
     * @return the Date of the last failure
     */
    Date getLastUpdateDate();

}
