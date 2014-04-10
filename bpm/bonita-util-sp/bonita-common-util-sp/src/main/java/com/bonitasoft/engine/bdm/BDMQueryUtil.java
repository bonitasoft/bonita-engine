/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * 
 */
package com.bonitasoft.engine.bdm;

/**
 * @author Romain Bioteau
 * 
 */
public class BDMQueryUtil {

    private static final String AND_SEPARATOR = "And";

    private static final String LOGIC_AND = " AND ";

    private static final String FROM = "FROM";

    private static final String SELECT = "SELECT";

    private static final String WHERE = "WHERE";

    public static String createQueryNameForUniqueConstraint(String businessObjectName, UniqueConstraint uniqueConstraint) {
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

    public static Query createQueryForUniqueConstraint(BusinessObject businessObject, UniqueConstraint uniqueConstraint) {
        String name = createQueryNameForUniqueConstraint(businessObject.getQualifiedName(), uniqueConstraint);
        String content = createQueryContentForUniqueConstraint(businessObject.getQualifiedName(), uniqueConstraint);
        Query q = new Query(name, content, businessObject.getQualifiedName());
        for (String fieldName : uniqueConstraint.getFieldNames()) {
            Field f = getField(fieldName, businessObject);
            q.addQueryParameter(f.getName(), f.getType().getClazz().getName());
        }
        return q;
    }

    public static Field getField(String fieldName, BusinessObject businessObject) {
        for (Field f : businessObject.getFields()) {
            if (f.getName().equals(fieldName)) {
                return f;
            }
        }
        throw new IllegalArgumentException(fieldName + " doesn't exist in " + businessObject.getQualifiedName());
    }

    public static String createQueryContentForUniqueConstraint(String businessObjectName, UniqueConstraint uniqueConstraint) {
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
}
