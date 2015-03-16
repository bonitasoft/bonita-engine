/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.builder;

import com.bonitasoft.engine.bdm.model.Index;

/**
 * @author Colin PUY
 */
public class IndexBuilder {

    private Index index = new Index();
    
    public static IndexBuilder anIndex() {
        return new IndexBuilder();
    }
    
    public Index build() {
        return index;
    }
    
    public IndexBuilder withName(String name) {
        index.setName(name);
        return this;
    }
}
