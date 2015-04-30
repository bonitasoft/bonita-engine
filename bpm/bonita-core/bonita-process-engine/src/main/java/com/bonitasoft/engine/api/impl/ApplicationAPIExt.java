/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.api.impl;

import com.bonitasoft.engine.api.impl.converter.ApplicationModelConverterExt;
import com.bonitasoft.engine.api.impl.converter.ApplicationNodeConverterExt;
import org.bonitasoft.engine.api.impl.converter.ApplicationModelConverter;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.converter.ApplicationMenuNodeConverter;
import org.bonitasoft.engine.business.application.converter.ApplicationNodeConverter;
import org.bonitasoft.engine.business.application.converter.ApplicationPageNodeConverter;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.profile.ProfileService;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationAPIExt extends org.bonitasoft.engine.api.impl.ApplicationAPIImpl {

    @Override
    protected ApplicationModelConverter getApplicationModelConverter(final PageService pageService) {
        return new ApplicationModelConverterExt(pageService);
    }

    @Override
    protected ApplicationNodeConverter getApplicationNodeConverter(final ApplicationService applicationService, final PageService pageService, final ProfileService profileService, final ApplicationPageNodeConverter applicationPageNodeConverter, final ApplicationMenuNodeConverter applicationMenuNodeConverter) {
        return new ApplicationNodeConverterExt(profileService, applicationService, applicationPageNodeConverter, applicationMenuNodeConverter, pageService);
    }
}
