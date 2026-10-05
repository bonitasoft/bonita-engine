/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.maintenance;

import org.bonitasoft.engine.bpm.BonitaObject;

/**
 * This object holds maintenance details such as maintenance state and message.
 */
public interface MaintenanceDetails extends BonitaObject {

    public State getMaintenanceState();

    public String getMaintenanceMessage();

    /**
     * Is the maintenance message active?
     *
     * @return <code>true</code> or <code>false</code> if the activation is set, or <code>null</code> for
     *         implementations that distinguish "not supplied"
     * @see #getMaintenanceMessageActive()
     */
    public Boolean isMaintenanceMessageActive();

    /**
     * Is the maintenance message active? The canonical accessor for this property, and the one JavaBeans
     * introspection and Groovy property access resolve to - {@link #isMaintenanceMessageActive()} returns the
     * wrapper <code>Boolean</code>, and the JavaBeans specification sanctions the <code>is</code> form only for a
     * primitive <code>boolean</code>. {@link #isMaintenanceMessageActive()} is retained for existing callers.
     * <p>
     * The property is nullable: <code>null</code> means "not supplied", which is how a partial update is
     * expressed. See {@code doc/groovy_3_to_4-specific-study/} for why this alias exists and why the return type
     * was not narrowed to a primitive.
     *
     * @return <code>true</code> or <code>false</code> if the activation is set, or <code>null</code> if it is not
     * @since 12.0.0
     * @see #isMaintenanceMessageActive()
     */
    default Boolean getMaintenanceMessageActive() {
        return isMaintenanceMessageActive();
    }

    /**
     * Activation state of platform maintenance
     */
    public enum State {

        ENABLED, DISABLED
    }
}
