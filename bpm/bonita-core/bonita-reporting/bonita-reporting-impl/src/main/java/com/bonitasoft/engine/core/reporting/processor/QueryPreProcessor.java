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
