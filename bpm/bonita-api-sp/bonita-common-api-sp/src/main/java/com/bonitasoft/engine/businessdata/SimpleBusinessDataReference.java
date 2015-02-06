/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.businessdata;

import org.bonitasoft.engine.bpm.businessdata.BusinessDataDefinition;

/**
 * A <code>SimpleBusinessDataReference</code> is a reference of a {@link BusinessDataDefinition} which is not multiple.
 *
 * @author Matthieu Chaffotte
 */
public interface SimpleBusinessDataReference extends BusinessDataReference {

    /**
     * Returns the identifier of the business data.
     * It can be null, if no business data is attached to the reference.
     *
     * @return the identifier of the business data
     */
    Long getStorageId();

}
