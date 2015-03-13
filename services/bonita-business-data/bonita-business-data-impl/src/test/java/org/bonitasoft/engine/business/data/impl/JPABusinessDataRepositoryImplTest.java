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
package org.bonitasoft.engine.business.data.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.persistence.EntityManager;

import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.business.data.BusinessDataModelRepository;
import org.bonitasoft.engine.business.data.SBusinessDataNotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class JPABusinessDataRepositoryImplTest {

    private static final long PRIMARY_KEY_1 = 1L;

    private JPABusinessDataRepositoryImpl repository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private BusinessDataModelRepository businessDataModelRepository;

    @Mock
    private Map<String, Object> configuration;

    @Mock
    EntityManager manager;

    @Before
    public void setUp() {
        repository = spy(new JPABusinessDataRepositoryImpl(transactionService, businessDataModelRepository, configuration));
        doReturn(manager).when(repository).getEntityManager();
    }

    @Test
    public void findById_should_not_detach_entities() throws Exception {
        final Address address1 = new Address(PRIMARY_KEY_1);
        when(manager.find(Address.class, PRIMARY_KEY_1)).thenReturn(address1);

        final Address result = repository.findById(Address.class, PRIMARY_KEY_1);

        assertThat(result).isEqualTo(address1);
        verify(manager, never()).detach(any(Address.class));
    }

    @Test(expected = SBusinessDataNotFoundException.class)
    public void findById_should_throw_an_exception_when_not_found() throws Exception {
        when(manager.find(Address.class, PRIMARY_KEY_1)).thenReturn(null);

        repository.findById(Address.class, PRIMARY_KEY_1);
    }

    @Test(expected = SBusinessDataNotFoundException.class)
    public void findById_should_throw_an_exception_with_a_null_identifier() throws Exception {
        repository.findById(Address.class, null);
    }


    class Address implements Entity {

        private static final long serialVersionUID = 2603989953326533907L;

        private final long persistenceId;

        public Address(final long persistenceId) {
            this.persistenceId = persistenceId;
        }

        @Override
        public Long getPersistenceId() {
            return persistenceId;
        }

        @Override
        public Long getPersistenceVersion() {
            return 0L;
        }

    }

}
