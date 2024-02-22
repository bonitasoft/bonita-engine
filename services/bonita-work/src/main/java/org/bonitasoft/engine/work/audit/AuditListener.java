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
package org.bonitasoft.engine.work.audit;

import org.bonitasoft.engine.work.WorkDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Default implementation backed to a slf4j logger.
 */
@Component
public class AuditListener {

    private static final Logger WORK_AUDIT = LoggerFactory.getLogger("BONITA_WORK_AUDIT.EXECUTION");

    public void detectionStarted(WorkDescriptor work) {
        WORK_AUDIT.debug("Start detection for execution #{} of {}", work.getExecutionCount(), work);
    }

    public void abnormalExecutionStatusDetected(WorkDescriptor work, ExecutionStatus executionStatus) {
        WORK_AUDIT.warn("Potential abnormal execution detected - cause {}. {}", executionStatus, work);
    }

    public void success(WorkDescriptor work) {
        WORK_AUDIT.info("Work successfully executed. {}", work);
    }

}
