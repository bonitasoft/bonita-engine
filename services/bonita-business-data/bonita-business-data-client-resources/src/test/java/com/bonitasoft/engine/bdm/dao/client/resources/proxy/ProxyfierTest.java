package com.bonitasoft.engine.bdm.dao.client.resources.proxy;

import static com.bonitasoft.engine.bdm.proxy.assertion.ProxyAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.bonita.pojo.AddressForTesting;
import org.bonita.pojo.EmployeeForTesting;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.bdm.proxy.model.TestEntity;

@RunWith(MockitoJUnitRunner.class)
public class ProxyfierTest {

    @Mock
    private LazyLoader lazyLoader;

    @InjectMocks
    private Proxyfier proxyfier;

    @Test
    public void should_proxify_an_entity() throws Exception {
        final TestEntity entity = new TestEntity();

        final TestEntity proxy = proxyfier.proxify(entity);

        assertThat(proxy).isAProxy();
    }

    @Test
    public void should_proxify_a_list_of_entities() throws Exception {
        final List<TestEntity> entities = Arrays.asList(new TestEntity(),
                new TestEntity());

        final List<TestEntity> proxies = proxyfier.proxify(entities);

        for (final TestEntity entity : proxies) {
            assertThat(entity).isAProxy();
        }
    }

    @Test
    public void shouldProxyfier_retrieve_list_setter_when_lazyLoader_returns_arraylist() throws Exception {

        //given
        final AddressForTesting address1 = new AddressForTesting();
        final AddressForTesting address2 = new AddressForTesting();
        final List<AddressForTesting> addresses = new ArrayList<AddressForTesting>();
        addresses.add(address1);
        addresses.add(address2);
        doReturn(addresses).when(lazyLoader).load(any(Method.class), anyLong());

        //when
        final long persistenceId = 1L;
        final EmployeeForTesting employee = new EmployeeForTesting();
        employee.setPersistenceId(persistenceId);
        final EmployeeForTesting proxify = proxyfier.proxify(employee);

        //then
        final List<?> lazyAddresses = (List<?>) proxify.getClass().getMethod("getAddresses", new Class[0]).invoke(proxify);
        assertThat(proxify).isAProxy();
        Assertions.assertThat(lazyAddresses).hasSize(2);

    }
}
