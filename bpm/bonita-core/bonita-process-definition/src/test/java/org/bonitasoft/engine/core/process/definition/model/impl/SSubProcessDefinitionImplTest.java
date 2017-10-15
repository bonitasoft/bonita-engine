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
package org.bonitasoft.engine.core.process.definition.model.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author Vincent Elcrin
 */
public class SSubProcessDefinitionImplTest {

    SSubProcessDefinitionImpl subProcessTriggeredByEvent = new SSubProcessDefinitionImpl(1L, "name", true);

    SSubProcessDefinitionImpl subProcess = new SSubProcessDefinitionImpl(1L, "name", false);

    @Test
    public void isStartable_return_false_if_sub_process_is_triggered_by_event() {
        assertFalse(subProcessTriggeredByEvent.isStartable());
    }

    @Test
    public void isStartable_return_false_if_sub_process_has_incoming_transitions() {
        subProcess.addIncomingTransition(new STransitionDefinitionImpl("incoming"));

        assertFalse(subProcess.isStartable());
    }

    @Test
    public void isStartable_return_true_if_sub_process_has_no_incoming_transitions_and_is_not_triggered_by_event() {
        assertTrue(subProcess.isStartable());
    }

}
