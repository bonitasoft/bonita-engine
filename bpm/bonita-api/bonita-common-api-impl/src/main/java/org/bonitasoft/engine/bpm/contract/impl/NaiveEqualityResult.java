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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.bpm.contract.impl;



/**
 * @author Julien Reboul
 *
 */
public enum NaiveEqualityResult {
    CONTINUE, RETURN_FALSE, RETURN_TRUE;

    public static NaiveEqualityResult checkEquality(final Object obj1, final Object obj2) {
        if (obj1 == obj2) {
            return RETURN_TRUE;
        } else if (obj2 == null) {
            return RETURN_FALSE;
        } else if (obj1.getClass() != obj2.getClass()) {
            return RETURN_FALSE;
        } else {
            return CONTINUE;
        }
    }
}
