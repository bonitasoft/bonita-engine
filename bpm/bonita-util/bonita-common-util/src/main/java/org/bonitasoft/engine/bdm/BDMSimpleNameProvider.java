/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

/**
 * @author Elias Ricken de Medeiros
 */
public class BDMSimpleNameProvider {

    public static char getSimpleNameAlias(String simpleName) {
        return Character.toLowerCase(simpleName.charAt(0));
    }

    public static String getSimpleBusinessObjectName(final String businessObjectName) {
        String newBusinessObjectName = businessObjectName;
        final int lastIndexOf = newBusinessObjectName.lastIndexOf(".");
        if (lastIndexOf != -1) {
            newBusinessObjectName = newBusinessObjectName.substring(lastIndexOf + 1, newBusinessObjectName.length());
        }
        return newBusinessObjectName;
    }

}
