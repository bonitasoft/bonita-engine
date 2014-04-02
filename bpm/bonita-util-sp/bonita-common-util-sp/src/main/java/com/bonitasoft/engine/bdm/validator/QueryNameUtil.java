/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * 
 */
package com.bonitasoft.engine.bdm.validator;

import com.bonitasoft.engine.bdm.UniqueConstraint;

/**
 * @author Romain Bioteau
 * 
 */
public class QueryNameUtil {

    public static String createQueryNameForUniqueConstraint(String businessObjectName, UniqueConstraint uniqueConstraint) {
        if (businessObjectName == null) {
            throw new IllegalArgumentException("businessObjectName cannot be null");
        }
        if (uniqueConstraint == null) {
            throw new IllegalArgumentException("uniqueConstraint cannot be null");
        }
        int lastIndexOf = businessObjectName.lastIndexOf(".");
        if (lastIndexOf != -1) {
            businessObjectName = businessObjectName.substring(lastIndexOf + 1, businessObjectName.length());
        }

        StringBuilder sb = new StringBuilder("get" + businessObjectName + "By");
        for (String f : uniqueConstraint.getFieldNames()) {
            f = Character.toUpperCase(f.charAt(0)) + f.substring(1);
            sb.append(f);
            sb.append("And");
        }
        String name = sb.toString();
        if (name.endsWith("And")) {
            name = name.substring(0, name.length() - 3);
        }
        return name;
    }
}
