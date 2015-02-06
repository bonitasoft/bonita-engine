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
package org.bonitasoft.engine.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractHibernatePersistenceServiceTest {

    @Mock
    private AbstractHibernatePersistenceService persistenceService;

    @Test
    public void should_getQueryFilters_append_OR_clause_when_wordSearch_is_enabled() {
        // We can't call the constructor as Hibernate currently tries to instantiate a Connection
        doCallRealMethod().when(persistenceService).buildLikeClauseForOneFieldOneTerm(Mockito.any(StringBuilder.class), anyString(), anyString(), anyBoolean());
        doCallRealMethod().when(persistenceService).buildLikeEscapeClause(anyString(), anyString(), anyString());
        doCallRealMethod().when(persistenceService).escapeTerm(anyString());
        doReturn("#").when(persistenceService).getLikeEscapeCharacter();

        final StringBuilder queryBuilder = new StringBuilder();
        persistenceService.buildLikeClauseForOneFieldOneTerm(queryBuilder, "myField", "foo", true);

        assertThat(queryBuilder.toString()).contains("LIKE 'foo%'").contains("LIKE '% foo%'");
    }

    @Test
    public void should_getQueryFilters_append_OR_clause_when_wordSearch_is_not_enabled() {
        // We can't call the constructor as Hibernate currently tries to instantiate a Connection
        doCallRealMethod().when(persistenceService).buildLikeClauseForOneFieldOneTerm(Mockito.any(StringBuilder.class), anyString(), anyString(), anyBoolean());
        doCallRealMethod().when(persistenceService).buildLikeEscapeClause(anyString(), anyString(), anyString());
        doCallRealMethod().when(persistenceService).escapeTerm(anyString());
        doReturn("#").when(persistenceService).getLikeEscapeCharacter();

        final StringBuilder queryBuilder = new StringBuilder();
        persistenceService.buildLikeClauseForOneFieldOneTerm(queryBuilder, "myField", "foo", false);

        assertThat(queryBuilder.toString()).contains("LIKE 'foo%'").doesNotContain("LIKE '% foo%'");
    }

}
