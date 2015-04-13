/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.api.impl.converter;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.converter.ApplicationMenuNodeConverter;
import org.bonitasoft.engine.business.application.converter.ApplicationNodeConverter;
import org.bonitasoft.engine.business.application.converter.ApplicationPageNodeConverter;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.profile.ProfileService;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationNodeConverterExt extends ApplicationNodeConverter {

    public ApplicationNodeConverterExt(final ProfileService profileService, final ApplicationService applicationService, final ApplicationPageNodeConverter applicationPageNodeConverter, final ApplicationMenuNodeConverter applicationMenuNodeConverter, final PageService pageService) {
        super(profileService, applicationService, applicationPageNodeConverter, applicationMenuNodeConverter, pageService);
    }

    @Override
    protected String getLayoutName(final ApplicationNode applicationNode) {
        return applicationNode.getLayout() != null? applicationNode.getLayout() : ApplicationService.DEFAULT_LAYOUT_NAME;
    }

    @Override
    protected Long handleMissingLayout(final ApplicationNode applicationNode, final ImportStatus importStatus) throws ImportException {
        importStatus.addError(new ImportError(applicationNode.getLayout(), ImportError.Type.PAGE));
        return null;
    }
}
