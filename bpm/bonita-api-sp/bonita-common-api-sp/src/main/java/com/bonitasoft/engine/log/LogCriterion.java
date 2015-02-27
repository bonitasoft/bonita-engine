/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.log;

/**
 * @author Bole Zhang
 */
public enum LogCriterion {

    /**
     * creation time ascending order
     */
    CREATION_DATE_ASC,

    /**
     * created by ascending order
     */
    CREATED_BY_ASC,

    /**
     * SeverityLevel ascending order
     */
    SEVERITY_LEVEL_ASC,

    /**
     * creation time descending order
     */
    CREATION_DATE_DESC,

    /**
     * created by descending order
     */
    CREATED_BY_DESC,

    /**
     * SeverityLevel descending order
     */
    SEVERITY_LEVEL_DESC,

    /**
     * 
     */
    DEFAULT

}
