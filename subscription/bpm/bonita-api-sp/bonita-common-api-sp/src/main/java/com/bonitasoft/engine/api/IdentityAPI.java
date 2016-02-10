/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

/**
 * This API does not contain anymore anything Subscription-specific. It is here for backwards compatibility. All usable methods come from superinterface
 * {@link org.bonitasoft.engine.api.IdentityAPI}.
 * 
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @see org.bonitasoft.engine.api.IdentityAPI
 */
public interface IdentityAPI extends org.bonitasoft.engine.api.IdentityAPI {

    // In order to not have an API break, this interface is still present even if it has no methods.
}
