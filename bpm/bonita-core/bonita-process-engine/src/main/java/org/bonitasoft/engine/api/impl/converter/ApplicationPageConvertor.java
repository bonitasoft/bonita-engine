/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.api.impl.converter;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.business.application.ApplicationPage;
import org.bonitasoft.engine.business.application.impl.ApplicationPageImpl;
import org.bonitasoft.engine.business.application.model.SApplicationPage;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationPageConvertor {

    public ApplicationPage toApplicationPage(final SApplicationPage sApplicationPage) {
        final ApplicationPageImpl appPage = new ApplicationPageImpl(sApplicationPage.getApplicationId(), sApplicationPage.getPageId(),
                sApplicationPage.getToken());
        appPage.setId(sApplicationPage.getId());
        return appPage;
    }

    public List<ApplicationPage> toApplicationPage(final List<SApplicationPage> sApplicationPages) {
        final List<ApplicationPage> appPages = new ArrayList<ApplicationPage>(sApplicationPages.size());
        for (final SApplicationPage sApplicationPage : sApplicationPages) {
            appPages.add(toApplicationPage(sApplicationPage));
        }
        return appPages;
    }
}
