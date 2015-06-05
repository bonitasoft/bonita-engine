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

import java.util.Arrays;
import java.util.List;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationTokenValidator {

    public ValidationStatus validate(String token) {
        List<String> keywords = Arrays.asList("content", "api", "theme");
        if (token == null || !token.matches("((\\p{Alnum})|-|\\.|_|~)+") || keywords.contains(token.toLowerCase())) {
            StringBuilder stb = new StringBuilder("The token '");
            stb.append(token);
            stb.append("' is invalid: the token can not be null or empty and should contain only alpha numeric characters and the following ");
            stb.append("special characters '-', '.', '_' or '~'. In addition, the following words are reserved keywords and cannot be used as token: 'api', 'content', 'theme'.");
            return new ValidationStatus(false, stb.toString());
        }
        return new ValidationStatus(true);
    }

}
