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
package org.bonitasoft.engine.api.impl.validator;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class TokenValidator {

    private String token;

    private String error;

    public TokenValidator(String token) {
        this.token = token;
    }

    public boolean validate() {
        if(token == null || !token.matches("((\\p{Alnum})|-|\\.|_|~)+")) {
            error = "The token '"
                    + token
                    + "' is invalid: the token can not be null or empty and should contain only alpha numeric characters and the following special characters '-', '.', '_' or '~'";
            return false;
        }
        return  true;
    }

    public String getError() {
        return error;
    }
}
