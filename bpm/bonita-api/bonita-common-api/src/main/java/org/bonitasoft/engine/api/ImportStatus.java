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
package org.bonitasoft.engine.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 
 * This object represents the status of the import of an entity
 * e.g. for an import of profile this object can be ImportStatus [name=MyProfile, status=ADDED, errors=[]]
 * 
 * 
 * 
 * @author Baptiste Mesta
 * 
 * @since 6.3.1
 */
public class ImportStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Status {
        ADDED, REPLACED, SKIPPED
    }

    private final String name;

    private Status status;

    private final List<ImportError> errors = new ArrayList<ImportError>();

    public ImportStatus(final String name) {
        super();
        this.name = name;
        this.status = Status.ADDED;
    }

    public String getName() {
        return name;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public List<ImportError> getErrors() {
        return errors;
    }

    public void addError(final ImportError error) {
        errors.add(error);
    }

    public void addErrors(final List<ImportError> errors) {
        this.errors.addAll(errors);
    }

    @Override
    public String toString() {
        return "ImportStatus [name=" + name + ", status=" + status + ", errors=" + errors + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (errors == null ? 0 : errors.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (status == null ? 0 : status.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ImportStatus other = (ImportStatus) obj;
        if (errors == null) {
            if (other.errors != null) {
                return false;
            }
        } else if (!errors.equals(other.errors)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (status != other.status) {
            return false;
        }
        return true;
    }

}
