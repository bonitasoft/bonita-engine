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
package org.bonitasoft.engine.bdm.dao.client.resources.proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.bdm.dao.client.resources.utils.BDMQueryCommandParameters;
import org.bonitasoft.engine.bdm.dao.client.resources.utils.EntityGetter;
import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.bdm.proxy.model.Child;
import org.bonitasoft.engine.bdm.proxy.model.Parent;
import org.bonitasoft.engine.session.impl.APISessionImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LazyLoaderTest {

    @Mock
    private CommandAPI commandAPI;

    private LazyLoader lazyLoader;

    @Before
    public void setUp() throws Exception {
        lazyLoader = spy(new LazyLoader(new APISessionImpl(1, new Date(), 3000, "john", 1, "default", 1)));
        doReturn(commandAPI).when(lazyLoader).getCommandAPI();
    }

    @Test
    public void should_load_object_through_command_api() throws Exception {
        long persistenceId = 22L;
        Child luce = new Child("Luce", 2);
        Method getChild = Parent.class.getMethod("getChild");
        when(commandAPI.execute("executeBDMQuery", parameters(getChild, persistenceId))).thenReturn(luce.toJson().getBytes());

        Object loadedChild = lazyLoader.load(getChild, persistenceId);

        assertThat(loadedChild).isEqualTo(luce);
    }

    @Test
    public void should_load_list_of_objects_through_command_api() throws Exception {
        long persistenceId = 22L;
        Child luce = new Child("Luce", 2);
        Child julien = new Child("Julien", 5);
        String json = "[" + luce.toJson() + "," + julien.toJson() + "]";
        Method getChildren = Parent.class.getMethod("getChildren");
        when(commandAPI.execute("executeBDMQuery", parameters(getChildren, persistenceId))).thenReturn(json.getBytes());

        Object loadedChild = lazyLoader.load(getChildren, persistenceId);

        assertThat(loadedChild).isInstanceOf(List.class);
        assertThat((List<Child>) loadedChild).containsOnly(luce, julien);
    }

    @Test
    public void should_getParameters_return_real_type_when_query_returns_list() throws Exception {
        long persistenceId = 22L;
        Method getChildren = Parent.class.getMethod("getChildren");

        //when
        // use the concrete type as we need a Serializable object in later in the code
        final Map<String, Serializable> parameters = parameters(getChildren, persistenceId);

        //then
        final HashMap<String, Serializable> queryParameters = new HashMap<>(); // use the concrete type as we need a Serializable object in later in the code

        queryParameters.put(Field.PERSISTENCE_ID, persistenceId);

        assertThat(parameters).hasSize(6)
                .containsOnly(
                        entry("queryName", "Child.findChildrenByParentPersistenceId"),
                        entry("returnsList", true),
                        entry("startIndex", 0),
                        entry("maxResults", Integer.MAX_VALUE),
                        entry("returnType", Child.class.getName()),
                        entry("queryParameters", queryParameters));
    }

    private Map<String, Serializable> parameters(Method method, long persistenceId) {
        return BDMQueryCommandParameters.createCommandParameters(new EntityGetter(method), persistenceId);
    }

}
