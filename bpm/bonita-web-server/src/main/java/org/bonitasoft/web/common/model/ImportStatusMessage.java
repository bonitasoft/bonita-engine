/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Fabio Lombardi
 */
public class ImportStatusMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<String, List<String>> errors;

    private String name;

    private final String statusType;

    public ImportStatusMessage(final String name, final String statusType) {
        errors = new HashMap<>();
        this.name = name;
        this.statusType = statusType;
    }

    public void addError(final String elementType, final String errorMessage) {
        if (!errors.containsKey(elementType)) {
            errors.put(elementType, new ArrayList<>());
        }
        errors.get(elementType).add(errorMessage);
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }

    public void setErrors(final Map<String, List<String>> errors) {
        this.errors = errors;
    }

    public String getName() {
        return name;
    }

    public String getStatusType() {
        return statusType;
    }
}
