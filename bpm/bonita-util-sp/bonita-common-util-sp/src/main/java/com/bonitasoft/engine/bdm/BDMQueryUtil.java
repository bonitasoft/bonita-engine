/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.text.WordUtils;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.Query;
import com.bonitasoft.engine.bdm.model.UniqueConstraint;
import com.bonitasoft.engine.bdm.model.field.Field;
import com.bonitasoft.engine.bdm.model.field.SimpleField;

/**
 * @author Romain Bioteau
 * @author Matthieu Chaffotte
 */
public class BDMQueryUtil {

    private static final String SELECT = "SELECT ";

    private static final String FROM = "FROM ";

    private static final String WHERE = "WHERE ";

    private static final String LOGIC_AND = "AND ";

    private static final String ORDER_BY = "ORDER BY ";

    public static final String MAX_RESULTS_PARAM_NAME = "maxResults";

    public static final String START_INDEX_PARAM_NAME = "startIndex";

    public static String createQueryNameForUniqueConstraint(final UniqueConstraint uniqueConstraint) {
        if (uniqueConstraint == null) {
            throw new IllegalArgumentException("uniqueConstraint cannot be null");
        }
        return getQueryName(uniqueConstraint.getFieldNames());
    }

    public static String getQueryName(final List<String> fieldNames) {
        final StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append("find");
        if (!fieldNames.isEmpty()) {
            nameBuilder.append("By");
            String paramName;
            final int numberOfFields = fieldNames.size() - 1;
            for (int i = 0; i < numberOfFields; i++) {
                final String fieldName = fieldNames.get(i);
                paramName = WordUtils.capitalize(fieldName);
                nameBuilder.append(paramName).append("And");
            }
            final String fieldName = fieldNames.get(numberOfFields);
            paramName = WordUtils.capitalize(fieldName);
            nameBuilder.append(paramName);
        }
        return nameBuilder.toString();
    }

    public static String getSimpleBusinessObjectName(String businessObjectName) {
        final int lastIndexOf = businessObjectName.lastIndexOf(".");
        if (lastIndexOf != -1) {
            businessObjectName = businessObjectName.substring(lastIndexOf + 1, businessObjectName.length());
        }
        return businessObjectName;
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

    public static String createQueryNameForField(final Field field) {
        if (field == null) {
            throw new IllegalArgumentException("field cannot be null");
        }
        return getQueryName(Arrays.asList(field.getName()));
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
        if (businessObjectName == null) {
            throw new IllegalArgumentException("businessObjectName is null");
        }
        if (businessObjectName.isEmpty()) {
            throw new IllegalArgumentException("businessObjectName is empty");
        }
        final String simpleName = getSimpleBusinessObjectName(businessObjectName);
        final char var = Character.toLowerCase(simpleName.charAt(0));
        final StringBuilder builder = new StringBuilder();
        builder.append(buildSelectFrom(simpleName, var));
        builder.append(buildWhereAnd(var, uniqueConstraint.getFieldNames()));
        return builder.toString();
    }

    public static String createQueryContentForField(final String businessObjectName, final Field field) {
        if (businessObjectName == null) {
            throw new IllegalArgumentException("businessObjectName is null");
        }
        if (field == null) {
            throw new IllegalArgumentException("field cannot be null");
        }
        final String simpleName = getSimpleBusinessObjectName(businessObjectName);
        final char var = Character.toLowerCase(simpleName.charAt(0));
        final StringBuilder builder = new StringBuilder();
        builder.append(buildSelectFrom(simpleName, var));
        builder.append(buildWhere(var, field.getName()));
        builder.append(buildOrderBy(var));
        return builder.toString();
    }

    public static List<Query> createProvidedQueriesForBusinessObject(final BusinessObject businessObject) {
        final List<Query> queries = new ArrayList<Query>();
        final Set<String> queryNames = new HashSet<String>();
        for (final UniqueConstraint uc : businessObject.getUniqueConstraints()) {
            final Query query = createQueryForUniqueConstraint(businessObject, uc);
            queryNames.add(query.getName());
            queries.add(query);
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

    public static Set<String> getAllProvidedQueriesNameForBusinessObject(final BusinessObject businessObject) {
        final Set<String> queryNames = new HashSet<String>();
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

    public static Query createSelectAllQueryForBusinessObject(final BusinessObject businessObject) {
        if (businessObject == null) {
            throw new IllegalArgumentException("businessObject cannot be null");
        }
        final String queryName = createSelectAllQueryName();
        final String content = createSelectAllQueryContent(businessObject.getQualifiedName());
        return new Query(queryName, content, List.class.getName());
    }

    public static String createSelectAllQueryContent(final String businessObjectName) {
        if (businessObjectName == null) {
            throw new IllegalArgumentException("businessObjectName is null");
        }
        final String simpleName = getSimpleBusinessObjectName(businessObjectName);
        final char var = Character.toLowerCase(simpleName.charAt(0));
        final StringBuilder sb = new StringBuilder();
        sb.append(buildSelectFrom(simpleName, var));
        sb.append(buildOrderBy(var));
        return sb.toString();
    }

    public static String createSelectAllQueryName() {
        return getQueryName(Collections.<String> emptyList());
    }

    private static String buildSelectFrom(final String simpleName, final char var) {
        final StringBuilder builder = new StringBuilder();
        builder.append(SELECT).append(var).append("\n");
        builder.append(FROM).append(simpleName).append(' ').append(var).append("\n");
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
        builder.append(prefix).append('.').append(paramName).append("= :").append(paramName).append("\n");
        return builder.toString();
    }

    private static String buildOrderBy(final char tablePrefix) {
        final StringBuilder builder = new StringBuilder();
        builder.append(ORDER_BY).append(tablePrefix).append('.').append(Field.PERSISTENCE_ID);
        return builder.toString();
    }

}
