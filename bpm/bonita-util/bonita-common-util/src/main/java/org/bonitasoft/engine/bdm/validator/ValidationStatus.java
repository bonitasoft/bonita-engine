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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Romain Bioteau
 */
public class ValidationStatus {

    private final List<String> errorList;

    private final List<String> warningList;

    public ValidationStatus() {
        errorList = new ArrayList<>();
        warningList = new ArrayList<>();
    }

    public void addError(final String error) {
        if (error == null || error.isEmpty()) {
            throw new IllegalArgumentException("error message cannot be null or empty");
        }
        errorList.add(error);
    }

    public void addWarning(final String warning) {
        if (warning == null || warning.isEmpty()) {
            throw new IllegalArgumentException("warning message cannot be null or empty");
        }
        warningList.add(warning);
    }

    public boolean isOk() {
        return errorList.isEmpty();
    }

    public void addValidationStatus(final ValidationStatus status) {
        warningList.addAll(status.getWarnings());
        errorList.addAll(status.getErrors());
    }

    public List<String> getErrors() {
        return errorList;
    }

    public List<String> getWarnings() {
        return warningList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ValidationStatus))
            return false;
        ValidationStatus that = (ValidationStatus) o;
        return Objects.equals(errorList, that.errorList) &&
                Objects.equals(warningList, that.warningList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorList, warningList);
    }
}
