package org.bonitasoft.engine.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.mockito.Mockito;

public class AbstractHibernatePersistenceServiceTest {

    @Test
    public void should_getQueryFilters_append_OR_clause_when_wordSearch_is_enabled() throws Exception {
        // We can't call the constructor as Hibernate currently tries to instantiate a Connection
        final AbstractHibernatePersistenceService persistenceService = mock(AbstractHibernatePersistenceService.class);
        doCallRealMethod().when(persistenceService).buildLikeClauseForOneFieldOneTerm(Mockito.any(StringBuilder.class), anyString(), anyString(), anyBoolean());
        doCallRealMethod().when(persistenceService).buildLikeEscapeClause(anyString(), anyString(), anyString());
        doCallRealMethod().when(persistenceService).escapeTerm(anyString());
        doReturn("#").when(persistenceService).getLikeEscapeCharacter();

        final StringBuilder queryBuilder = new StringBuilder();
        persistenceService.buildLikeClauseForOneFieldOneTerm(queryBuilder, "myField", "foo", true);

        assertThat(queryBuilder.toString()).contains("LIKE 'foo%'").contains("LIKE '% foo%'");
    }

    @Test
    public void should_getQueryFilters_append_OR_clause_when_wordSearch_is_not_enabled() throws Exception {
        // We can't call the constructor as Hibernate currently tries to instantiate a Connection
        final AbstractHibernatePersistenceService persistenceService = mock(AbstractHibernatePersistenceService.class);
        doCallRealMethod().when(persistenceService).buildLikeClauseForOneFieldOneTerm(Mockito.any(StringBuilder.class), anyString(), anyString(), anyBoolean());
        doCallRealMethod().when(persistenceService).buildLikeEscapeClause(anyString(), anyString(), anyString());
        doCallRealMethod().when(persistenceService).escapeTerm(anyString());
        doReturn("#").when(persistenceService).getLikeEscapeCharacter();

        final StringBuilder queryBuilder = new StringBuilder();
        persistenceService.buildLikeClauseForOneFieldOneTerm(queryBuilder, "myField", "foo", false);

        assertThat(queryBuilder.toString()).contains("LIKE 'foo%'").doesNotContain("LIKE '% foo%'");
    }

    // buildLikeEscapeClause
    // isWordSearchEnabled

}
