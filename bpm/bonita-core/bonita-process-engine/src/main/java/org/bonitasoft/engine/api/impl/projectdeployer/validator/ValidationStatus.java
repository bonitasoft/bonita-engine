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
package org.bonitasoft.engine.api.impl.projectdeployer.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ValidationStatus {

    public final static int OK = 0;
    public final static int INFO = 1;
    public final static int WARNING = 2;
    public final static int ERROR = 3;

    private int severity;
    private String message;
    private List<ValidationStatus> children;

    private ValidationStatus(int severity, String message) {
        this.severity = severity;
        this.message = message;
        this.children = new ArrayList<>();
    }

    public void addChild(ValidationStatus child) {
        this.children.add(child);
    }

    public int getSeverity() {
        return children.stream()
                .map(ValidationStatus::getSeverity)
                .reduce(Integer::max)
                .orElse(severity);
    }

    public List<ValidationStatus> getChildren() {
        return children;
    }

    public static ValidationStatus error(String message) {
        return new ValidationStatus(ERROR, message);
    }

    public static ValidationStatus warning(String message) {
        return new ValidationStatus(WARNING, message);
    }

    public static ValidationStatus info(String message) {
        return new ValidationStatus(INFO, message);
    }

    public static ValidationStatus ok() {
        return new ValidationStatus(OK, "");
    }

    public boolean isOK() {
        return Objects.equals(getSeverity(), OK);
    }

    public String getMessage() {
        return message;
    }

}
