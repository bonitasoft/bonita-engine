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

import javax.sql.DataSource;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sequence.SequenceManager;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
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
@RunWith(MockitoJUnitRunner.class)
public class TenantHibernatePersistenceServiceTest {

    @Mock
    private DataSource datasource;

    @Mock
    private HibernateConfigurationProvider hbmConfigurationProvider;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private SequenceManager sequenceManager;

    @Mock
    private ReadSessionAccessor sessionAccessor;

    @Mock
    Query<?> query;

    private HibernateMetricsBinder metricsBinder = (sessionFactory) -> {
    };

    private TenantHibernatePersistenceService tenantHibernatePersistenceService;

    @Before
    public void before() throws Exception {
        doReturn(mock(SessionFactory.class)).when(hbmConfigurationProvider).getSessionFactory();
        tenantHibernatePersistenceService = spy(
                new TenantHibernatePersistenceService("TenantHibernatePersistenceService", sessionAccessor, hbmConfigurationProvider, null,
                        ' ', logger, sequenceManager, datasource,
                        true, null, metricsBinder));
    }

    @Test
    public final void checkOrderByClause_should_do_nothing_when_no_ORDER_BY_clause_in_query_and_checking_mode_is_empty() {
        // Given
        System.setProperty("sysprop.bonita.orderby.checking.mode", "");
        doReturn("").when(query).getQueryString();

        // When
        tenantHibernatePersistenceService.checkOrderByClause(query);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void checkOrderByClause_should_throw_exception_when_no_ORDER_BY_clause_in_query_and_checking_mode_is_STRICT() {
        // Given
        System.setProperty("sysprop.bonita.orderby.checking.mode", OrderByCheckingMode.STRICT.name());
        doReturn("").when(query).getQueryString();

        // When
        tenantHibernatePersistenceService.checkOrderByClause(query);
    }

    @Test
    public final void checkOrderByClause_should_do_nothing_when_no_ORDER_BY_clause_in_query_and_no_checking_mode() {
        // Given
        System.clearProperty("sysprop.bonita.orderby.checking.mode");
        doReturn("").when(query).getQueryString();

        // When
        tenantHibernatePersistenceService.checkOrderByClause(query);
    }

    @Test
    public final void checkOrderByClause_should_do_nothing_when_no_ORDER_BY_clause_in_query_and_checking_mode_is_NONE() {
        // Given
        System.setProperty("sysprop.bonita.orderby.checking.mode", OrderByCheckingMode.NONE.name());
        doReturn("").when(query).getQueryString();

        // When
        tenantHibernatePersistenceService.checkOrderByClause(query);
    }

    @Test
    public final void should_log_when_no_ORDER_BY_clause_in_query_and_checking_mode_is_WARNING() throws Exception {
        // Given
        System.setProperty("sysprop.bonita.orderby.checking.mode", OrderByCheckingMode.WARNING.name());
        // recreate the service because the checking mode is read at service creation:
        tenantHibernatePersistenceService = spy(
                new TenantHibernatePersistenceService("TenantHibernatePersistenceService", sessionAccessor, hbmConfigurationProvider, null,
                        ' ', logger, sequenceManager, datasource,
                        true, null, metricsBinder));
        doReturn("").when(query).getQueryString();

        // When
        tenantHibernatePersistenceService.checkOrderByClause(query);

        // Then
        verify(logger).log(AbstractHibernatePersistenceService.class,
                TechnicalLogSeverity.WARNING,
                "Query '' does not contain 'ORDER BY' clause. It's better to modify your query to order the result, especially if you use the pagination.");
    }

    @Test
    public final void checkOrderByClause_should_do_nothing_when_ORDER_BY_clause_in_query_and_checking_mode_is_STRICT() {
        // Given
        doReturn("Order by").when(query).getQueryString();
        System.setProperty("sysprop.bonita.orderby.checking.mode", OrderByCheckingMode.STRICT.name());

        // When
        tenantHibernatePersistenceService.checkOrderByClause(query);
    }

}
