/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
 **/

package org.bonitasoft.engine.core.process.definition.model.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Created by Vincent Elcrin
 * Date: 18/12/13
 * Time: 15:00
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
