/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.bonitasoft.engine.data;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.data.model.SDataSource;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Celine Souchet
 */
public class DataServiceImplTest {

    private ClassLoaderService classLoaderService;

    private List<DataSourceConfiguration> dataSourceConfigurations;

    private Recorder recorder;

    private ReadPersistenceService persistence;

    private TechnicalLoggerService logger;

    private QueriableLoggerService queriableLoggerService;

    private DataServiceImpl dataServiceImpl;

    @Before
    public void setUp() {
        classLoaderService = mock(ClassLoaderService.class);
        dataSourceConfigurations = new ArrayList<DataSourceConfiguration>();
        recorder = mock(Recorder.class);
        persistence = mock(ReadPersistenceService.class);
        logger = mock(TechnicalLoggerService.class);
        queriableLoggerService = mock(QueriableLoggerService.class);
        dataServiceImpl = new DataServiceImpl(recorder, persistence, classLoaderService, dataSourceConfigurations, logger, queriableLoggerService);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.DataServiceImpl#getDataSource(long)}.
     * 
     * @throws SDataSourceNotFoundException
     * @throws SBonitaReadException
     */
    @Test
    public final void getDataSourceById() throws SDataSourceNotFoundException, SBonitaReadException {
        final SDataSource sDataSource = mock(SDataSource.class);
        when(persistence.selectById(any(SelectByIdDescriptor.class))).thenReturn(sDataSource);

        Assert.assertEquals(sDataSource, dataServiceImpl.getDataSource(456L));
    }

    @Test(expected = SDataSourceNotFoundException.class)
    public final void getDataSourceByIdNotExists() throws SBonitaReadException, SDataSourceNotFoundException {
        when(persistence.selectById(any(SelectByIdDescriptor.class))).thenReturn(null);

        dataServiceImpl.getDataSource(456L);
    }

    @Test(expected = SDataSourceNotFoundException.class)
    public final void getByIdThrowException() throws SBonitaReadException, SDataSourceNotFoundException {
        when(persistence.selectById(any(SelectByIdDescriptor.class))).thenThrow(new SBonitaReadException(""));

        dataServiceImpl.getDataSource(456L);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.data.DataServiceImpl#getDataSource(java.lang.String, java.lang.String)}.
     * 
     * @throws SBonitaReadException
     * @throws SDataSourceNotFoundException
     */
    @Test
    public final void getDataSourceByNameAndVersion() throws SBonitaReadException, SDataSourceNotFoundException {
        final SDataSource sDataSource = mock(SDataSource.class);
        when(persistence.selectOne(any(SelectOneDescriptor.class))).thenReturn(sDataSource);

        Assert.assertEquals(sDataSource, dataServiceImpl.getDataSource("name", "version"));
    }

    @Test(expected = SDataSourceNotFoundException.class)
    public final void getDataSourceByNameAndVersionNotExists() throws SBonitaReadException, SDataSourceNotFoundException {
        when(persistence.selectOne(any(SelectOneDescriptor.class))).thenReturn(null);

        dataServiceImpl.getDataSource("name", "version");
    }

    @Test(expected = SDataSourceNotFoundException.class)
    public final void getDataSourceByNameAndVersionThrowException() throws SBonitaReadException, SDataSourceNotFoundException {
        when(persistence.selectOne(any(SelectOneDescriptor.class))).thenThrow(new SBonitaReadException(""));

        dataServiceImpl.getDataSource("name", "version");
    }

}
