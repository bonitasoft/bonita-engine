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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

class QueryGeneratorForSearchTerm {

    private String likeEscapeCharacter;

    QueryGeneratorForSearchTerm(char likeEscapeCharacter) {
        this.likeEscapeCharacter = String.valueOf(likeEscapeCharacter);
    }

    /**
     * Get like clause for given term with escaped sql query wildcards and escape character
     *
     * @param term
     * @param prefixPattern
     * @param suffixPattern
     */
    private String buildLikeEscapeClause(final String term, final String prefixPattern, final String suffixPattern) {
        return " LIKE '" + (prefixPattern != null ? prefixPattern : "")
                + QueryBuilder.escapeTerm(term, likeEscapeCharacter)
                + (suffixPattern != null ? suffixPattern : "") + "' ESCAPE '"
                + likeEscapeCharacter + "'";
    }

    private void buildLikeClauseForOneFieldOneTerm(final StringBuilder queryBuilder, final String currentField,
            final String currentTerm,
            final boolean enableWordSearch) {
        // Search if a sentence starts with the term
        queryBuilder.append(currentField).append(buildLikeEscapeClause(currentTerm, "", "%"));

        if (enableWordSearch) {
            // Search also if a word starts with the term
            // We do not want to search for %currentTerm% to ensure we can use Lucene-like library.
            queryBuilder.append(" OR ").append(currentField).append(buildLikeEscapeClause(currentTerm, "% ", "%"));
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

    String generate(Set<String> fields, List<String> terms, boolean enableWordSearch) {
        StringBuilder stringBuilder = new StringBuilder();
        final Iterator<String> fieldIterator = fields.iterator();
        while (fieldIterator.hasNext()) {
            buildLikeClauseForOneFieldMultipleTerms(stringBuilder, fieldIterator.next(), terms, enableWordSearch);
            if (fieldIterator.hasNext()) {
                stringBuilder.append(" OR ");
            }
        }
        return stringBuilder.toString();
    }
}
