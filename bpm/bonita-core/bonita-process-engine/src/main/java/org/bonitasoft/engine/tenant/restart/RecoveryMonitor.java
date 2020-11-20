/**
 * Copyright (C) 2020 Bonitasoft S.A.
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
package org.bonitasoft.engine.tenant.restart;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import java.time.Duration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope(SCOPE_PROTOTYPE)
class RecoveryMonitor {

    private long finishing;
    private long executing;
    private long notExecutable;
    private long notFound;
    private long inError;
    private long startTime;
    private int numberOfElementsToProcess;

    public void startNow(int numberOfElementsToProcess) {
        if (startTime > 0) {
            throw new UnsupportedOperationException("Can't start the Recovery Monitor, it is already started");
        }
        this.numberOfElementsToProcess = numberOfElementsToProcess;
        startTime = System.currentTimeMillis();
    }

    public long getFinishing() {
        return finishing;
    }

    public long getExecuting() {
        return executing;
    }

    public long getNumberOfElementRecovered() {
        return executing + finishing;
    }

    public long getNotExecutable() {
        return notExecutable;
    }

    public long getNotFound() {
        return notFound;
    }

    public long getInError() {
        return inError;
    }

    public void incrementFinishing() {
        this.finishing++;
    }

    public void incrementExecuting() {
        this.executing++;
    }

    public void incrementNotExecutable() {
        this.notExecutable++;
    }

    public void incrementInError() {
        this.inError++;
    }

    public void incrementNotFound() {
        this.notFound++;
    }

    public void incrementNotFound(int add) {
        this.notFound += add;
    }

    public void printProgress() {
        //This will be called only when more than one "page" of element to restart are present
        log.info("Restarting elements...Handled "
                + (getFinishing() + getExecuting() + getNotExecutable() + getNotFound() + getInError()) + " of "
                + numberOfElementsToProcess +
                " elements candidates to be recovered in " + Duration.ofMillis(System.currentTimeMillis() - startTime));
    }

    public void printSummary() {
        // only print a single status line for that
        long numberOfElementRecovered = getNumberOfElementRecovered();
        if (numberOfElementRecovered == 0) {
            log.info("Recovery of elements executed. Nothing detected that needs recovery.");
        } else {
            log.info("Recovery of elements executed, {} elements recovered.", numberOfElementRecovered);
        }
        // details in debug
        log.debug("Handled {} elements candidates to be recovered in {}",
                (getFinishing() + getExecuting() + getNotExecutable() + getNotFound() + getInError()),
                Duration.ofMillis(System.currentTimeMillis() - startTime));
        log.debug("Found {} elements recovered (Executing)", getExecuting());
        log.debug("Found {} elements recovered (Finishing)", getFinishing());
        log.debug("Found {} elements that were not executable (e.g. unmerged gateway)", getNotExecutable());
        log.debug(getNotFound() + " elements were not found (might have been manually executed)");
        log.debug("Found {} elements in error (see stacktrace for reason)", getInError());
    }
}
