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
package org.bonitasoft.engine.bpm.process;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Celine Souchet
 * @version 6.3.5
 * @since 6.0.0
 * @see ProcessInstance#getState()
 */
public enum ProcessInstanceState {

    /**
     * The initializing state. Id=0
     */
    INITIALIZING(0),

    /**
     * The started state. Id=1
     */
    STARTED(1),

    /**
     * The suspended state. Id=2
     */
    SUSPENDED(2),

    /**
     * The cancelled state. Id=3
     */
    CANCELLED(3),

    /**
     * The aborted state. Id=4
     */
    ABORTED(4),

    /**
     * The completing state. Id=5
     */
    COMPLETING(5),

    /**
     * The completed state. Id=6
     */
    COMPLETED(6),

    /**
     * The error state. Id=7
     */
    ERROR(7),

    /**
     * The aborting state. Id=11
     */
    ABORTING(11);

    private static Map<Integer, ProcessInstanceState> map = new HashMap<Integer, ProcessInstanceState>(11);

    private int id;

    private ProcessInstanceState(final int id) {
        this.id = id;
    }

    /**
     * Get the identifier corresponding to the state.
     * 
     * @return The identifier corresponding to the state.
     * @since 6.3.5
     */
    public int getId() {
        return id;
    }

    /**
     * Get the {@link ProcessInstanceState} corresponding to the identifier.
     * 
     * @return The {@link ProcessInstanceState} corresponding to the identifier.
     * @since 6.3.5
     */
    public static ProcessInstanceState getFromId(final int id) {
        if (!map.containsKey(id)) {
            map.put(id, fromIdToProcessInstanceState(id));
        }
        return map.get(id);
    }

	private static ProcessInstanceState fromIdToProcessInstanceState(final int id) {
		for (final ProcessInstanceState state : values()) {
			if (id == state.getId()) {
				return state;
			}
		}
		return null;
	}
}
