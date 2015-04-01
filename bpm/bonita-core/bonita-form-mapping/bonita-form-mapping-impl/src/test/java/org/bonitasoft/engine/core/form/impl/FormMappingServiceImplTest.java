/*
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
 */
package org.bonitasoft.engine.core.form.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.bonitasoft.engine.core.form.SFormMapping;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class FormMappingServiceImplTest {

    @Mock
    private Recorder recorder;
    @Mock
    private ReadPersistenceService persistenceService;
    @Mock
    private SessionService sessionService;
    @Mock
    private ReadSessionAccessor sessionAccessor;

    @InjectMocks
    private FormMappingServiceImpl formMappingService;

    @Test
    public void testCreate() throws Exception {

    }

    @Test
    public void testUpdate() throws Exception {

    }

    @Test
    public void testDelete() throws Exception {

    }

    @Test
    public void testGetNumberOfFormMappings() throws Exception {
        QueryOptions queryOptions = mock(QueryOptions.class);
        formMappingService.getNumberOfFormMappings(queryOptions);

        verify(persistenceService).getNumberOfEntities(SFormMapping.class, queryOptions, Collections.<String, Object> emptyMap());
    }

    @Test
    public void testSearchFormMappings() throws Exception {
        QueryOptions queryOptions = mock(QueryOptions.class);
        formMappingService.searchFormMappings(queryOptions);

        verify(persistenceService).searchEntity(SFormMapping.class, queryOptions, Collections.<String, Object> emptyMap());
    }
}
