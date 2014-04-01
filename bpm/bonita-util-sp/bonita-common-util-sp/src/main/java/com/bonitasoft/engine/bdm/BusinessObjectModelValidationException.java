/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package com.bonitasoft.engine.bdm;

import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Romain Bioteau
 *
 */
public class BusinessObjectModelValidationException extends Exception {

	private ValidationStatus validationStatus;

	public BusinessObjectModelValidationException(
			ValidationStatus validationStatus) {
		this.validationStatus = validationStatus;
	}
	
	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		for(String errorMessage : validationStatus.getErrors()){
			sb.append("\n");
			sb.append("- " +errorMessage);
		}
		return sb.toString();
	}
	
	private static final long serialVersionUID = 1L;

}
