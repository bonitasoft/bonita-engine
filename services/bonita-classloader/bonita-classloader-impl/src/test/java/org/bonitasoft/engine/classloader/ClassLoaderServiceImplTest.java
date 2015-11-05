package org.bonitasoft.engine.classloader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.util.Collections;

import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class ClassLoaderServiceImplTest {

    @Mock
    private ParentClassLoaderResolver parentClassLoaderResolver;
    @Mock
    private TechnicalLoggerService logger;
    @Mock
    private EventService eventService;
    @Spy
    @InjectMocks
    private ClassLoaderServiceImpl classLoaderService;
    private ClassLoader testClassLoader;
    private VirtualClassLoader processClassLoader;
    private MyClassLoaderListener myClassLoaderListener;
    private VirtualClassLoader tenantClassLoader;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void before() throws Exception {
        processClassLoader = classLoaderService.getLocalClassLoader(ScopeType.PROCESS.name(), 12);
        tenantClassLoader = classLoaderService.getLocalClassLoader(ScopeType.TENANT.name(), 13);
        testClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(processClassLoader);
        myClassLoaderListener = new MyClassLoaderListener();
        temporaryFolder.create();
        final File file = temporaryFolder.newFolder();
        doReturn(file.toURI()).when(classLoaderService).getLocalTemporaryFolder(anyString(),anyLong());
    }

    @After
    public void after() {
        Thread.currentThread().setContextClassLoader(testClassLoader);
    }


    @Test
    public void should_addListener_add_on_specified_classloader_do_not_call_on_others() throws Exception {
        //given
        classLoaderService.addListener(ScopeType.TENANT.name(), 13, myClassLoaderListener);
        //when
        processClassLoader.destroy();
        //then
        assertThat(myClassLoaderListener.isOnDestroyCalled()).isFalse();
    }

    @Test
    public void should_addListener_add_on_specified_classloader_call_listener() throws Exception {
        //given
        classLoaderService.addListener(ScopeType.TENANT.name(), 13, myClassLoaderListener);
        //when
        tenantClassLoader.destroy();
        //then
        assertThat(myClassLoaderListener.isOnDestroyCalled()).isTrue();
    }

    @Test
    public void should_removeListener_remove_the_listener() throws Exception {
        //given
        classLoaderService.addListener(ScopeType.TENANT.name(), 13, myClassLoaderListener);
        classLoaderService.removeListener(ScopeType.TENANT.name(), 13, myClassLoaderListener);
        //when
        processClassLoader.destroy();
        //then
        assertThat(myClassLoaderListener.isOnDestroyCalled()).isFalse();

    }

    @Test
    public void should_refreshClassLoader_call_replace_classloader() throws Exception {
        //given
        classLoaderService.addListener(ScopeType.PROCESS.name(), 12, myClassLoaderListener);
        //when
        classLoaderService.refreshLocalClassLoader(ScopeType.PROCESS.name(), 12, Collections.<String, byte[]> emptyMap());
        //then
        assertThat(myClassLoaderListener.isOnUpdateCalled()).isTrue();
        assertThat(myClassLoaderListener.isOnDestroyCalled()).isFalse();
    }

    @Test
    public void should_stop_destroy_all_classloaders() throws Exception {
        //given
        classLoaderService.addListener(ScopeType.PROCESS.name(), 12, myClassLoaderListener);
        classLoaderService.addListener(ScopeType.TENANT.name(), 13, myClassLoaderListener);
        //when
        classLoaderService.stop();
        //then
        assertThat(myClassLoaderListener.getOnUpdateCalled()).isEqualTo(0);
        assertThat(myClassLoaderListener.getOnDestroyCalled()).isEqualTo(2);
    }

    @Test
    public void should_removeLocalClassLoader_call_destroy() throws Exception {
        //given
        classLoaderService.addListener(ScopeType.PROCESS.name(), 12, myClassLoaderListener);
        classLoaderService.addListener(ScopeType.TENANT.name(), 13, myClassLoaderListener);
        //when
        classLoaderService.removeLocalClassLoader(ScopeType.PROCESS.name(), 12);

        //then
        assertThat(myClassLoaderListener.getOnDestroyCalled()).isEqualTo(1);
    }

}
