package com.bonitasoft.engine.bdm.dao.proxy;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.bdm.dao.proxy.LazyLoader;
import com.bonitasoft.engine.bdm.dao.proxy.Proxyfier;
import com.bonitasoft.engine.bdm.proxy.assertion.ProxyAssert;
import com.bonitasoft.engine.bdm.proxy.model.TestEntity;

@RunWith(MockitoJUnitRunner.class)
public class ProxyfierTest {

	@Mock
	private LazyLoader lazyLoader;

	@InjectMocks
	private Proxyfier proxyfier;

	@Test
	public void should_proxify_an_entity() throws Exception {
		TestEntity entity = new TestEntity();

		TestEntity proxy = proxyfier.proxify(entity);

		ProxyAssert.assertThat(proxy).isAProxy();
	}

	@Test
	public void should_proxify_a_list_of_entities() throws Exception {
		List<TestEntity> entities = Arrays.asList(new TestEntity(),
				new TestEntity());

		List<TestEntity> proxies = proxyfier.proxify(entities);

		for (TestEntity entity : proxies) {
			ProxyAssert.assertThat(entity).isAProxy();
		}
	}
}
