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
package org.bonitasoft.web.rest.server.api.bpm.flownode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Collections;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerInstanceNotFoundException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.junit.Test;
import org.restlet.Response;
import org.restlet.representation.Representation;

public class TimerEventTriggerResourceIT extends AbstractConsoleTest {

    private TimerEventTriggerResource restResource;

    @Override
    public void consoleTestSetUp() throws Exception {
        restResource = spy(new TimerEventTriggerResource(TenantAPIAccessor.getProcessAPI(getInitiator().getSession())));
        doReturn(mock(HttpServletRequest.class)).when(restResource).getHttpRequest();
        final HttpSession session = mock(HttpSession.class);
        doReturn(session).when(restResource).getHttpSession();
        final APISession apiSession = TestUserFactory.getJohnCarpenter().getSession();
        doReturn(apiSession).when(session).getAttribute("apiSession");
        doReturn(0).when(restResource).getIntegerParameter(anyString(), anyBoolean());
        doReturn(0L).when(restResource).getLongParameter(anyString(), anyBoolean());
        doReturn("").when(restResource).getParameter(anyString(), anyBoolean());
        final Response response = mock(Response.class);
        doReturn(mock(Representation.class)).when(response).getEntity();
        doReturn(response).when(restResource).getResponse();
        doReturn(Collections.emptyList()).when(restResource).getParameterAsList(anyString());
    }

    @Test
    public void should_search_timer_event_triggers() throws Exception {
        restResource.searchTimerEventTriggers();
    }

    @Test(expected = TimerEventTriggerInstanceNotFoundException.class)
    public void should_throw_exception_when_timer_event_triggers_not_found() throws Exception {
        doReturn("1").when(restResource).getAttribute(TimerEventTriggerResource.ID_PARAM_NAME);

        final TimerEventTrigger trigger = new TimerEventTrigger(System.currentTimeMillis());
        restResource.updateTimerEventTrigger(trigger);
    }

    @Test
    public void should_update_timer_event_triggers() throws Exception {
        final long timerEventTriggerId = 1L;
        final ProcessAPI processAPI = mock(ProcessAPI.class);
        restResource = spy(new TimerEventTriggerResource(processAPI));
        doReturn("" + timerEventTriggerId).when(restResource).getAttribute(TimerEventTriggerResource.ID_PARAM_NAME);
        final Date date = new Date();
        doReturn(date).when(processAPI).updateExecutionDateOfTimerEventTriggerInstance(eq(timerEventTriggerId),
                any(Date.class));
        final TimerEventTrigger trigger = new TimerEventTrigger(System.currentTimeMillis());
        assertThat(restResource.updateTimerEventTrigger(trigger).getExecutionDate()).isEqualTo(date.getTime());
    }
}
