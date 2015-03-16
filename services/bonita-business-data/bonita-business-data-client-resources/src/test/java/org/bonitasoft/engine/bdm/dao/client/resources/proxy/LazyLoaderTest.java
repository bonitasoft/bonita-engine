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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.bdm.proxy.model.Child;
import org.bonitasoft.engine.bdm.proxy.model.Parent;
import org.bonitasoft.engine.session.APISession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.bonitasoft.engine.bdm.dao.client.resources.utils.BDMQueryCommandParameters;
import org.bonitasoft.engine.bdm.dao.client.resources.utils.EntityGetter;

@RunWith(MockitoJUnitRunner.class)
public class LazyLoaderTest {

    @Mock
    private CommandAPI commandAPI;
    
    private LazyLoader lazyLoader;
    
    @Before
    public void setUp() throws Exception {
        lazyLoader = spy(new LazyLoader(new FakeSession()));
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
    @SuppressWarnings("unchecked")
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
    
    private Map<String, Serializable> parameters(Method method, long persistenceId) {
        return BDMQueryCommandParameters.createCommandParameters(new EntityGetter(method), persistenceId);
    }
    
    @SuppressWarnings("serial")
    private class FakeSession implements APISession {

        @Override
        public long getId() {
            return 0;
        }

        @Override
        public Date getCreationDate() {
            return null;
        }

        @Override
        public long getDuration() {
            return 0;
        }

        @Override
        public String getUserName() {
            return null;
        }

        @Override
        public long getUserId() {
            return 0;
        }

        @Override
        public boolean isTechnicalUser() {
            return false;
        }

        @Override
        public String getTenantName() {
            return null;
        }

        @Override
        public long getTenantId() {
            return 0;
        }
    }
}
