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
package org.bonitasoft.engine.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * This object represents the status of the import of an entity
 * e.g. for an import of profile this object can be ImportStatus [name=MyProfile, status=ADDED, errors=[]]
 *
 * @author Baptiste Mesta
 * @since 6.3.1
 */
@Data
public class ImportStatus implements Serializable {

    public enum Status {
        ADDED, REPLACED, SKIPPED
    }

    private final String name;

    private Status status = Status.ADDED;

    private List<ImportError> errors = new ArrayList<>();

    public void addError(final ImportError error) {
        errors.add(error);
    }

    public void addErrors(final List<ImportError> errors) {
        this.errors.addAll(errors);
    }

    public void addErrorsIfNotExists(final List<ImportError> errors) {
        for (ImportError importError : errors) {
            if (importError != null && !getErrors().contains(importError)) {
                addError(importError);
            }
        }
    }
}
