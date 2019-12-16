/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.persistence;

import static org.mockito.Mockito.*;

import java.util.Set;
import javax.sql.DataSource;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sequence.SequenceManager;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 * @version 6.4.0
 * @since 6.4.0
 */
@SuppressWarnings("ALL")
@RunWith(MockitoJUnitRunner.class)
public class PlatformHibernatePersistenceServiceTest {

    @Mock
    private DataSource datasource;

    private final boolean enableWordSearch = true;

    @Mock
    private HibernateConfigurationProvider hbmConfigurationProvider;

    private final char likeEscapeCharacter = ' ';

    @Mock
    private TechnicalLoggerService logger;

    private final String name = "PlatformHibernatePersistenceService";

    @Mock
    private SequenceManager sequenceManager;

    @Mock
    Query<?> query;

    private Set<String> wordSearchExclusionMappings;

    private PlatformHibernatePersistenceService platformHibernatePersistenceService;

    @Before
    public void before() throws ConfigurationException {
        doReturn(mock(SessionFactory.class)).when(hbmConfigurationProvider).getSessionFactory();
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.persistence.AbstractHibernatePersistenceService#selectList(org.bonitasoft.engine.persistence.SelectListDescriptor)}.
     */
    @Test
    public final void checkOrderByClause_should_do_nothing_when_no_ORDER_BY_clause_in_query_and_checking_mode_is_empty()
            throws Exception {
        // Given
        System.setProperty("sysprop.bonita.orderby.checking.mode", "");

        platformHibernatePersistenceService = spy(
                new PlatformHibernatePersistenceService(name, hbmConfigurationProvider, null,
                        likeEscapeCharacter, logger, sequenceManager, datasource, enableWordSearch,
                        wordSearchExclusionMappings));
        doReturn("").when(query).getQueryString();

        // When
        platformHibernatePersistenceService.checkOrderByClause(query);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.persistence.AbstractHibernatePersistenceService#selectList(org.bonitasoft.engine.persistence.SelectListDescriptor)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public final void checkOrderByClause_should_throw_exception_when_no_ORDER_BY_clause_in_query_and_checking_mode_is_STRICT()
            throws Exception {
        // Given
        System.setProperty("sysprop.bonita.orderby.checking.mode", OrderByCheckingMode.STRICT.name());

        platformHibernatePersistenceService = spy(
                new PlatformHibernatePersistenceService(name, hbmConfigurationProvider, null,
                        likeEscapeCharacter, logger, sequenceManager, datasource, enableWordSearch,
                        wordSearchExclusionMappings));
        doReturn("").when(query).getQueryString();

        // When
        platformHibernatePersistenceService.checkOrderByClause(query);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.persistence.AbstractHibernatePersistenceService#selectList(org.bonitasoft.engine.persistence.SelectListDescriptor)}.
     */
    @Test
    public final void checkOrderByClause_should_do_nothing_when_no_ORDER_BY_clause_in_query_and_no_checking_mode()
            throws Exception {
        // Given
        System.clearProperty("sysprop.bonita.orderby.checking.mode");

        platformHibernatePersistenceService = spy(
                new PlatformHibernatePersistenceService(name, hbmConfigurationProvider, null,
                        likeEscapeCharacter, logger, sequenceManager, datasource, enableWordSearch,
                        wordSearchExclusionMappings));
        doReturn("").when(query).getQueryString();

        // When
        platformHibernatePersistenceService.checkOrderByClause(query);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.persistence.AbstractHibernatePersistenceService#selectList(org.bonitasoft.engine.persistence.SelectListDescriptor)}.
     */
    @Test
    public final void checkOrderByClause_should_do_nothing_when_no_ORDER_BY_clause_in_query_and_checking_mode_is_NONE()
            throws Exception {
        // Given
        System.setProperty("sysprop.bonita.orderby.checking.mode", OrderByCheckingMode.NONE.name());

        platformHibernatePersistenceService = spy(
                new PlatformHibernatePersistenceService(name, hbmConfigurationProvider, null,
                        likeEscapeCharacter, logger, sequenceManager, datasource, enableWordSearch,
                        wordSearchExclusionMappings));
        doReturn("").when(query).getQueryString();

        // When
        platformHibernatePersistenceService.checkOrderByClause(query);
    }

    @Test
    public final void checkOrderByClause_should_log_when_no_ORDER_BY_clause_in_query_and_checking_mode_is_WARNING()
            throws Exception {
        // Given
        System.setProperty("sysprop.bonita.orderby.checking.mode", OrderByCheckingMode.WARNING.name());

        platformHibernatePersistenceService = new PlatformHibernatePersistenceService(name, hbmConfigurationProvider,
                null,
                likeEscapeCharacter, logger, sequenceManager, datasource, enableWordSearch,
                wordSearchExclusionMappings);
        doReturn("").when(query).getQueryString();

        // When
        platformHibernatePersistenceService.checkOrderByClause(query);

        // Then
        verify(logger).log(AbstractHibernatePersistenceService.class,
                TechnicalLogSeverity.WARNING,
                "Query '' does not contain 'ORDER BY' clause. It's better to modify your query to order the result, especially if you use the pagination.");
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.persistence.AbstractHibernatePersistenceService#selectList(org.bonitasoft.engine.persistence.SelectListDescriptor)}.
     */
    @Test
    public final void checkOrderByClause_should_do_nothing_when_ORDER_BY_clause_in_query_and_checking_mode_is_STRICT()
            throws Exception {
        // Given
        final Query query = mock(Query.class);
        doReturn("Order by").when(query).getQueryString();
        //        doReturn(query).when(session).getNamedQuery(nullable(String.class));
        System.setProperty("sysprop.bonita.orderby.checking.mode", OrderByCheckingMode.STRICT.name());

        platformHibernatePersistenceService = spy(
                new PlatformHibernatePersistenceService(name, hbmConfigurationProvider, null,
                        likeEscapeCharacter, logger, sequenceManager, datasource, enableWordSearch,
                        wordSearchExclusionMappings));

        // When
        platformHibernatePersistenceService.checkOrderByClause(query);
    }

}
