/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.bonitasoft.engine.bdm.validator.rule;

import javax.lang.model.SourceVersion;

import com.bonitasoft.engine.bdm.QueryParameter;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

public class QueryParameterValidationRule implements ValidationRule {
	
	@Override
	public boolean appliesTo(Object modelElement) {
		return modelElement instanceof QueryParameter;
	}

	@Override
	public ValidationStatus checkRule(Object modelElement) {
		if(!appliesTo(modelElement)){
			throw new IllegalArgumentException(QueryParameterValidationRule.class.getName() +" doesn't handle validation for "+modelElement.getClass().getName());
		}
		QueryParameter parameter = (QueryParameter) modelElement;
		ValidationStatus status = new ValidationStatus();
		String name = parameter.getName();
		if (name == null || name.isEmpty()) {
			status.addError("A parameter must have name");
			return status;
		}
		if(!SourceVersion.isIdentifier(name)){
		    status.addError(name + " is not a valid Java identifier.");
		}

		if(parameter.getClassName() == null || parameter.getClassName().isEmpty()){
			status.addError(name + " query parameter must have a classname");
		}
		return status;
	}




}
