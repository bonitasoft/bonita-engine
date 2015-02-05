/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.bdm;

import java.io.Serializable;

/**
 * @author Romain Bioteau
 * @author Matthieu Chaffotte
 */
public interface Entity extends Serializable {

    Long getPersistenceId();

    Long getPersistenceVersion();

}
