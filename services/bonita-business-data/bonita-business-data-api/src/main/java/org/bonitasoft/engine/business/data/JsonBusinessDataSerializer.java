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
package org.bonitasoft.engine.business.data;

import java.util.List;

import org.bonitasoft.engine.bdm.Entity;

public interface JsonBusinessDataSerializer {

    String EMPTY_OBJECT = "{}";

    /**
     * Returns whether standard JSON shape is enabled for BDM serialization.
     * Standard shape returns:
     * - Scalar/count queries as {"value": n} instead of [n]
     * - Single-entity queries as {...} instead of [{...}]
     *
     * @return true if standard shape is enabled, false for legacy array format
     */
    boolean isStandardShapeEnabled();

    String serializeEntity(Entity entity, String businessDataURIPattern)
            throws SBusinessDataRepositorySerializationException;

    String serializeEntities(List<? extends Entity> entities, String businessDataURIPattern)
            throws SBusinessDataRepositorySerializationException;

    /**
     * @deprecated Use {@link #serializeScalarResult(List, String, boolean)} instead.
     *             This method is kept for backward compatibility but will be removed in future versions.
     */
    @Deprecated(forRemoval = true)
    String serializeCountResult(List<Long> list, String entityClassName)
            throws SBusinessDataRepositorySerializationException;

    /**
     * Serialize scalar query results (Long, Double, Float, Integer).
     *
     * @param list the list containing the scalar result (typically single element)
     * @param entityClassName the fully qualified name of the entity class
     * @param useStandardShape if true, returns {"value": 10} format; if false, returns [10] format
     * @return JSON serialized scalar result, e.g., "[10]" or "{\"value\": 10}" depending on format
     * @throws SBusinessDataRepositorySerializationException if an error occurs during serialization
     */
    String serializeScalarResult(List<?> list, String entityClassName, boolean useStandardShape)
            throws SBusinessDataRepositorySerializationException;

    /**
     * Serialize entity query results handling both single and multiple entity returns.
     * Standard shape behavior:
     * - Query defined to return single entity: {...} (object) or {} if empty
     * - Query defined to return List (multiple results): [{...}] (array) even if only 1 result
     * Legacy shape behavior (backward compatible):
     * - All queries return arrays: [{...}] even for single entity queries
     *
     * @param entities the list of entities to serialize
     * @param businessDataURIPattern the URI pattern for generating entity links
     * @param useStandardShape if true, uses standard shape rules; if false, always returns array
     * @param queryReturnsMultipleResults if true, the query is designed to return a List (always array output)
     * @return JSON serialized entity result
     * @throws SBusinessDataRepositorySerializationException if an error occurs during serialization
     */
    String serializeEntityQueryResult(List<? extends Entity> entities, String businessDataURIPattern,
            boolean useStandardShape, boolean queryReturnsMultipleResults)
            throws SBusinessDataRepositorySerializationException;
}
