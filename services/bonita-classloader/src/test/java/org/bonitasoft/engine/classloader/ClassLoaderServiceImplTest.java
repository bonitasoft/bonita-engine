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
import static org.bonitasoft.engine.commons.Pair.pair;
import static org.bonitasoft.engine.dependency.model.ScopeType.PROCESS;
import static org.bonitasoft.engine.dependency.model.ScopeType.TENANT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;

import org.bonitasoft.engine.dependency.impl.PlatformDependencyService;
import org.bonitasoft.engine.dependency.impl.TenantDependencyService;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class ClassLoaderServiceImplTest {

    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    private ParentClassLoaderResolver parentClassLoaderResolver = new ParentClassLoaderResolver() {

        @Override
        public ClassLoaderIdentifier getParentClassLoaderIdentifier(ClassLoaderIdentifier childId) {
            if (childId.getType().equals(PROCESS.name())) {
                return new ClassLoaderIdentifier(TENANT.name(), PARENT_ID);
            } else {
                return ClassLoaderIdentifier.GLOBAL;
            }
        }
    };
    private ParentClassLoaderResolver badParentClassLoaderResolver = new ParentClassLoaderResolver() {

        @Override
        public ClassLoaderIdentifier getParentClassLoaderIdentifier(ClassLoaderIdentifier childId) {
            if (childId.getType().equals(PROCESS.name())) {
                return new ClassLoaderIdentifier(TENANT.name(), PARENT_ID);
            } else {
                return null;
            }
        }
    };
    @Mock
    private EventService eventService;
    @Mock
    private PlatformDependencyService platformDependencyService;
    @Mock
    private TenantDependencyService tenantDependencyService;
    @Mock
    private SessionAccessor sessionAccessor;
    @Mock
    private UserTransactionService userTransactionService;
    @Mock
    private BroadcastService broadcastService;
    @Mock
    private ClassLoaderUpdater classLoaderUpdater;

    @Mock
    private PlatformClassLoaderListener platformClassLoaderListener1;
    @Mock
    private PlatformClassLoaderListener platformClassLoaderListener2;

    @Captor
    private ArgumentCaptor<RefreshClassloaderSynchronization> synchronizationArgumentCaptor;

    private ClassLoaderServiceImpl classLoaderService;
    private ClassLoader testClassLoader;
    private VirtualClassLoader processClassLoader;
    private MyClassLoaderListener myClassLoaderListener;
    private VirtualClassLoader tenantClassLoader;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    private long CHILD_ID = 12;
    private long PARENT_ID = 13;

    @Before
    public void before() throws Exception {
        classLoaderService = new ClassLoaderServiceImpl(parentClassLoaderResolver, eventService,
                platformDependencyService, sessionAccessor, userTransactionService, broadcastService,
                classLoaderUpdater, Arrays.asList(platformClassLoaderListener1, platformClassLoaderListener2));
        //tenant in theses tests is 0L
        classLoaderService.registerDependencyServiceOfTenant(0L, tenantDependencyService);
        processClassLoader = classLoaderService.getLocalClassLoader(PROCESS.name(), CHILD_ID);
        tenantClassLoader = classLoaderService.getLocalClassLoader(TENANT.name(), PARENT_ID);
        testClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(processClassLoader);
        myClassLoaderListener = new MyClassLoaderListener();
        temporaryFolder.create();
    }

    @After
    public void after() {
        Thread.currentThread().setContextClassLoader(testClassLoader);
    }

    @Test
    public void should_addListener_add_on_specified_classloader_do_not_call_on_others() {
        //given
        classLoaderService.addListener(TENANT.name(), PARENT_ID, myClassLoaderListener);
        //when
        processClassLoader.destroy();
        //then
        assertThat(myClassLoaderListener.isOnDestroyCalled()).isFalse();
    }

    @Test
    public void should_addListener_add_on_specified_classloader_call_listener() {
        //given
        classLoaderService.addListener(TENANT.name(), PARENT_ID, myClassLoaderListener);
        //when
        tenantClassLoader.destroy();
        //then
        assertThat(myClassLoaderListener.isOnDestroyCalled()).isTrue();
    }

    @Test
    public void should_removeListener_remove_the_listener() {
        //given
        classLoaderService.addListener(TENANT.name(), PARENT_ID, myClassLoaderListener);
        classLoaderService.removeListener(TENANT.name(), PARENT_ID, myClassLoaderListener);
        //when
        processClassLoader.destroy();
        //then
        assertThat(myClassLoaderListener.isOnDestroyCalled()).isFalse();

    }

    @Test
    public void should_refreshClassLoader_call_replace_classloader() throws Exception {
        //given
        classLoaderService.addListener(PROCESS.name(), CHILD_ID, myClassLoaderListener);
        //when
        classLoaderService.refreshClassLoaderImmediately(PROCESS, CHILD_ID);
        //then
        assertThat(myClassLoaderListener.isOnUpdateCalled()).isTrue();
        assertThat(myClassLoaderListener.isOnDestroyCalled()).isFalse();
    }

    @Test
    public void should_stop_destroy_all_classloaders() throws Exception {
        //given
        classLoaderService.addListener(PROCESS.name(), CHILD_ID, myClassLoaderListener);
        classLoaderService.addListener(TENANT.name(), PARENT_ID, myClassLoaderListener);
        classLoaderService.getLocalClassLoader(PROCESS.name(), 125);
        classLoaderService.addListener(PROCESS.name(), 125, myClassLoaderListener);
        classLoaderService.getLocalClassLoader(PROCESS.name(), 126);
        classLoaderService.addListener(PROCESS.name(), 126, myClassLoaderListener);
        classLoaderService.getLocalClassLoader(PROCESS.name(), 127);
        classLoaderService.addListener(PROCESS.name(), 127, myClassLoaderListener);
        classLoaderService.getLocalClassLoader(PROCESS.name(), 128);
        classLoaderService.addListener(PROCESS.name(), 128, myClassLoaderListener);
        classLoaderService.getLocalClassLoader(PROCESS.name(), 129);
        classLoaderService.addListener(PROCESS.name(), 129, myClassLoaderListener);
        classLoaderService.getLocalClassLoader(PROCESS.name(), 130);
        classLoaderService.addListener(PROCESS.name(), 130, myClassLoaderListener);
        //when
        classLoaderService.stop();
        //then
        assertThat(myClassLoaderListener.getOnUpdateCalled()).isEqualTo(0);
        assertThat(myClassLoaderListener.getOnDestroyCalled()).isEqualTo(8);
    }

    @Test
    public void should_removeLocalClassLoader_call_destroy() throws Exception {
        //given
        classLoaderService.addListener(PROCESS.name(), CHILD_ID, myClassLoaderListener);
        classLoaderService.addListener(TENANT.name(), PARENT_ID, myClassLoaderListener);
        //when
        classLoaderService.removeLocalClassLoader(PROCESS.name(), CHILD_ID);

        //then
        assertThat(myClassLoaderListener.getOnDestroyCalled()).isEqualTo(1);
    }

    @Test(expected = SClassLoaderException.class)
    public void should_removeLocalClassLoader_throw_exception_if_parent_not_removed() throws Exception {
        //given
        classLoaderService.getLocalClassLoader(PROCESS.name(), 17);//second classloader
        //when
        classLoaderService.removeLocalClassLoader(PROCESS.name(), CHILD_ID);
        classLoaderService.removeLocalClassLoader(TENANT.name(), PARENT_ID);
    }

    @Test
    public void should_removeLocalClassLoader_work_if_remove_in_right_order() throws Exception {
        //given
        //when
        classLoaderService.removeLocalClassLoader(PROCESS.name(), CHILD_ID);
        classLoaderService.removeLocalClassLoader(TENANT.name(), PARENT_ID);
    }

    @Test
    public void should_getLocalClassLoader_create_expected_hierarchy() {
        //given
        VirtualClassLoader localClassLoader = classLoaderService.getLocalClassLoader(PROCESS.name(), CHILD_ID);
        //when

        assertThat(localClassLoader.getIdentifier()).isEqualTo(new ClassLoaderIdentifier(PROCESS.name(), CHILD_ID));
        ClassLoader parent = localClassLoader.getParent();
        assertThat(parent).isInstanceOf(VirtualClassLoader.class);
        assertThat(((VirtualClassLoader) parent).getIdentifier())
                .isEqualTo(new ClassLoaderIdentifier(TENANT.name(), PARENT_ID));
        ClassLoader global = parent.getParent();
        assertThat(global).isInstanceOf(VirtualClassLoader.class);
        assertThat(((VirtualClassLoader) global).getIdentifier()).isEqualTo(ClassLoaderIdentifier.GLOBAL);
        ClassLoader root = global.getParent();
        assertThat(root).isNotInstanceOf(VirtualClassLoader.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_create_throw_an_exception_if_bad_resolver() {
        //given
        classLoaderService = new ClassLoaderServiceImpl(badParentClassLoaderResolver, eventService,
                platformDependencyService, sessionAccessor, userTransactionService, broadcastService,
                classLoaderUpdater, Collections.emptyList());
        //when
        processClassLoader = classLoaderService.getLocalClassLoader(PROCESS.name(), CHILD_ID);

        //then exception

    }

    @Test
    public void should_globalListeners_be_called_on_destroy() throws Exception {
        //given
        classLoaderService.getLocalClassLoader(PROCESS.name(), 17);//second classloader
        //when
        classLoaderService.removeLocalClassLoader(PROCESS.name(), CHILD_ID);
        classLoaderService.removeLocalClassLoader(PROCESS.name(), 17);

        //then
        verify(platformClassLoaderListener1, times(2)).onDestroy(any(VirtualClassLoader.class));
        verify(platformClassLoaderListener2, times(2)).onDestroy(any(VirtualClassLoader.class));
    }

    @Test
    public void should_globalListeners_be_called_on_update() throws Exception {
        //given
        classLoaderService.getLocalClassLoader(PROCESS.name(), 17);//second classloader
        //when
        classLoaderService.refreshClassLoaderImmediately(PROCESS, CHILD_ID);
        classLoaderService.refreshClassLoaderImmediately(PROCESS, 17);

        //then
        verify(platformClassLoaderListener1, times(2)).onUpdate(any(VirtualClassLoader.class));
        verify(platformClassLoaderListener2, times(2)).onUpdate(any(VirtualClassLoader.class));
    }

    @Test
    public void should_refresh_classloader_after_transaction() throws Exception {
        doNothing().when(userTransactionService).registerBonitaSynchronization(synchronizationArgumentCaptor.capture());

        classLoaderService.refreshClassLoaderAfterUpdate(PROCESS, 42);

        assertThat(synchronizationArgumentCaptor.getValue()).isInstanceOf(RefreshClassloaderSynchronization.class);
    }

    @Test
    public void should_register_only_one_synchronization_when_refreshing_multiple_classloader_in_the_same_transaction()
            throws Exception {
        doNothing().when(userTransactionService).registerBonitaSynchronization(synchronizationArgumentCaptor.capture());

        classLoaderService.refreshClassLoaderAfterUpdate(PROCESS, 42);
        classLoaderService.refreshClassLoaderAfterUpdate(PROCESS, 42);
        classLoaderService.refreshClassLoaderAfterUpdate(PROCESS, 42);
        classLoaderService.refreshClassLoaderAfterUpdate(PROCESS, 42);

        assertThat(synchronizationArgumentCaptor.getAllValues()).hasSize(1);
    }

    @Test
    public void should_refresh_multiple_classloaders_after_transaction() throws Exception {
        doNothing().when(userTransactionService).registerBonitaSynchronization(synchronizationArgumentCaptor.capture());

        classLoaderService.refreshClassLoaderAfterUpdate(PROCESS, 41);
        classLoaderService.refreshClassLoaderAfterUpdate(PROCESS, 42);
        classLoaderService.refreshClassLoaderAfterUpdate(PROCESS, 43);
        classLoaderService.refreshClassLoaderAfterUpdate(PROCESS, 42);

        assertThat(synchronizationArgumentCaptor.getAllValues()).hasSize(1);
        assertThat(synchronizationArgumentCaptor.getValue().getIds())
                .containsExactlyInAnyOrder(pair(PROCESS, 41L), pair(PROCESS, 42L), pair(PROCESS, 43L));
    }

    @Test
    public void should_refresh_classloader_after_transaction_once_per_transaction() throws Exception {
        doNothing().when(userTransactionService).registerBonitaSynchronization(synchronizationArgumentCaptor.capture());

        classLoaderService.refreshClassLoaderAfterUpdate(PROCESS, 42);
        // Simulate an end + begin of a transaction:
        classLoaderService.removeRefreshClassLoaderSynchronization();
        classLoaderService.refreshClassLoaderAfterUpdate(PROCESS, 42);

        assertThat(synchronizationArgumentCaptor.getAllValues()).hasSize(2);
    }

    @Test
    public void should_only_warn_when_refreshing_classloader_on_not_existing_tenant() throws Exception {
        doReturn(55L).when(sessionAccessor).getTenantId();
        systemOutRule.clearLog();

        classLoaderService.refreshClassLoaderImmediately(TENANT, 55L);
        assertThat(systemOutRule.getLog()).contains("No dependency service is initialized");
    }

    @Test
    public void should_initialize_class_loader_when_getting_it() {
        VirtualClassLoader localClassLoader = classLoaderService.getLocalClassLoader(TENANT.name(), 43L);

        verify(classLoaderUpdater).initializeClassLoader(classLoaderService, localClassLoader,
                new ClassLoaderIdentifier(TENANT.name(), 43L));
    }

    @Test
    public void should_initialize_only_once_classloader() {
        doAnswer(invocation -> {
            VirtualClassLoader argument = invocation.getArgument(1);
            argument.replaceClassLoader(mock(BonitaClassLoader.class));
            return null;
        }).when(classLoaderUpdater).initializeClassLoader(any(), any(), any());

        VirtualClassLoader localClassLoader = classLoaderService.getLocalClassLoader(TENANT.name(), 43L);
        classLoaderService.getLocalClassLoader(TENANT.name(), 43L);

        verify(classLoaderUpdater, times(1)).initializeClassLoader(classLoaderService, localClassLoader,
                new ClassLoaderIdentifier(TENANT.name(), 43L));
    }

    @Test
    public void should_not_initialize_classloader_when_adding_and_removing_listener() {
        SingleClassLoaderListener singleClassLoaderListener = mock(SingleClassLoaderListener.class);

        assertThat(classLoaderService.addListener(TENANT.name(), 44L, singleClassLoaderListener)).isTrue();
        assertThat(classLoaderService.removeListener(TENANT.name(), 44L, singleClassLoaderListener)).isTrue();

        verify(classLoaderUpdater, never()).initializeClassLoader(any(), any(),
                eq(new ClassLoaderIdentifier(TENANT.name(), 44L)));
    }

}
