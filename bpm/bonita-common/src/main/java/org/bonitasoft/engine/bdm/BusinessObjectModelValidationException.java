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
package org.bonitasoft.engine.bdm;

import java.io.Serial;

import org.bonitasoft.engine.api.result.Status;
import org.bonitasoft.engine.api.result.Status.Level;
import org.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Romain Bioteau
 */
public class BusinessObjectModelValidationException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ValidationStatus validationStatus;

    public BusinessObjectModelValidationException(final ValidationStatus validationStatus) {
        this.validationStatus = validationStatus;
    }

    @Override
    public String getMessage() {
        final StringBuilder sb = new StringBuilder();
        validationStatus.getStatuses().stream().filter(status -> Level.ERROR.equals(status.getLevel()))
                .map(Status::getMessage).forEach(s -> sb.append("\n- ").append(s));
        return sb.toString();
    }

}
