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
package org.bonitasoft.engine.bpm.process;

/**
 * Use to update the string indexes.
 *
 * @author Emmanuel Duchastenier
 * @since 7.12.0
 * @see ProcessInstance#getStringIndex1()
 * @see ProcessInstance#getStringIndex2()
 * @see ProcessInstance#getStringIndex3()
 * @see ProcessInstance#getStringIndex4()
 * @see ProcessInstance#getStringIndex5()
 * @see org.bonitasoft.engine.api.ProcessAPI#updateProcessInstanceIndex(long, Index, String)
 */
public enum Index {

    /**
     * Corresponding to the first search key
     */
    FIRST,
    /**
     * Corresponding to the second search key
     */
    SECOND,
    /**
     * Corresponding to the third search key
     */
    THIRD,
    /**
     * Corresponding to the fourth search key
     */
    FOURTH,
    /**
     * Corresponding to the fifth search key
     */
    FIFTH
}
