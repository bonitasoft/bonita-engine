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
package org.bonitasoft.engine.core.process.definition.model.event.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.bonitasoft.engine.core.process.definition.model.event.trigger.impl.SCatchErrorEventTriggerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.STransitionDefinitionImpl;
import org.junit.Test;

/**
 * Created by Vincent Elcrin
 * Date: 18/12/13
 * Time: 14:50
 * 
 * @author Celine Souchet
 */
public class SStartEventDefinitionImplTest {

    SStartEventDefinitionImpl startEvent = new SStartEventDefinitionImpl(1L, "name");

    @Test
    public void isStartable_return_false_if_start_event_has_trigger_events() {
        startEvent.addEventTriggerDefinition(new SCatchErrorEventTriggerDefinitionImpl("error"));

        assertFalse(startEvent.isStartable());
    }

    @Test
    public void isStartable_return_false_if_start_event_has_incoming_transitions() {
        startEvent.addIncomingTransition(new STransitionDefinitionImpl("incoming"));

        assertFalse(startEvent.isStartable());
    }

    @Test
    public void isStartable_return_true_if_start_event_has_no_incoming_transitions_and_no_trigger_events() {
        assertTrue(startEvent.isStartable());
    }
}
