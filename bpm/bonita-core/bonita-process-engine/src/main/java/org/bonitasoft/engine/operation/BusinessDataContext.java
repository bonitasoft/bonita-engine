/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package org.bonitasoft.engine.operation;

import org.bonitasoft.engine.commons.Container;

/**
 * @author Elias Ricken de Medeiros
 */
public class BusinessDataContext {

    private String name;

    private Container container;

    public BusinessDataContext(final String name, final Container container) {
        this.name = name;
        this.container = container;
    }

    public String getName() {
        return name;
    }

    public Container getContainer() {
        return container;
    }

}
