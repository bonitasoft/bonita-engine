/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.bdm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Romain Bioteau
 */
public class BDMQueryUtil {

    private static final String AND_SEPARATOR = "And";

    private static final String LOGIC_AND = " AND ";

    private static final String FROM = "FROM";

    private static final String SELECT = "SELECT";

    private static final String WHERE = "WHERE";

    private static final String ORDER_BY = " ORDER BY ";

    public static final String MAX_RESULTS_PARAM_NAME = "maxResults";

    public static final String START_INDEX_PARAM_NAME = "startIndex";

    public static String createQueryNameForUniqueConstraint(String businessObjectName, final UniqueConstraint uniqueConstraint) {
        if (businessObjectName == null) {
            throw new IllegalArgumentException("businessObjectName cannot be null");
        }
        if (uniqueConstraint == null) {
            throw new IllegalArgumentException("uniqueConstraint cannot be null");
        }
        businessObjectName = getSimpleBusinessObjectName(businessObjectName);
        StringBuilder sb = new StringBuilder("get" + businessObjectName + "By");
        for (String f : uniqueConstraint.getFieldNames()) {
            f = Character.toUpperCase(f.charAt(0)) + f.substring(1);
            sb.append(f);
            sb.append(AND_SEPARATOR);
        }
        String name = sb.toString();
        if (name.endsWith(AND_SEPARATOR)) {
            name = name.substring(0, name.length() - 3);
        }
        return name;
    }

    private static String getSimpleBusinessObjectName(String businessObjectName) {
        int lastIndexOf = businessObjectName.lastIndexOf(".");
        if (lastIndexOf != -1) {
            businessObjectName = businessObjectName.substring(lastIndexOf + 1, businessObjectName.length());
        }
        return businessObjectName;
    }

