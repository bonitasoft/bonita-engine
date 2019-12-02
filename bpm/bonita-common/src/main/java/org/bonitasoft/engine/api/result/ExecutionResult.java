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
package org.bonitasoft.engine.api.result;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bonitasoft.engine.api.result.Status.Level;

/**
 * Contains a list of {@link Status} as a result of an API execution
 */
public class ExecutionResult implements Serializable {

    public static final ExecutionResult OK = new ExecutionResult();
    private List<Status> statuses = new ArrayList<>();

    public ExecutionResult(Status... statuses) {
        this(asList(requireNonNull(statuses)));
    }

    public ExecutionResult(List<Status> statusList) {
        this.statuses.addAll(requireNonNull(statusList));
    }

    public void addStatus(Status... statuses) {
        this.statuses.addAll(asList(requireNonNull(statuses)));
    }

    public boolean isOk() {
        return statuses.isEmpty() || statuses.stream()
                .allMatch(status -> status.getLevel() == Level.OK);
    }

    public boolean hasErrors() {
        return statuses.stream()
                .anyMatch(status -> status.getLevel() == Level.ERROR);
    }

    public boolean hasWarnings() {
        return statuses.stream()
                .anyMatch(status -> status.getLevel() == Level.WARNING);
    }

    public boolean hasInfo() {
        return statuses.stream()
                .anyMatch(status -> status.getLevel() == Level.INFO);
    }

    public List<Status> getErrors() {
        return statuses.stream()
                .filter(status -> status.getLevel() == Level.ERROR)
                .collect(Collectors.toList());
    }

    public List<Status> getWarnings() {
        return statuses.stream()
                .filter(status -> status.getLevel() == Level.WARNING)
                .collect(Collectors.toList());
    }

    public List<Status> getInfo() {
        return statuses.stream()
                .filter(status -> status.getLevel() == Level.INFO)
                .collect(Collectors.toList());
    }

    public List<Status> getAllStatus() {
        return Collections.unmodifiableList(statuses);
    }
}
