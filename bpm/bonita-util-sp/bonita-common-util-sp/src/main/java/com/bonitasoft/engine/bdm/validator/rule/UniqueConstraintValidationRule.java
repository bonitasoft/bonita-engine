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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bonitasoft.engine.bdm.UniqueConstraint;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

public class UniqueConstraintValidationRule implements ValidationRule {

	@Override
	public boolean appliesTo(Object modelElement) {
		return modelElement instanceof UniqueConstraint;
	}

	@Override
	public ValidationStatus checkRule(Object modelElement) {
		if(!appliesTo(modelElement)){
			throw new IllegalArgumentException(UniqueConstraintValidationRule.class.getName() +" doesn't handle validation for "+modelElement.getClass().getName());
		}
		UniqueConstraint uc = (UniqueConstraint) modelElement;
		ValidationStatus status = new ValidationStatus();
		String name = uc.getName();
		if (name == null || name.isEmpty()) {
			status.addError("A unique constraint must have name");
			return status;
		}
		boolean hasWhitspaces = hasWhitespaces(name);
		if(hasWhitspaces){
			status.addError(name + " cannot have whitespaces");
		}

		if(uc.getFieldNames().isEmpty()){
			status.addError(name + " unique constraint must have at least one field declared");
		}

		return status;
	}

	private boolean hasWhitespaces(String name) {
		Pattern pattern = Pattern.compile("\\s");
		Matcher matcher = pattern.matcher(name);
		boolean hasWhitspaces = matcher.find();
		return hasWhitspaces;
	}


}
