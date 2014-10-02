/**
 * Copyright (C) 2011-2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.api.impl.DummySCustomUserInfoDefinition;
import org.bonitasoft.engine.api.impl.DummySCustomUserInfoValue;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.EventTriggerInstance;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerInstance;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerType;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SThrowErrorEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SThrowMessageEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SThrowSignalEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.SEventTriggerInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.SThrowErrorEventTriggerInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.SThrowMessageEventTriggerInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.SThrowSignalEventTriggerInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.STimerEventTriggerInstanceImpl;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.identity.CustomUserInfoValue;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.impl.CustomUserInfoDefinitionImpl;
import org.bonitasoft.engine.identity.model.SCustomUserInfoValue;
import org.bonitasoft.engine.identity.model.SUser;
import org.junit.Test;

public class ModelConvertorTest {

    @Test
    public void convertDataInstanceIsTransient() {
        final SDataInstance sDataInstance = mock(SDataInstance.class);
        when(sDataInstance.getClassName()).thenReturn(Integer.class.getName());
        when(sDataInstance.isTransientData()).thenReturn(true);

        final DataInstance dataInstance = ModelConvertor.toDataInstance(sDataInstance);
        assertTrue(dataInstance.isTransientData());
    }

    @Test
    public void convertDataInstanceIsNotTransient() {
        final SDataInstance sDataInstance = mock(SDataInstance.class);
        when(sDataInstance.getClassName()).thenReturn(Integer.class.getName());
        when(sDataInstance.isTransientData()).thenReturn(false);

        final DataInstance dataInstance = ModelConvertor.toDataInstance(sDataInstance);
        assertFalse(dataInstance.isTransientData());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getProcessInstanceState_conversionOnUnknownStateShouldThrowException() {
        ModelConvertor.getProcessInstanceState("un_known_state");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getProcessInstanceState_conversionOnNullStateShouldThrowException() {
        ModelConvertor.getProcessInstanceState(null);
    }

    @Test
    public void convertSUserToUserDoesntShowPassword() {
        final SUser sUser = mock(SUser.class);

        final User testUser = ModelConvertor.toUser(sUser);

        assertThat(testUser.getPassword()).isEmpty();
        verify(sUser, never()).getPassword();
    }

    @Test
    public void should_convert_server_definition_into_client_definition() {
        CustomUserInfoDefinitionImpl definition = ModelConvertor.convert(
                new DummySCustomUserInfoDefinition(1L, "name", "description"));

        assertThat(definition.getId()).isEqualTo(1L);
        assertThat(definition.getName()).isEqualTo("name");
        assertThat(definition.getDescription()).isEqualTo("description");
    }

    @Test
    public void should_convert_server_value_into_client_value() {
        CustomUserInfoValue value = ModelConvertor.convert(
                new DummySCustomUserInfoValue(2L, 2L, 1L, "value"));

        assertThat(value.getDefinitionId()).isEqualTo(2L);
        assertThat(value.getValue()).isEqualTo("value");
        assertThat(value.getUserId()).isEqualTo(1L);
    }

    @Test
    public void should_return_null_when_trying_to_convert_a_null_value() {
        CustomUserInfoValue value = ModelConvertor.convert((SCustomUserInfoValue) null);

        assertThat(value).isNull();
    }

    @Test
    public void toEventTriggerInstance_cant_convert_ERROR_Type() {
        // Given
        final SThrowErrorEventTriggerInstance sEventTriggerInstance = new SThrowErrorEventTriggerInstanceImpl();

        // Then
        final EventTriggerInstance eventTriggerInstance = ModelConvertor.toEventTriggerInstance(sEventTriggerInstance);

        // When
        assertNull(eventTriggerInstance);
    }

    @Test
    public void toEventTriggerInstance_cant_convert_SIGNAL_Type() {
        // Given
        final SThrowMessageEventTriggerInstance sEventTriggerInstance = new SThrowMessageEventTriggerInstanceImpl();

        // Then
        final EventTriggerInstance eventTriggerInstance = ModelConvertor.toEventTriggerInstance(sEventTriggerInstance);

        // When
        assertNull(eventTriggerInstance);
    }

    @Test
    public void toEventTriggerInstance_cant_convert_MESSAGE_Type() {
        // Given
        final SThrowSignalEventTriggerInstance sEventTriggerInstance = new SThrowSignalEventTriggerInstanceImpl();

        // Then
        final EventTriggerInstance eventTriggerInstance = ModelConvertor.toEventTriggerInstance(sEventTriggerInstance);

        // When
        assertNull(eventTriggerInstance);
    }

    @Test
    public void toEventTriggerInstance_cant_convert_TERMINATE_Type() {
        // Given
        final SEventTriggerInstance sEventTriggerInstance = new SEventTriggerInstanceImpl() {

            private static final long serialVersionUID = 514899463254242741L;

            @Override
            public String getDiscriminator() {
                return null;
            }

            @Override
            public SEventTriggerType getEventTriggerType() {
                return SEventTriggerType.TERMINATE;
            }
        };

        // Then
        final EventTriggerInstance eventTriggerInstance = ModelConvertor.toEventTriggerInstance(sEventTriggerInstance);

        // When
        assertNull(eventTriggerInstance);
    }

    @Test
    public void toEventTriggerInstance_can_convert_TIMER_Type() {
        // Given
        final STimerEventTriggerInstance sTimerEventTriggerInstance = new STimerEventTriggerInstanceImpl(2, "eventInstanceName", 69, "jobTriggerName");

        // Then
        final TimerEventTriggerInstance eventTriggerInstance = (TimerEventTriggerInstance) ModelConvertor.toEventTriggerInstance(sTimerEventTriggerInstance);

        // When
        assertNotNull(eventTriggerInstance);
        assertEquals(sTimerEventTriggerInstance.getEventInstanceId(), eventTriggerInstance.getEventInstanceId());
        assertEquals(sTimerEventTriggerInstance.getId(), eventTriggerInstance.getId());
        assertEquals(sTimerEventTriggerInstance.getEventInstanceName(), eventTriggerInstance.getEventInstanceName());
        assertEquals(sTimerEventTriggerInstance.getExecutionDate(), eventTriggerInstance.getExecutionDate().getTime());
    }

    @Test
    public void toTimerEventTriggerInstance_can_convert() {
        // Given
        final STimerEventTriggerInstance sTimerEventTriggerInstance = new STimerEventTriggerInstanceImpl(2, "eventInstanceName", 69, "jobTriggerName");

        // Then
        final TimerEventTriggerInstance eventTriggerInstance = ModelConvertor.toTimerEventTriggerInstance(sTimerEventTriggerInstance);

        // When
        assertNotNull(eventTriggerInstance);
        assertEquals(sTimerEventTriggerInstance.getEventInstanceId(), eventTriggerInstance.getEventInstanceId());
        assertEquals(sTimerEventTriggerInstance.getId(), eventTriggerInstance.getId());
        assertEquals(sTimerEventTriggerInstance.getEventInstanceName(), eventTriggerInstance.getEventInstanceName());
        assertEquals(sTimerEventTriggerInstance.getExecutionDate(), eventTriggerInstance.getExecutionDate().getTime());
    }
}
