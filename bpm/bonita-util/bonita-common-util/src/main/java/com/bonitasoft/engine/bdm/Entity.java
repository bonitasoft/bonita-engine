/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/
package com.bonitasoft.engine.bdm;

import java.io.Serializable;

/**
 * @author Romain Bioteau
 * @author Matthieu Chaffotte
 * @deprecated since version 7.0.0.  This class was only introduced to avoid an API break of Bonita Engine Subscription version. Use {@link org.bonitasoft.engine.bdm.Entity} instead.
 */
@Deprecated
public interface Entity extends Serializable {

    Long getPersistenceId();

    Long getPersistenceVersion();
}
