/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.process.definition.model;

/**
 * Criteria for a page of the grouped process-name search.
 *
 * @param activationState
 *        the activation state to filter on, or {@code null} for no activation-state filter
 * @param searchTerm
 *        the SQL LIKE pattern to match against name and displayName ({@code "%"} to match all)
 * @param sortByName
 *        {@code true} to order primarily by name, {@code false} to order primarily by displayName
 * @param ascending
 *        {@code true} for ascending order, {@code false} for descending
 * @param startIndex
 *        the index of the first group to return (0-based)
 * @param maxResults
 *        the maximum number of groups to return
 */
public record ProcessNameGroupQuery(
        String activationState,
        String searchTerm,
        boolean sortByName,
        boolean ascending,
        int startIndex,
        int maxResults) {}
