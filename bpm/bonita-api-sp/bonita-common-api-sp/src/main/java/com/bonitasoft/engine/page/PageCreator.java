/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Laurent Leseigneur
 */
public interface PageCreator extends Serializable {

  
    public enum PageField {
        NAME, DESCRIPTION;
    }

    
    public PageCreator setDescription(final String description);

    public Map<PageField, Serializable> getFields();

}
