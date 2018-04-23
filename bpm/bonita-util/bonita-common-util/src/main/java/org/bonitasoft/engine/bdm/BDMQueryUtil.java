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
package org.bonitasoft.engine.bdm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.Query;
import org.bonitasoft.engine.bdm.model.UniqueConstraint;
import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.model.field.SimpleField;

/**
 * @author Romain Bioteau
 * @author Matthieu Chaffotte
 */
public class BDMQueryUtil {

    public static final String MAX_RESULTS_PARAM_NAME = "maxResults";

    public static final String START_INDEX_PARAM_NAME = "startIndex";

    public static List<Query> createProvidedQueriesForBusinessObject(final BusinessObject businessObject) {
        final List<Query> queries = new ArrayList<>();
        for (QueryGenerator queryGenerator : getQueryGenerators()) {
            createProvidedQueriesForBusinessObject(businessObject, queries, queryGenerator);
        }
        return queries;
    }

    protected static void createProvidedQueriesForBusinessObject(BusinessObject businessObject, List<Query> queries, QueryGenerator queryGenerator) {
        final Set<String> queryNames = new HashSet<>();
        if (!containsQueryWithName(businessObject, queryGenerator.getQueryName(Field.PERSISTENCE_ID))) {
            addQueryAndNameIfNotNull(queries, queryNames, queryGenerator.createQueryForPersistenceId(businessObject));
        }

        for (final UniqueConstraint uniqueConstraint : businessObject.getUniqueConstraints()) {
            addQueryAndNameIfNotNull(queries, queryNames, queryGenerator.createQueryForUniqueConstraint(businessObject, uniqueConstraint));

        }
        for (final Field field : businessObject.getFields()) {
            if (field instanceof SimpleField) {
                if (field.isCollection() == null || !field.isCollection()) {
                    final String potentialConflictingQueryName = queryGenerator.createQueryNameForField(field);
                    if (!queryNames.contains(potentialConflictingQueryName)) {
                        addQueryIfNotNull(queries, queryGenerator.createQueryForField(businessObject, field));
                    }
                }
            }
        }
        queries.add(queryGenerator.createSelectAllQueryForBusinessObject(businessObject));
    }

    private static void addQueryAndNameIfNotNull(List<Query> queries, Set<String> queryNames, Query query) {
        if (query != null) {
            addQueryIfNotNull(queries, query);
            queryNames.add(query.getName());
        }
    }

    private static void addQueryIfNotNull(List<Query> queries, Query query) {
        if (query != null) {
            queries.add(query);
        }
    }

    protected static List<QueryGenerator> getQueryGenerators() {
        List<QueryGenerator> queryGenerators = new ArrayList<>();
        queryGenerators.add(new FindQueryGenerator());
        queryGenerators.add(new CountQueryGenerator());
        return queryGenerators;
    }

    public static Set<String> getAllProvidedQueriesNameForBusinessObject(final BusinessObject businessObject) {
        final Set<String> queryNames = new HashSet<>();
        for (QueryGenerator queryGenerator : getQueryGenerators()) {
            if (!containsQueryWithName(businessObject, queryGenerator.getQueryName(Field.PERSISTENCE_ID))) {
                final SimpleField persistenceIdField = new SimpleField();
                persistenceIdField.setName(Field.PERSISTENCE_ID);
                persistenceIdField.setType(FieldType.LONG);
                queryNames.add(queryGenerator.createQueryNameForField(persistenceIdField));
            }

            for (final UniqueConstraint uc : businessObject.getUniqueConstraints()) {
                if(uc.getFieldNames() != null){
                    queryNames.add(queryGenerator.createQueryNameForUniqueConstraint(uc));
                }
            }
            for (final Field f : businessObject.getFields()) {
                if (f instanceof SimpleField) {
                    if (f.isCollection() == null || !f.isCollection()) {
                        queryNames.add(queryGenerator.createQueryNameForField(f));
                    }
                }
            }
            queryNames.add(queryGenerator.getSelectAllQueryName());
        }

        return queryNames;
    }

    private static boolean containsQueryWithName(final BusinessObject businessObject, final String queryName) {
        for (final Query q : businessObject.getQueries()) {
            if (Objects.equals(queryName, q.getName())) {
                return true;
            }
        }
        return false;
    }

    public static String getCountQueryName(String selectQueryName) {
        return new StringBuilder(QueryGenerator.COUNT_PREFIX).append(selectQueryName.substring(0, 1).toUpperCase()).append(selectQueryName.substring(1)).toString();
    }

    public static List<Query> createProvidedQueriesForLazyField(final BusinessObjectModel bom, final BusinessObject bo) {
        final List<Query> queries = new ArrayList<>();
        for (QueryGenerator queryGenerator : getQueryGenerators()) {
            for (final BusinessObject businessObject : bom.getBusinessObjects()) {
                for (final Field f : businessObject.getFields()) {
                    if (f instanceof RelationField && ((RelationField) f).isLazy()) {
                        if (((RelationField) f).getReference().equals(bo)) {
                            final Query query = queryGenerator.createQueryForLazyField(businessObject, (RelationField) f);
                            if (query != null) {
                                queries.add(query);
                            }
                        }
                    }
                }
            }
        }

        return queries;
    }

    public static List<Query> createCountProvidedQueriesForBusinessObject(BusinessObject businessObject) {
        List<Query> queries = new ArrayList<>();
        final QueryGenerator queryGenerator = new CountQueryGenerator();
        createProvidedQueriesForBusinessObject(businessObject, queries, queryGenerator);
        return queries;
    }
}
