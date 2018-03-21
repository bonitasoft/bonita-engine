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
package org.bonitasoft.engine.bdm.validator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bonitasoft.engine.api.result.Status;
import org.bonitasoft.engine.api.result.StatusCode;

/**
 * @author Romain Bioteau
 */
public class ValidationStatus {

    private final List<Status> statusList;

    public ValidationStatus() {
        statusList = new ArrayList<>();
    }

    public void addError(StatusCode code, final String error) {
        if (error == null || error.isEmpty()) {
            throw new IllegalArgumentException("error message cannot be null or empty");
        }
        statusList.add(Status.errorStatus(code, error));
    }

    public void addError(StatusCode code, final String error, Map<String, Serializable> context) {
        if (error == null || error.isEmpty()) {
            throw new IllegalArgumentException("error message cannot be null or empty");
        }
        statusList.add(Status.errorStatus(code, error, context));
    }


    public void addWarning(StatusCode code, final String warning) {
        if (warning == null || warning.isEmpty()) {
            throw new IllegalArgumentException("warning message cannot be null or empty");
        }
        statusList.add(Status.warningStatus(code, warning));
    }

    public void addWarning(StatusCode code, final String warning, Map<String, Serializable> context) {
        if (warning == null || warning.isEmpty()) {
            throw new IllegalArgumentException("warning message cannot be null or empty");
        }
        statusList.add(Status.warningStatus(code, warning, context));
    }

    public boolean isOk() {
        return statusList.stream().map(Status::getLevel).noneMatch(Status.Level.ERROR::equals);
    }

    public void addValidationStatus(final ValidationStatus status) {
        statusList.addAll(status.getStatuses());
    }

    /**
     * @Deprecated since release 7.7.0, replaced by {@link #getStatuses()}
     */
    @Deprecated
    public List<String> getErrors() {
        return statusList.stream().filter(status -> Objects.equals(Status.Level.ERROR, status.getLevel()))
                .map(Status::getMessage).collect(Collectors.toList());
    }

    /**
     * @Deprecated since release 7.7.0, replaced by {@link #getStatuses()}
     */
    @Deprecated
    public List<String> getWarnings() {
        return statusList.stream().filter(status -> Objects.equals(Status.Level.WARNING, status.getLevel()))
                .map(Status::getMessage).collect(Collectors.toList());
    }

    public List<Status> getStatuses() {
        return statusList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ValidationStatus))
            return false;
        ValidationStatus that = (ValidationStatus) o;
        return Objects.equals(statusList, that.statusList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(statusList);
    }
}
