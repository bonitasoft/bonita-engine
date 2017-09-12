/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.engine.business.data.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.business.data.proxy.ServerLazyLoader;
import org.bonitasoft.engine.business.data.proxy.ServerProxyfier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Elias Ricken de Medeiros
 */
@RunWith(MockitoJUnitRunner.class)
public class BusinessDataReloaderTest {

    @Mock
    private BusinessDataRepository repository;

    @Mock
    private ServerLazyLoader lazyLoader;

    @InjectMocks
    private ServerProxyfier proxyfier;

    @InjectMocks
    private BusinessDataReloader reloader;

    @Test
    public void reloadEntity_should_call_findById_on_repository() throws Exception {
        //given
        long id = 5L;
        EntityPojo entity = new EntityPojo(id);
        EntityPojo entityUpdated = new EntityPojo(6L);
        given(repository.findById(EntityPojo.class, id)).willReturn(entityUpdated);

        //when
        Entity reloadedEntity = reloader.reloadEntity(entity);

        //then
        assertThat(reloadedEntity).isEqualTo(entityUpdated);

    }

    @Test
    public void reloadEntity_should_use_real_class_to_reload_entity() throws Exception {
        //given
        long id = 5L;
        EntityPojo proxyfiedEntityPojo = proxyfier.proxify(new EntityPojo(id));
        EntityPojo entityUpdated = new EntityPojo(6L);
        // use the real class here
        given(repository.findById(EntityPojo.class, id)).willReturn(entityUpdated);

        //when
        Entity reloadedEntity = reloader.reloadEntity(proxyfiedEntityPojo);

        //then
        assertThat(reloadedEntity).isEqualTo(entityUpdated);

    }

    @Test
    public void reloadEntitySoftly_should_return_entity_itself_when_id_is_not_set() throws Exception {
        //given
        EntityPojo entity = new EntityPojo(null);

        //when
        Entity reloadedEntity = reloader.reloadEntitySoftly(entity);

        //then
        assertThat(reloadedEntity).isEqualTo(entity);
        verifyZeroInteractions(repository);
    }

    @Test
    public void reloadEntitySoftly_should_reload_entity_id_is_set() throws Exception {
        //given
        long id = 2L;
        EntityPojo entity = new EntityPojo(id);
        EntityPojo entityUpdated = new EntityPojo(3L);

        given(repository.findById(EntityPojo.class, id)).willReturn(entityUpdated);

        //when
        Entity reloadedEntity = reloader.reloadEntitySoftly(entity);

        //then
        assertThat(reloadedEntity).isEqualTo(entityUpdated);
    }

    @Test
    public void reloadEntitySoftly_should_use_real_class_to_reload_entity() throws Exception {
        //given
        long id = 2L;
        EntityPojo proxyfiedEntity = new EntityPojo(id);
        EntityPojo entityUpdated = new EntityPojo(3L);

        //use real class
        given(repository.findById(EntityPojo.class, id)).willReturn(entityUpdated);

        //when
        Entity reloadedEntity = reloader.reloadEntitySoftly(proxyfiedEntity);

        //then
        assertThat(reloadedEntity).isEqualTo(entityUpdated);
    }

    @Test
    public void getEntityRealClass_should_handle_Bonita_proxy() throws Exception {
        final Class entityRealClass = reloader.getEntityRealClass(proxyfier.proxify(new EntityPojo(451L)));

        assertThat(entityRealClass).isEqualTo(EntityPojo.class);
    }

    @Test
    public void getEntityRealClass_should_handle_Hibernate_proxy() throws Exception {
        final Class entityRealClass = reloader.getEntityRealClass(new FakeHibernateProxyEntity());

        assertThat(entityRealClass).isEqualTo(EntityPojo.class);
    }

    @Test
    public void getEntityRealClass_should_handle_Hibernate_proxy_inside_Bonita_proxy() throws Exception {
        final Class entityRealClass = reloader.getEntityRealClass(proxyfier.proxify(new FakeHibernateProxyEntity()));

        assertThat(entityRealClass).isEqualTo(EntityPojo.class);
    }
}
