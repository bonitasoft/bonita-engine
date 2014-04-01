/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.data.DataServiceImpl;
import org.bonitasoft.engine.data.DataSourceConfiguration;
import org.bonitasoft.engine.data.DataSourceImplementation;
import org.bonitasoft.engine.data.SDataException;
import org.bonitasoft.engine.data.SDataSourceAlreadyExistException;
import org.bonitasoft.engine.data.SDataSourceInactiveException;
import org.bonitasoft.engine.data.SDataSourceInitializationException;
import org.bonitasoft.engine.data.SDataSourceNotFoundException;
import org.bonitasoft.engine.data.model.SDataSource;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.services.QueriableLoggerService;

public class DataServiceCachedImpl extends DataServiceImpl {

    // not using synchronized map as data is never changing.
    // We prefer having fast reads and synchronized only writes (happen only a few times during the warm up)
    private final Map<Long, SDataSource> sDataSources = new HashMap<Long, SDataSource>();

    private final Map<String, SDataSource> sDataSourcesByNameAndVersion = new HashMap<String, SDataSource>();

    private final Map<String, DataSourceImplementation> sDataSourceImplementations = new HashMap<String, DataSourceImplementation>();

    public DataServiceCachedImpl(final Recorder recorder, final ReadPersistenceService persistenceService,
            final ClassLoaderService classLoaderService, final List<DataSourceConfiguration> dataSourceConfigurations,
            final TechnicalLoggerService logger, final QueriableLoggerService queriableLoggerService) {
        super(recorder, persistenceService, classLoaderService, dataSourceConfigurations, logger, queriableLoggerService);
    }

    private String getKeyFromDataSourceNameAndVersion(final String name, final String version) {
        final String key = name + "--" + version;
        return key;
    }

    private String getKeyFromDataSourceTypeAndId(final Class<?> clazz, final long dataSourceId) {
        final String key = clazz.getName() + "--" + dataSourceId;
        return key;
    }

    @Override
    public SDataSource getDataSource(final long dataSourceId) throws SDataSourceNotFoundException {
        if (sDataSources.containsKey(dataSourceId)) {
            return sDataSources.get(dataSourceId);
        }
        final SDataSource sDataSource = super.getDataSource(dataSourceId);
        synchronized (this) {
            sDataSources.put(dataSourceId, sDataSource);
            sDataSourcesByNameAndVersion.put(getKeyFromDataSourceNameAndVersion(sDataSource.getName(), sDataSource.getVersion()), sDataSource);
        }
        return sDataSource;
    }

    @Override
    public SDataSource getDataSource(final String name, final String version) throws SDataSourceNotFoundException {
        final String key = getKeyFromDataSourceNameAndVersion(name, version);
        if (sDataSourcesByNameAndVersion.containsKey(key)) {
            return sDataSourcesByNameAndVersion.get(key);
        }
        final SDataSource sDataSource = super.getDataSource(name, version);
        synchronized (this) {
            sDataSourcesByNameAndVersion.put(key, sDataSource);
            sDataSources.put(sDataSource.getId(), sDataSource);
        }
        return sDataSource;
    }

    @Override
    public void removeDataSource(final long dataSourceId) throws SDataSourceNotFoundException {
        final SDataSource sDataSource = getDataSource(dataSourceId);
        super.removeDataSource(dataSourceId);
        synchronized (this) {
            sDataSources.remove(dataSourceId);
            sDataSourcesByNameAndVersion.remove(getKeyFromDataSourceNameAndVersion(sDataSource.getName(), sDataSource.getVersion()));
        }
    }

    @Override
    public void removeDataSource(final SDataSource sDataSource) throws SDataSourceNotFoundException {
        super.removeDataSource(sDataSource);
        synchronized (this) {
            sDataSources.remove(sDataSource.getId());
            sDataSourcesByNameAndVersion.remove(getKeyFromDataSourceNameAndVersion(sDataSource.getName(), sDataSource.getVersion()));
        }
    }

    @Override
    public void createDataSource(final SDataSource sDataSource)
            throws SDataSourceAlreadyExistException, SDataException {
        super.createDataSource(sDataSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends DataSourceImplementation> T getDataSourceImplementation(final Class<T> dataSourceType, final long dataSourceId)
            throws SDataSourceNotFoundException, SDataSourceInitializationException, SDataSourceInactiveException {
        final String key = getKeyFromDataSourceTypeAndId(dataSourceType, dataSourceId);
        if (sDataSourceImplementations.containsKey(key)) {
            return (T) sDataSourceImplementations.get(key);
        }
        final T dataSourceImplementation = super.getDataSourceImplementation(dataSourceType, dataSourceId);
        synchronized (this) {
            sDataSourceImplementations.put(key, dataSourceImplementation);
        }
        return dataSourceImplementation;
    }
}
