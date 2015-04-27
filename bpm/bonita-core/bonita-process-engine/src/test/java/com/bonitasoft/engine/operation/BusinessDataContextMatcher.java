/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.operation;

import org.bonitasoft.engine.commons.Container;
import org.mockito.ArgumentMatcher;

/**
 * @author Elias Ricken de Medeiros
 */
public class BusinessDataContextMatcher extends ArgumentMatcher<BusinessDataContext> {

    private final BusinessDataContext expectedContext;

    public BusinessDataContextMatcher(BusinessDataContext expectedContext) {
        this.expectedContext = expectedContext;
    }

    @Override
    public boolean matches(final Object argument) {
        if (!(argument instanceof BusinessDataContext)) {
            return false;
        }
        BusinessDataContext actualContext = (BusinessDataContext) argument;
        Container expectedContainer = expectedContext.getContainer();
        Container actualContainer = actualContext.getContainer();
        return expectedContext.getName().equals(actualContext.getName()) && expectedContainer.getId() == actualContainer.getId()
                && expectedContainer.getType().equals(actualContainer.getType());
    }
}
