/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

package org.bonitasoft.engine.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.MapEntry.entry;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.service.ServicesResolver;
import org.bonitasoft.engine.service.TaskResult;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class BroadcastServiceLocalTest {

    public static final Long TENANT_ID = 125L;
    @InjectMocks
    private BroadcastServiceLocal broadcastServiceLocal;
    @Mock
    private Callable callable;
    @Mock
    private ServicesResolver servicesResolver;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void should_execute_on_platform_inject_service() throws Exception {
        //when
        broadcastServiceLocal.executeOnAllNodes(callable);
        //then
        verify(servicesResolver).injectServices(null, callable);
    }

    @Test
    public void should_execute_on_platform_execute_call_on_callable() throws Exception {
        //when
        broadcastServiceLocal.executeOnAllNodes(callable);
        //then
        verify(callable).call();
    }

    @Test
    public void should_execute_on_tenant_inject_service() throws Exception {
        //when
        broadcastServiceLocal.executeOnAllNodes(callable, TENANT_ID);
        //then
        verify(servicesResolver).injectServices(TENANT_ID, callable);
    }

    @Test
    public void should_execute_on_tenant_execute_call_on_callable() throws Exception {
        //when
        broadcastServiceLocal.executeOnAllNodes(callable, TENANT_ID);
        //then
        verify(callable).call();
    }

    @Test
    public void should_execute_on_platform_report_error() throws Exception {
        //given
        IOException exception = new IOException();
        doThrow(exception).when(callable).call();
        //when
        Map execute = broadcastServiceLocal.executeOnAllNodes(callable);
        //then
        assertThat(execute).containsOnly(entry("local", TaskResult.error(exception)));
    }

    @Test
    public void should_execute_on_platform_report_ok() throws Exception {
        //given
        Object returned = new Object();
        doReturn(returned).when(callable).call();
        //when
        Map execute = broadcastServiceLocal.executeOnAllNodes(callable);
        //then
        assertThat(execute).containsOnly(entry("local", TaskResult.ok(returned)));
    }

    @Test
    public void should_execute_on_tenant_report_error() throws Exception {
        //given
        IOException exception = new IOException();
        doThrow(exception).when(callable).call();
        //when
        Map execute = broadcastServiceLocal.executeOnAllNodes(callable, TENANT_ID);
        //then
        assertThat(execute).containsOnly(entry("local", TaskResult.error(exception)));
    }

    @Test
    public void should_execute_on_tenant_report_ok() throws Exception {
        //given
        Object returned = new Object();
        doReturn(returned).when(callable).call();
        //when
        Map execute = broadcastServiceLocal.executeOnAllNodes(callable, TENANT_ID);
        //then
        assertThat(execute).containsOnly(entry("local", TaskResult.ok(returned)));
    }

    @Test
    public void should_execute_on_tenant_throw_illegalStateException__when_unable_to_inject_service() throws Exception {
        //given
        doThrow(InvocationTargetException.class).when(servicesResolver).injectServices(anyLong(), any(Object.class));
        //then
        expectedException.expect(IllegalStateException.class);
        broadcastServiceLocal.executeOnAllNodes(callable, TENANT_ID);
    }

    @Test
    public void should_execute_on_platform_throw_illegalStateException__when_unable_to_inject_service() throws Exception {
        //given
        doThrow(InvocationTargetException.class).when(servicesResolver).injectServices(anyLong(), any(Object.class));
        //then
        expectedException.expect(IllegalStateException.class);
        broadcastServiceLocal.executeOnAllNodes(callable);
    }

    @Test
    public void should_do_nothing_when_calling_executeOnOthers() throws Exception {
        //when
        broadcastServiceLocal.executeOnOthers(callable);
        //then
        verifyZeroInteractions(callable, servicesResolver);
    }

    @Test
    public void should_do_nothing_when_calling_executeOnOthers_on_tenant() throws Exception {
        //when
        broadcastServiceLocal.executeOnOthers(callable, TENANT_ID);
        //then
        verifyZeroInteractions(callable, servicesResolver);
    }

}
