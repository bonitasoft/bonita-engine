/*******************************************************************************
 * Copyright (C) 2011-2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.profile;

/**
 * @author Zhao Na
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @author Baptiste Mesta
 * 
 */
public enum ImportPolicy {

    /**
     * Delete all profiles before importing the XML file
     */
    DELETE_EXISTING,
    /**
     * In case of conflict on the name, it replaces completely the profile including profile entries and profile mappings
     */
    REPLACE_DUPLICATES,
    /**
     * Throw exception in case of already existing profile with the same name
     */
    FAIL_ON_DUPLICATES,
    /**
     * In case of conflict on the name, the profile is not imported and the existing profile is kept unmodified
     */
    IGNORE_DUPLICATES;

}
