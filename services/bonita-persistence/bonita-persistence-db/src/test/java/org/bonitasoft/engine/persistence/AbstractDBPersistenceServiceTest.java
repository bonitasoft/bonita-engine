package org.bonitasoft.engine.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sequence.SequenceManager;
import org.bonitasoft.engine.services.SPersistenceException;
import org.bonitasoft.engine.services.UpdateDescriptor;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.junit.Test;


public class AbstractDBPersistenceServiceTest {

    class DummyDBPersistenceService extends AbstractDBPersistenceService {

        public DummyDBPersistenceService(final String name, final DBConfigurationsProvider dbConfigurationsProvider, final String statementDelimiter, final String likeEscapeCharacter, final SequenceManager sequenceManager,
                final DataSource datasource, final boolean enableWordSearch, final Set<String> wordSearchExclusionMappings, final TechnicalLoggerService logger) throws ClassNotFoundException {
            super(name, dbConfigurationsProvider, statementDelimiter, likeEscapeCharacter, sequenceManager, datasource, enableWordSearch, wordSearchExclusionMappings, logger);
            // TODO Auto-generated constructor stub
        }

        @Override
        public <T> T selectOne(final SelectOneDescriptor<T> selectDescriptor) throws SBonitaReadException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <T> List<T> selectList(final SelectListDescriptor<T> selectDescriptor) throws SBonitaReadException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public <T extends PersistentObject> T selectById(final SelectByIdDescriptor<T> selectDescriptor) throws SBonitaReadException {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void update(final UpdateDescriptor desc) throws SPersistenceException {
            // TODO Auto-generated method stub

        }

        @Override
        public void purge(final String classToPurge) throws SPersistenceException {
            // TODO Auto-generated method stub

        }

        @Override
        public void purge() throws SPersistenceException {
            // TODO Auto-generated method stub

        }

        @Override
        public void insertInBatch(final List<PersistentObject> entities) throws SPersistenceException {
            // TODO Auto-generated method stub

        }

        @Override
        public void insert(final PersistentObject entity) throws SPersistenceException {
            // TODO Auto-generated method stub

        }

        @Override
        public void flushStatements() throws SPersistenceException {
            // TODO Auto-generated method stub

        }

        @Override
        public void deleteByTenant(final Class<? extends PersistentObject> entityClass, final List<FilterOption> filters) throws SPersistenceException {
            // TODO Auto-generated method stub

        }

        @Override
        public void deleteAll(final Class<? extends PersistentObject> entityClass) throws SPersistenceException {
            // TODO Auto-generated method stub

        }

        @Override
        public void delete(final List<Long> ids, final Class<? extends PersistentObject> entityClass) throws SPersistenceException {
            // TODO Auto-generated method stub

        }

        @Override
        public void delete(final long id, final Class<? extends PersistentObject> entityClass) throws SPersistenceException {
            // TODO Auto-generated method stub

        }

        @Override
        public void delete(final PersistentObject entity) throws SPersistenceException {
            // TODO Auto-generated method stub

        }

        @Override
        protected long getTenantId() throws STenantIdNotSetException {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        protected void doExecuteSQL(final String sqlResource, final String statementDelimiter, final Map<String, String> replacements, final boolean useDataSourceConnection) throws SPersistenceException, IOException {
            // TODO Auto-generated method stub

        }
    }


    @Test
    public void should_word_search_is_disable_when_entity_class_is_null() throws Exception {
        boolean enableWordSearch = true;
        Set<String> wordSearchExclusionMappings = Collections.<String>emptySet();
        Class<? extends PersistentObject> entityClass = null;
        final boolean expectedResult = false;

        executeIsWordSearchEnabled(enableWordSearch, wordSearchExclusionMappings, entityClass, expectedResult);
    }


    private void executeIsWordSearchEnabled(final boolean enableWordSearch, final Set<String> wordSearchExclusionMappings, final Class<? extends PersistentObject> entityClass, final boolean expectedResult)
            throws ClassNotFoundException {
        DBConfigurationsProvider dbConfigurationsProvider = mock(DBConfigurationsProvider.class);
        SequenceManager sequenceManager = mock(SequenceManager.class);
        DataSource datasource = mock(DataSource.class);
        TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        AbstractDBPersistenceService persistenceService = new DummyDBPersistenceService("name", dbConfigurationsProvider, ";", "#", sequenceManager, datasource, enableWordSearch, wordSearchExclusionMappings, logger);

        assertThat(persistenceService.isWordSearchEnabled(entityClass), is(expectedResult));
    }
}
