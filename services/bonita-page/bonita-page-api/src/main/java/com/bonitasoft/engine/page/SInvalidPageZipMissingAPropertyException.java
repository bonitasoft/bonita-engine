/*
 * *****************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 * ******************************************************************************
 */

package com.bonitasoft.engine.page;

/**
 * @author Baptiste Mesta
 */
public class SInvalidPageZipMissingAPropertyException extends SInvalidPageZipException {

    private String fields;

    public SInvalidPageZipMissingAPropertyException(String fields) {
        super("Missing fields in the page.properties: " + fields);
        this.fields = fields;
    }

    public String getFields() {
        return fields;
    }
}
