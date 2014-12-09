/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.api;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Hashtable;

import javax.naming.NamingException;

import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.junit.Before;
import org.junit.Test;


/**
 * @author Julien Reboul
 *
 */
public class EJB3ServerAPITest {

    private EJB3ServerAPI ejb3ServerAPI;
    private HashMap<String, String> parameters;

    @Before
    public void setUp() throws Exception {
        parameters = mock(HashMap.class);
        ejb3ServerAPI = spy(new EJB3ServerAPI());
        ejb3ServerAPI.parameters = parameters;
    }

    @Test
    public void testEJB3ServerAPI_calls_default_reference_on_init() throws Exception {
        doReturn(mock(ServerAPI.class)).when(ejb3ServerAPI).lookup(eq(EJB3ServerAPI.SERVER_API_BEAN_NAME_JBOSS7), any(Hashtable.class));
        ejb3ServerAPI.initServerAPIReference();
        verify(ejb3ServerAPI).lookup(eq(EJB3ServerAPI.SERVER_API_BEAN_NAME_JBOSS7), any(Hashtable.class));
    }

    @Test
    public void testEJB3ServerAPI_calls_given_reference_on_init() throws Exception {
        final String ejbReference = "serverAPI";
        doReturn(mock(ServerAPI.class)).when(ejb3ServerAPI).lookup(eq(ejbReference), any(Hashtable.class));
        when(parameters.get(EJB3ServerAPI.EJB_NAMING_REFERENCE_PROPERTY)).thenReturn(ejbReference);
        ejb3ServerAPI.initServerAPIReference();
        verify(ejb3ServerAPI).lookup(eq(ejbReference), any(Hashtable.class));
    }

    @Test
    public void testEJB3ServerAPI_throws_error_on_wrong_reference_name_on_init() throws Exception {
        final String ejbReference = "serverAPI";
        doThrow(NamingException.class).when(ejb3ServerAPI).lookup(eq(ejbReference), any(Hashtable.class));
        when(parameters.get(EJB3ServerAPI.EJB_NAMING_REFERENCE_PROPERTY)).thenReturn(ejbReference);
        try {
            ejb3ServerAPI.initServerAPIReference();
            fail();
        } catch (final ServerAPIException e) {
            assertThat(e).hasCauseExactlyInstanceOf(NamingException.class).hasMessage(
                    "[" + ejbReference + "] Reference To Server API does not exists. Edit bonita-client.properties#"
                            + EJB3ServerAPI.EJB_NAMING_REFERENCE_PROPERTY + " property to change the reference name");
            verify(ejb3ServerAPI).lookup(eq(ejbReference), any(Hashtable.class));
        }
    }

}
