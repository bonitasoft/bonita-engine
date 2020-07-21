/**
 * Copyright (C) 2020 Bonitasoft S.A.
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

import static org.bonitasoft.engine.persistence.QueryBuilder.escapeTerm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;

class QueryGeneratorForSearchTerm {

    private String likeEscapeCharacter;
    private int parameterCounter = 1;
    private Map<String, Object> parameters = new HashMap<>();

    QueryGeneratorForSearchTerm(char likeEscapeCharacter) {
        this.likeEscapeCharacter = String.valueOf(likeEscapeCharacter);
    }

    private String createParameter(Object fieldValue) {
        final String parameterName = "s" + parameterCounter++;
        parameters.put(parameterName, fieldValue);
        return ":" + parameterName;
    }

    /**
     * Get like clause for given term with escaped sql query wildcards and escape character
     */
    private String buildLikeEscapeClause(String term) {
        return " LIKE " + createParameter(term)
                + " ESCAPE '" + likeEscapeCharacter + "'";
    }

    void buildLikeClauseForOneFieldOneTerm(final StringBuilder queryBuilder, final String currentField,
            final String currentTerm,
            final boolean enableWordSearch) {
        // Search if a sentence starts with the term
        queryBuilder.append(currentField)
                .append(buildLikeEscapeClause(escapeTerm(currentTerm, likeEscapeCharacter) + "%"));

        if (enableWordSearch) {
            // Search also if a word starts with the term
            // We do not want to search for %currentTerm% to ensure we can use Lucene-like library.
            queryBuilder.append(" OR ").append(currentField)
                    .append(buildLikeEscapeClause("% " + escapeTerm(currentTerm, likeEscapeCharacter) + "%"));
        }
    }

    private void buildLikeClauseForOneFieldMultipleTerms(final StringBuilder queryBuilder, final String currentField,
            final List<String> terms,
            final boolean enableWordSearch) {
        final Iterator<String> termIterator = terms.iterator();
        while (termIterator.hasNext()) {
            final String currentTerm = termIterator.next();

            buildLikeClauseForOneFieldOneTerm(queryBuilder, currentField, currentTerm, enableWordSearch);

            if (termIterator.hasNext()) {
                queryBuilder.append(" OR ");
            }
        }
    }

    QueryGeneratedSearchTerms generate(Set<String> fields, List<String> terms, boolean enableWordSearch) {
        StringBuilder stringBuilder = new StringBuilder();
        final Iterator<String> fieldIterator = fields.iterator();
        while (fieldIterator.hasNext()) {
            buildLikeClauseForOneFieldMultipleTerms(stringBuilder, fieldIterator.next(), terms, enableWordSearch);
            if (fieldIterator.hasNext()) {
                stringBuilder.append(" OR ");
            }
        }
        return new QueryGeneratedSearchTerms(stringBuilder.toString(), parameters);
    }

    @Data
    @AllArgsConstructor
    static final class QueryGeneratedSearchTerms {

        private String search;
        private Map<String, Object> parameters;
    }
}
