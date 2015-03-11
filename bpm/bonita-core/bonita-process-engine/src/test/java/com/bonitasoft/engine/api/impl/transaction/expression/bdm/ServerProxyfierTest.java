/*******************************************************************************
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.expression.bdm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.bdm.Entity;

/**
 * @author Romain Bioteau
 * @author Laurent Leseigneur
 */
@RunWith(MockitoJUnitRunner.class)
public class ServerProxyfierTest {

    @Mock
    private ServerLazyLoader lazyLoader;
    private ServerProxyfier serverProxyfier;

    @Before
    public void setUp() throws Exception {
        serverProxyfier = new ServerProxyfier(lazyLoader);
    }

    @Test
    public void should_proxify_an_entity() throws Exception {
        PersonEntity proxy = serverProxyfier.proxify(new PersonEntity());

        assertThat(proxy).isInstanceOf(ProxyObject.class);
        assertThat(proxy.getClass().getSuperclass()).isEqualTo(PersonEntity.class);
    }

    @Test
    public void should_not_reproxify_a_server_proxy() throws Exception {
        PersonEntity originalProxy = serverProxyfier.proxify(new PersonEntity());
        PersonEntity proxy = serverProxyfier.proxify(originalProxy);

        assertThat(proxy).isSameAs(originalProxy);
    }

    @Test
    public void should_reproxify_an_hibernate_proxy() throws Exception {
        final ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(PersonEntity.class);
        Entity aProxy = (Entity) factory.create(new Class[0], new Object[0], new MethodHandler() {

            @Override
            public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                return null;
            }
        });
        PersonEntity proxy = (PersonEntity) serverProxyfier.proxify(aProxy);

        assertThat(proxy).isNotSameAs(aProxy);
    }

    @Test
    public void should_call_on_lazy_loaded_getter_use_lazyLoader() throws Exception {
        //given
        PersonEntity personEntity = new PersonEntity();

        final Method method = PersonEntity.class.getMethod("getWithLazyLoadedAnnotation");
        doReturn("lazyResult").when(lazyLoader).load(any(Method.class), anyLong());

        //when
        PersonEntity proxy = serverProxyfier.proxify(personEntity);
        final String withLazyLoadedAnnotation = proxy.getWithLazyLoadedAnnotation();

        //
        verify(lazyLoader).load(method, personEntity.getPersistenceId());
        assertThat(withLazyLoadedAnnotation).isEqualTo("lazyResult");
    }

    @Test
    public void should_not_call_lazyLoader() throws Exception {
        //given
        PersonEntity personEntity = new PersonEntity();
        final Method method = PersonEntity.class.getMethod("getWithoutLazyLoadedAnnotation");

        //when
        PersonEntity proxy = serverProxyfier.proxify(personEntity);
        final String withLazyLoadedAnnotation = proxy.getWithoutLazyLoadedAnnotation();

        //
        verify(lazyLoader, never()).load(method, personEntity.getPersistenceId());
        assertThat(withLazyLoadedAnnotation).isEqualTo("getWithoutLazyLoadedAnnotation");
    }

}
