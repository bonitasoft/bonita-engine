/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.businessdata;

import java.io.Serializable;

/**
 * A <code>BusinessDataReference</code> defines all needed fields to retrieve a business data.
 *
 * @author Matthieu Chaffotte
 */
public interface BusinessDataReference extends Serializable {

    /**
     * Returns the name of the business data.
     *
     * @return the name of the business data
     */
    String getName();

    /**
     * Returns the type of the business data.
     *
     * @return the type of the business data
     */
    String getType();

}
