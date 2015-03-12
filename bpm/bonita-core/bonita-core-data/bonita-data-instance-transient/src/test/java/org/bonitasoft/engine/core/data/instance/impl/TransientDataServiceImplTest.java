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
package org.bonitasoft.engine.core.data.instance.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.impl.SShortTextDataInstanceImpl;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransientDataServiceImplTest {

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private TransientDataServiceImpl transientDataServiceImpl;

    @Before
    public void before() {
        when(cacheService.getCachesNames()).thenReturn(Arrays.asList("transient_data"));
    }

    @Test
    public void should_createDataInstance_add_in_cache() throws Exception {
        // given
        SShortTextDataInstanceImpl data = createData(12, 42, "name", "containerType");

        // when
        transientDataServiceImpl.createDataInstance(data);

        // then
        assertThat(data.getId()).isGreaterThan(0);
        verify(cacheService, times(1)).store("transient_data", "name:42:containerType", data);
    }

    private SShortTextDataInstanceImpl createData(final long id, final int containerId, final String name, final String containerType) throws SCacheException {
        SShortTextDataInstanceImpl data = new SShortTextDataInstanceImpl();
        data.setId(id);
        data.setName(name);
        data.setContainerId(containerId);
        data.setContainerType(containerType);
        data.setValue("A value");
        when(cacheService.get("transient_data", name + ":" + containerId + ":" + containerType)).thenReturn(data);
        return data;
    }

    @Test
    public void testUpdateDataInstance() throws Exception {
        // given
        SShortTextDataInstanceImpl data = createData(12, 42, "name", "ctype");
        when(cacheService.getKeys("transient_data")).thenReturn(Arrays.asList((Object) "name:42:ctype"));

        // when
        EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        entityUpdateDescriptor.addField("value", "newValue");
        transientDataServiceImpl.updateDataInstance(data, entityUpdateDescriptor);

        // then
        assertThat(transientDataServiceImpl.getDataInstance(12).getValue()).isEqualTo("newValue");
        verify(cacheService, times(1)).store("transient_data", "name:42:ctype", data);
    }

    @Test
    public void testDeleteDataInstance() throws Exception {
        SShortTextDataInstanceImpl data = createData(12, 42, "name", "ctype");

        transientDataServiceImpl.deleteDataInstance(data);

        verify(cacheService).remove("transient_data", "name:42:ctype");
    }

    @Test
    public void should_getDataInstanceById_return_the_data() throws Exception {
        // given
        SShortTextDataInstanceImpl data = createData(12, 42, "name", "ctype");
        when(cacheService.getKeys("transient_data")).thenReturn(Arrays.asList((Object) "name:42:ctype"));

        // when
        SDataInstance result = transientDataServiceImpl.getDataInstance(12);

        // then
        assertThat(result).isEqualTo(data);
    }

    @Test
    public void testGetDataInstanceStringLongString() throws Exception {
        // given
        SShortTextDataInstanceImpl data = createData(12, 42, "name", "ctype");
        when(cacheService.getKeys("transient_data")).thenReturn(Arrays.asList((Object) "name:42:ctype"));

        // when
        SDataInstance result = transientDataServiceImpl.getDataInstance("name", 42, "ctype");

        // then
        assertThat(result).isEqualTo(data);
    }

    @Test
    public void testGetDataInstancesLongStringIntInt() throws Exception {
        SShortTextDataInstanceImpl data = createData(12, 42, "name", "ctype");
        when(cacheService.getKeys("transient_data")).thenReturn(Arrays.asList((Object) "name:42:ctype"));

        List<SDataInstance> dataInstances = transientDataServiceImpl.getDataInstances(42, "ctype", 0, 10);

        assertThat(dataInstances.size()).isEqualTo(1);
        assertThat(dataInstances.get(0)).isEqualTo(data);
    }

}
