/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.reporting.processor;

import static com.bonitasoft.engine.core.reporting.processor.Vendor.ORACLE;
import static com.bonitasoft.engine.core.reporting.processor.Vendor.SQLSERVER;


public class QueryPreProcessor {

    public String preProcessFor(Vendor vendor, String queryString) {
        if (ORACLE.equals(vendor) || SQLSERVER.equals(vendor)) {
            return queryString.replaceAll("(?i)=\\s+FALSE", "= 0");
        }
        return queryString;
    }
}
