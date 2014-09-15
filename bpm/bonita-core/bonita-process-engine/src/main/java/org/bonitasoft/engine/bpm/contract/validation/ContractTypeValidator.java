/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.contract.validation;

import org.bonitasoft.engine.bpm.contract.validation.type.BooleanValidator;
import org.bonitasoft.engine.bpm.contract.validation.type.DateValidator;
import org.bonitasoft.engine.bpm.contract.validation.type.DecimalValidator;
import org.bonitasoft.engine.bpm.contract.validation.type.IntegerValidator;
import org.bonitasoft.engine.bpm.contract.validation.type.TextValidator;
import org.bonitasoft.engine.core.process.definition.model.SType;

public class ContractTypeValidator {

    public boolean isValid(SType type, Object object) {
        switch (type) {
            case BOOLEAN:
                return new BooleanValidator().validate(object);
            case DATE:
                return new DateValidator().validate(object);
            case DECIMAL:
                return new DecimalValidator().validate(object);
            case INTEGER:
                return new IntegerValidator().validate(object);
            case TEXT: 
                return new TextValidator().validate(object);
            default:
                return false;
        }

    }
}
