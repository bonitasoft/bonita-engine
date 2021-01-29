/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.classloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.identifier;
import static org.bonitasoft.engine.dependency.model.ScopeType.PROCESS;
import static org.bonitasoft.engine.dependency.model.ScopeType.TENANT;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class ParentClassLoaderResolverTest {

    @Mock
    private ReadSessionAccessor sessionAccessor;
    @InjectMocks
    private ParentClassLoaderResolver parentClassLoaderResolver;

    @Test
    public void should_getParentClassLoaderIdentifier_return_global_on_tenant_classloader() throws Exception {
        //given
        final ClassLoaderIdentifier childId = identifier(TENANT, 124);
        //when
        final ClassLoaderIdentifier parentClassLoaderIdentifier = parentClassLoaderResolver
                .getParentClassLoaderIdentifier(childId);
        //then
        assertThat(parentClassLoaderIdentifier).isEqualTo(ClassLoaderIdentifier.GLOBAL);
    }

    @Test(expected = BonitaRuntimeException.class)
    public void should_getParentClassLoaderIdentifier_throw_exception_on_process_if_no_tenant() throws Exception {
        //given
        doThrow(new STenantIdNotSetException("")).when(sessionAccessor).getTenantId();
        final ClassLoaderIdentifier childId = identifier(PROCESS, 124);
        //when
        parentClassLoaderResolver.getParentClassLoaderIdentifier(childId);
        //then exception
    }

    @Test
    public void should_getParentClassLoaderIdentifier_return_tenant() throws Exception {
        //given
        doReturn(25l).when(sessionAccessor).getTenantId();
        final ClassLoaderIdentifier childId = identifier(PROCESS, 124);
        //when
        final ClassLoaderIdentifier parentClassLoaderIdentifier = parentClassLoaderResolver
                .getParentClassLoaderIdentifier(childId);
        //then
        assertThat(parentClassLoaderIdentifier).isEqualTo(identifier(TENANT, 25));
    }

    @Test
    public void should_give_APPLICATION_for_parent_of_global_classloader() throws Exception {
        ClassLoaderIdentifier parentClassLoaderIdentifier = parentClassLoaderResolver
                .getParentClassLoaderIdentifier(ClassLoaderIdentifier.GLOBAL);

        assertThat(parentClassLoaderIdentifier).isEqualTo(ClassLoaderIdentifier.APPLICATION);
    }

}
