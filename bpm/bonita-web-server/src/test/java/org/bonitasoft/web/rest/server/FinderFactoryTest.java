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
package org.bonitasoft.web.rest.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.server.api.bpm.flownode.ActivityVariableResource;
import org.bonitasoft.web.rest.server.api.bpm.flownode.ActivityVariableResourceFinder;
import org.bonitasoft.web.rest.server.api.bpm.message.BPMMessageResource;
import org.bonitasoft.web.rest.server.api.bpm.message.BPMMessageResourceFinder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.ServerResource;

@RunWith(MockitoJUnitRunner.class)
public class FinderFactoryTest {

    private FinderFactory factory;

    @Mock
    private ProcessAPI processAPI;

    @Mock
    private APISession apiSession;

    private final Request request = new Request();
    private final Response response = new Response(request);

    @Before
    public void setUp() {
        factory = new FinderFactory();
    }

    @Test(expected = RuntimeException.class)
    public void should_throw_RuntimeException_for_a_not_supported_class() {

        factory.create(NotSupportedResource.class);
    }

    private class NotSupportedResource extends ServerResource {

    }

    @Test
    public void should_return_ActivityVariableResource_for_ActivityVariableResourceFinder() {
        final ActivityVariableResourceFinder activityVariableResourceFinder = spy(new ActivityVariableResourceFinder());
        doReturn(processAPI).when(activityVariableResourceFinder).getProcessAPI(any(Request.class));
        final ServerResource serverResource = activityVariableResourceFinder.create(request, response);
        assertThat(serverResource).isInstanceOf(ActivityVariableResource.class);
    }

    @Test
    public void should_return_SendMessageResource_for_SendMessageResourceFinder() {
        final BPMMessageResourceFinder sendMessageResourceFinder = spy(new BPMMessageResourceFinder());
        doReturn(processAPI).when(sendMessageResourceFinder).getProcessAPI(any(Request.class));
        final ServerResource serverResource = sendMessageResourceFinder.create(request, response);
        assertThat(serverResource).isInstanceOf(BPMMessageResource.class);
    }

}
