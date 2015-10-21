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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.text.WordUtils;
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

    private static final char BLANK_SPACE = ' ';

    private static final String NEW_LINE = "\n";

    private static final String SELECT = "SELECT ";

    public static final String COUNT = "COUNT";

    public static final String OPENING_PARENTHESIS = "(";

    public static final String CLOSING_PARENTHESIS = ")";

    private static final String FROM = "FROM ";

    private static final String WHERE = "WHERE ";

    private static final String LOGIC_AND = "AND ";

    private static final String ORDER_BY = "ORDER BY ";

    public static final String MAX_RESULTS_PARAM_NAME = "maxResults";

    public static final String START_INDEX_PARAM_NAME = "startIndex";

    public static final String COUNT_SUFFIX = ".count";

    public static String createQueryNameForUniqueConstraint(final UniqueConstraint uniqueConstraint) {
        if (uniqueConstraint == null) {
            throw new IllegalArgumentException("uniqueConstraint cannot be null");
        }
        return getQueryName(uniqueConstraint.getFieldNames().toArray(new String[0]));//FIXME concat all fields instead of taking first field
    }

    public static String getQueryName(final String... fieldNames) {
        final StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append("find");
        if (fieldNames != null && fieldNames.length > 0) {
            nameBuilder.append("By");
            String paramName;
            final int numberOfFields = fieldNames.length - 1;
            for (int i = 0; i < numberOfFields; i++) {
                final String fieldName = fieldNames[i];
                paramName = WordUtils.capitalize(fieldName);
                nameBuilder.append(paramName).append("And");
            }
            final String fieldName = fieldNames[numberOfFields];
            paramName = WordUtils.capitalize(fieldName);
            nameBuilder.append(paramName);
        }
        return nameBuilder.toString();
    }

    public static String getSimpleBusinessObjectName(final String businessObjectName) {
        String newBusinessObjectName = businessObjectName;
        final int lastIndexOf = newBusinessObjectName.lastIndexOf(".");
        if (lastIndexOf != -1) {
            newBusinessObjectName = newBusinessObjectName.substring(lastIndexOf + 1, newBusinessObjectName.length());
        }
        return newBusinessObjectName;
    }

    public static Query createQueryForUniqueConstraint(final BusinessObject businessObject, final UniqueConstraint uniqueConstraint) {
        final String name = createQueryNameForUniqueConstraint(uniqueConstraint);
        final String content = createQueryContentForUniqueConstraint(businessObject.getQualifiedName(), uniqueConstraint);
        final Query q = new Query(name, content, businessObject.getQualifiedName());
        for (final String fieldName : uniqueConstraint.getFieldNames()) {
            final Field f = getField(fieldName, businessObject);
            if (f instanceof SimpleField) {
                q.addQueryParameter(f.getName(), ((SimpleField) f).getType().getClazz().getName());
            }
        }
        return q;
    }

    public static Query createQueryForField(final BusinessObject businessObject, final Field field) {
        if (field == null) {
            throw new IllegalArgumentException("field cannot be null");
        }
        if (field.isCollection() != null && field.isCollection()) {
            throw new IllegalArgumentException("Collection field are not supported");
        }
        final String name = createQueryNameForField(field);
        final String content = createQueryContentForField(businessObject.getQualifiedName(), field);
        final Query q = new Query(name, content, List.class.getName());
        if (field instanceof SimpleField) {
            q.addQueryParameter(field.getName(), ((SimpleField) field).getType().getClazz().getName());
        }
        return q;
    }

    public static Query createQueryForPersistenceId(final BusinessObject businessObject) {
        final SimpleField persistenceIdField = new SimpleField();
        persistenceIdField.setName(Field.PERSISTENCE_ID);
        persistenceIdField.setType(FieldType.LONG);
        final String name = createQueryNameForField(persistenceIdField);
        final UniqueConstraint constraint = new UniqueConstraint();
        constraint.setFieldNames(Arrays.asList(persistenceIdField.getName()));
        final String content = createQueryContentForUniqueConstraint(businessObject.getQualifiedName(), constraint);
        final Query q = new Query(name, content, businessObject.getQualifiedName());
        q.addQueryParameter(persistenceIdField.getName(), persistenceIdField.getType().getClazz().getName());
        return q;
    }

    public static String createQueryNameForField(final Field field) {
        if (field == null) {
            throw new IllegalArgumentException("field cannot be null");
        }
        return getQueryName(field.getName());
    }

    public static Field getField(final String fieldName, final BusinessObject businessObject) {
        for (final Field f : businessObject.getFields()) {
            if (f.getName().equals(fieldName)) {
                return f;
            }
        }
        throw new IllegalArgumentException(fieldName + " doesn't exist in " + businessObject.getQualifiedName());
    }

    public static String createQueryContentForUniqueConstraint(final String businessObjectName, final UniqueConstraint uniqueConstraint) {
        checkArgumentisNotEmpty(businessObjectName);
        final String simpleName = getSimpleBusinessObjectName(businessObjectName);
        final char alias = getSimpleNameAlias(simpleName);
        final String selectBlock = buildSelectFrom(simpleName, alias);
        return buildQueryForUniqueConstraint(uniqueConstraint, alias, selectBlock);
    }

    protected static char getSimpleNameAlias(String simpleName) {
        return Character.toLowerCase(simpleName.charAt(0));
    }

    protected static void checkArgumentisNotEmpty(String argument) {
        if (argument == null) {
            throw new IllegalArgumentException("businessObjectName is null");
        }
        if (argument.isEmpty()) {
            throw new IllegalArgumentException("businessObjectName is empty");
        }
    }

    public static String createQueryContentForField(final String businessObjectName, final Field field) {
        checkArgumentisNotEmpty(businessObjectName);
        if (field == null) {
            throw new IllegalArgumentException("field cannot be null");
        }
        final String simpleName = getSimpleBusinessObjectName(businessObjectName);
        final char var = getSimpleNameAlias(simpleName);
        final StringBuilder builder = new StringBuilder();
        builder.append(buildSelectFrom(simpleName, var));
        builder.append(buildWhere(var, field.getName()));
        builder.append(buildOrderBy(var));
        return builder.toString();
    }

    public static List<Query> createProvidedQueriesForBusinessObject(final BusinessObject businessObject) {
        final List<Query> queries = new ArrayList<Query>();
        final Set<String> queryNames = new HashSet<String>();

        if (!containsQueryWithName(businessObject, getQueryName(Field.PERSISTENCE_ID))) {
            final Query query = createQueryForPersistenceId(businessObject);
            queries.add(query);
            queryNames.add(query.getName());
        }

        for (final UniqueConstraint uniqueConstraint : businessObject.getUniqueConstraints()) {
            final Query query = createQueryForUniqueConstraint(businessObject, uniqueConstraint);
            final Query countQuery = createCountQueryForUniqueConstraint(businessObject, uniqueConstraint);

            queryNames.add(query.getName());
            queryNames.add(countQuery.getName());

            queries.add(query);
            queries.add(countQuery);

        }
        for (final Field f : businessObject.getFields()) {
            if (f instanceof SimpleField) {
                if (f.isCollection() == null || !f.isCollection()) {
                    final String potentialConflictingQueryName = createQueryNameForField(f);
                    if (!queryNames.contains(potentialConflictingQueryName)) {
                        final Query query = createQueryForField(businessObject, f);
                        queries.add(query);
                    }
                }
            }
        }
        queries.add(createSelectAllQueryForBusinessObject(businessObject));
        return queries;
    }

    public static Query createQueryForLazyField(final BusinessObject businessObject, final RelationField field) {
        if (field == null) {
            throw new IllegalArgumentException("field cannot be null");
        }
        final String name = createQueryNameForLazyField(businessObject, field);
        final String content = createQueryContentForLazyField(businessObject.getQualifiedName(), field);
        final Query q = new Query(name, content, field.getReference().getQualifiedName());
        if (field.isCollection()) {
            q.setReturnType(List.class.getName());
        }
        q.addQueryParameter(Field.PERSISTENCE_ID, Long.class.getName());
        return q;
    }

    public static String createQueryContentForLazyField(final String businessObjectName, final RelationField field) {
        checkArgumentisNotEmpty(businessObjectName);
        if (field == null) {
            throw new IllegalArgumentException("field cannot be null");
        }
        final String boName = getSimpleBusinessObjectName(businessObjectName);
        final String boAlias = boName.toLowerCase() + "_0";
        final String fieldName = field.getName();
        final String fieldAlias = fieldName.toLowerCase() + "_1";

        return String.format("SELECT %s FROM %s %s JOIN %s.%s as %s WHERE %s.%s= :%s", fieldAlias, boName, boAlias,
                boAlias,
                fieldName, fieldAlias, boAlias, Field.PERSISTENCE_ID, Field.PERSISTENCE_ID);

    }

    public static String createQueryNameForLazyField(final BusinessObject businessObject, final RelationField field) {
        final StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append("find");
        nameBuilder.append(WordUtils.capitalize(field.getName()));
        nameBuilder.append("By");
        nameBuilder.append(getSimpleBusinessObjectName(businessObject.getQualifiedName()));
        nameBuilder.append("PersistenceId");
        return nameBuilder.toString();
    }

    public static Set<String> getAllProvidedQueriesNameForBusinessObject(final BusinessObject businessObject) {
        final Set<String> queryNames = new HashSet<String>();

        if (!containsQueryWithName(businessObject, getQueryName(Field.PERSISTENCE_ID))) {
            final SimpleField persistenceIdField = new SimpleField();
            persistenceIdField.setName(Field.PERSISTENCE_ID);
            persistenceIdField.setType(FieldType.LONG);
            queryNames.add(createQueryNameForField(persistenceIdField));
        }

        for (final UniqueConstraint uc : businessObject.getUniqueConstraints()) {
            queryNames.add(createQueryNameForUniqueConstraint(uc));
        }
        for (final Field f : businessObject.getFields()) {
            if (f instanceof SimpleField) {
                if (f.isCollection() == null || !f.isCollection()) {
                    queryNames.add(createQueryNameForField(f));
                }
            }
        }
        queryNames.add(createSelectAllQueryName());
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

    public static Query createSelectAllQueryForBusinessObject(final BusinessObject businessObject) {
        if (businessObject == null) {
            throw new IllegalArgumentException("businessObject cannot be null");
        }
        final String queryName = createSelectAllQueryName();
        final String content = createSelectAllQueryContent(businessObject.getQualifiedName());
        return new Query(queryName, content, List.class.getName());
    }

    public static String createSelectAllQueryContent(final String businessObjectName) {
        checkArgumentisNotEmpty(businessObjectName);
        final String simpleName = getSimpleBusinessObjectName(businessObjectName);
        final char alias = getSimpleNameAlias(simpleName);
        final StringBuilder sb = new StringBuilder();
        sb.append(buildSelectFrom(simpleName, alias));
        sb.append(buildOrderBy(alias));
        return sb.toString();
    }

    public static String createSelectAllQueryName() {
        return getQueryName();
    }

    private static String buildSelectFrom(final String simpleName, final char simpleNameAlias) {
        final StringBuilder builder = new StringBuilder();
        builder.append(SELECT).append(simpleNameAlias).append(NEW_LINE);
        builder.append(FROM).append(simpleName).append(BLANK_SPACE).append(simpleNameAlias).append(NEW_LINE);
        return builder.toString();
    }

    private static String buildCountSelectFrom(final String simpleName, final char alias) {
        final StringBuilder builder = new StringBuilder();
        builder.append(SELECT)
                .append(COUNT).append(OPENING_PARENTHESIS).append(alias).append(CLOSING_PARENTHESIS)
                .append(NEW_LINE)

        .append(FROM).append(simpleName).append(BLANK_SPACE).append(alias)
                .append(NEW_LINE);
        return builder.toString();
    }

    private static String buildWhere(final char prefix, final String parameterName) {
        final StringBuilder builder = new StringBuilder(WHERE);
        builder.append(buildCompareField(prefix, parameterName));
        return builder.toString();
    }

    private static String buildWhereAnd(final char prefix, final List<String> parameterNames) {
        final StringBuilder builder = new StringBuilder(WHERE);
        String paramName;
        final int numberOfParameters = parameterNames.size() - 1;
        for (int i = 0; i < numberOfParameters; i++) {
            paramName = parameterNames.get(i);
            builder.append(buildCompareField(prefix, paramName));
            builder.append(LOGIC_AND);
        }
        paramName = parameterNames.get(numberOfParameters);
        builder.append(buildCompareField(prefix, paramName));
        return builder.toString();
    }

    private static String buildCompareField(final char prefix, final String paramName) {
        final StringBuilder builder = new StringBuilder();
        builder.append(prefix).append('.').append(paramName).append("= :").append(paramName).append(NEW_LINE);
        return builder.toString();
    }

    private static String buildOrderBy(final char tablePrefix) {
        final StringBuilder builder = new StringBuilder();
        builder.append(ORDER_BY).append(tablePrefix).append('.').append(Field.PERSISTENCE_ID);
        return builder.toString();
    }

    public static List<Query> createProvidedQueriesForLazyField(final BusinessObjectModel bom, final BusinessObject bo) {
        final List<Query> queries = new ArrayList<Query>();
        for (final BusinessObject businessObject : bom.getBusinessObjects()) {
            for (final Field f : businessObject.getFields()) {
                if (f instanceof RelationField && ((RelationField) f).isLazy()) {
                    if (((RelationField) f).getReference().equals(bo)) {
                        final Query query = createQueryForLazyField(businessObject, (RelationField) f);
                        queries.add(query);
                    }
                }
            }
        }
        return queries;
    }

    public static String createCountQueryNameForUniqueConstraint(UniqueConstraint uniqueConstraint) {
        return new StringBuilder().append(createQueryNameForUniqueConstraint(uniqueConstraint)).append(COUNT_SUFFIX).toString();
    }

    public static String createCountQueryContentForUniqueConstraint(String businessObjectName, UniqueConstraint uniqueConstraint) {
        checkArgumentisNotEmpty(businessObjectName);
        final String simpleName = getSimpleBusinessObjectName(businessObjectName);
        final char alias = getSimpleNameAlias(simpleName);

        final String selectBlock = buildCountSelectFrom(simpleName, alias);

        return buildQueryForUniqueConstraint(uniqueConstraint, alias, selectBlock);
    }

    protected static String buildQueryForUniqueConstraint(UniqueConstraint uniqueConstraint, char alias, String selectBlock) {
        final StringBuilder builder = new StringBuilder();
        builder.append(selectBlock);
        builder.append(buildWhereAnd(alias, uniqueConstraint.getFieldNames()));
        return builder.toString();
    }

    public static Query createCountQueryForUniqueConstraint(BusinessObject businessObject, UniqueConstraint uniqueConstraint) {
        final String name = createCountQueryNameForUniqueConstraint(uniqueConstraint);
        final String content = createCountQueryContentForUniqueConstraint(businessObject.getQualifiedName(), uniqueConstraint);
        final Query q = new Query(name, content, businessObject.getQualifiedName());
        for (final String fieldName : uniqueConstraint.getFieldNames()) {
            final Field f = getField(fieldName, businessObject);
            if (f instanceof SimpleField) {
                q.addQueryParameter(f.getName(), ((SimpleField) f).getType().getClazz().getName());
            }
        }
        return q;
    }
}
