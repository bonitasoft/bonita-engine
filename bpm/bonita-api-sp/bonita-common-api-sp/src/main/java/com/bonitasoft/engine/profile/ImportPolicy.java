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
 * 
 */
public enum ImportPolicy {

    DELETE_EXISTING, REPLACE_DUPLICATES, FAIL_ON_DUPLICATES, IGNORE_DUPLICATES; // , MERGE_DUPLICATES (Merge should retain existing IDs)

}