    public static Query createQueryForUniqueConstraint(final BusinessObject businessObject, final UniqueConstraint uniqueConstraint) {
        String name = createQueryNameForUniqueConstraint(businessObject.getQualifiedName(), uniqueConstraint);
        String content = createQueryContentForUniqueConstraint(businessObject.getQualifiedName(), uniqueConstraint);
        Query q = new Query(name, content, businessObject.getQualifiedName());
        for (String fieldName : uniqueConstraint.getFieldNames()) {
            Field f = getField(fieldName, businessObject);
            q.addQueryParameter(f.getName(), f.getType().getClazz().getName());
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
        String name = createQueryNameForField(businessObject.getQualifiedName(), field);
        String content = createQueryContentForField(businessObject.getQualifiedName(), field);
        Query q = new Query(name, content, List.class.getName());
        q.addQueryParameter(field.getName(), field.getType().getClazz().getName());
        return q;
    }

    public static String createQueryNameForField(String businessObjectName, final Field field) {
        if (businessObjectName == null) {
            throw new IllegalArgumentException("businessObjectName cannot be null");
        }
        if (field == null) {
            throw new IllegalArgumentException("field cannot be null");
        }
        businessObjectName = getSimpleBusinessObjectName(businessObjectName);
        StringBuilder sb = new StringBuilder("get" + businessObjectName + "By");
        String fName = field.getName();
        fName = Character.toUpperCase(fName.charAt(0)) + fName.substring(1);
        sb.append(fName);
        String name = sb.toString();
        return name;
    }

    public static Field getField(final String fieldName, final BusinessObject businessObject) {
        for (Field f : businessObject.getFields()) {
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
        String simpleName = getSimpleBusinessObjectName(businessObjectName);
        char var = Character.toLowerCase(simpleName.charAt(0));
        StringBuilder sb = new StringBuilder();
        sb.append(SELECT);
        sb.append(" ");
        sb.append(var);
        sb.append("\n");
        sb.append(FROM);
        sb.append(" ");
        sb.append(simpleName);
        sb.append(" ");
        sb.append(var);
        sb.append("\n");
        sb.append(WHERE);
        sb.append(" ");
        for (String fieldName : uniqueConstraint.getFieldNames()) {
            sb.append(var + ".");
            sb.append(fieldName);
            sb.append("=:");
            sb.append(fieldName);
            sb.append(LOGIC_AND);
        }
        String query = sb.toString();
        if (query.endsWith(LOGIC_AND)) {
            query = query.substring(0, query.length() - LOGIC_AND.length());
        }
        return query;
    }

    public static String createQueryContentForField(final String businessObjectName, final Field field) {
        if (businessObjectName == null) {
            throw new IllegalArgumentException("businessObjectName is null");
        }
        if (field == null) {
            throw new IllegalArgumentException("field cannot be null");
        }
        String simpleName = getSimpleBusinessObjectName(businessObjectName);
        char var = Character.toLowerCase(simpleName.charAt(0));
        StringBuilder sb = new StringBuilder();
        sb.append(SELECT);
        sb.append(" ");
        sb.append(var);
        sb.append("\n");
        sb.append(FROM);
        sb.append(" ");
        sb.append(simpleName);
        sb.append(" ");
        sb.append(var);
        sb.append("\n");
        sb.append(WHERE);
        sb.append(" ");
        sb.append(var + ".");
        sb.append(field.getName());
        sb.append("=:");
        sb.append(field.getName());
        appendOrderByClause(var, sb);
        return sb.toString();
    }

    protected static void appendOrderByClause(final char tablePrefix, final StringBuilder sb) {
        sb.append(ORDER_BY);
        sb.append(tablePrefix + "." + Field.PERSISTENCE_ID);
    }

    public static List<Query> createProvidedQueriesForBusinessObject(final BusinessObject businessObject) {
        List<Query> queries = new ArrayList<Query>();
        Set<String> queryNames = new HashSet<String>();
        for (UniqueConstraint uc : businessObject.getUniqueConstraints()) {
            Query query = createQueryForUniqueConstraint(businessObject, uc);
            queryNames.add(query.getName());
            queries.add(query);

        }
        for (Field f : businessObject.getFields()) {
            if (f.isCollection() == null || !f.isCollection()) {
                Query query = createQueryForField(businessObject, f);
                if (!queryNames.contains(query.getName())) {
                    queries.add(query);
                }
            }
        }
        queries.add(createSelectAllQueryForBusinessObject(businessObject));
        return queries;
    }

    public static Set<String> getAllProvidedQueriesNameForBusinessObject(final BusinessObject businessObject) {
        Set<String> queryNames = new HashSet<String>();
        for (UniqueConstraint uc : businessObject.getUniqueConstraints()) {
            queryNames.add(createQueryNameForUniqueConstraint(businessObject.getQualifiedName(), uc));
        }
        for (Field f : businessObject.getFields()) {
            queryNames.add(createQueryNameForField(businessObject.getQualifiedName(), f));
        }
        queryNames.add(createSelectAllQueryName(businessObject));
        return queryNames;
    }

    public static Query createSelectAllQueryForBusinessObject(final BusinessObject businessObject) {
        if (businessObject == null) {
            throw new IllegalArgumentException("businessObject cannot be null");
        }
        String queryName = createSelectAllQueryName(businessObject);
        String content = createSelectAllQueryContent(businessObject.getQualifiedName());
        return new Query(queryName, content, List.class.getName());
    }

    public static String createSelectAllQueryContent(final String businessObjectName) {
        if (businessObjectName == null) {
            throw new IllegalArgumentException("businessObjectName is null");
        }
        String simpleName = getSimpleBusinessObjectName(businessObjectName);
        char var = Character.toLowerCase(simpleName.charAt(0));
        StringBuilder sb = new StringBuilder();
        sb.append(SELECT);
        sb.append(" ");
        sb.append(var);
        sb.append("\n");
        sb.append(FROM);
        sb.append(" ");
        sb.append(simpleName);
        sb.append(" ");
        sb.append(var);
        appendOrderByClause(var, sb);
        return sb.toString();
    }

    public static String createSelectAllQueryName(final BusinessObject businessObject) {
        return "getAll" + getSimpleBusinessObjectName(businessObject.getQualifiedName());
    }
}
