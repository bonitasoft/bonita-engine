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

import java.time.Duration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class ExecutionMonitor {

    long finishing;
    long executing;
    long notExecutable;
    long notFound;
    long inError;
    private final long startTime;
    private final int numberOfElementsToProcess;

    public ExecutionMonitor(int numberOfElementsToProcess) {
        this.numberOfElementsToProcess = numberOfElementsToProcess;
        startTime = System.currentTimeMillis();
    }

    public void printProgress() {
        log.info("Restarting elements...Handled "
                + (finishing + executing + notExecutable + notFound + inError) + " of "
                + numberOfElementsToProcess +
                " elements to be restarted in " + Duration.ofMillis(System.currentTimeMillis() - startTime));
    }

    public void printSummary() {
        log.info("Restart of elements completed.");
        log.info("Handled {} elements to be restarted in {}",
                (finishing + executing + notExecutable + notFound + inError),
                Duration.ofMillis(System.currentTimeMillis() - startTime));
        log.info("Found {} elements to be executed", executing);
        log.info("Found {} elements to be completed", finishing);
        log.info("Found {} elements that were not executable (e.g. unmerged gateway)", notExecutable);
        if (notFound > 0) {
            log.info(notFound + " elements were not found (might have been manually executed)");
        }
        if (inError > 0) {
            log.info("Found {} elements in error (see stacktrace for reason)", inError);
        }
    }
}
