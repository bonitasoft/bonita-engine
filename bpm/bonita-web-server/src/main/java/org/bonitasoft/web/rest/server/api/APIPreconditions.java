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
package org.bonitasoft.web.rest.server.api;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.i18n.T_;

/**
 * @author Vincent Elcrin
 */
public class APIPreconditions {

    public static void check(boolean condition, T_ message) {
        if (!condition) {
            throw new APIException(message);
        }
    }

    public static boolean containsOnly(String key, Map<String, String> map) {
        if (map == null) {
            return false;
        }
        HashMap<String, String> clone = new HashMap<>(map);
        return clone.remove(key) != null && clone.isEmpty();
    }
}
