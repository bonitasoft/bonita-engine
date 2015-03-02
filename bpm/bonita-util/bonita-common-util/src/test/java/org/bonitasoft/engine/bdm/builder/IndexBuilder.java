/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.bdm.builder;

import org.bonitasoft.engine.bdm.model.Index;

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
