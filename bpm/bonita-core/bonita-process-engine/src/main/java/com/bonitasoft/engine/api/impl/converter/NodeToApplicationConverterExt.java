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
import org.bonitasoft.engine.api.impl.validator.ApplicationImportValidator;
import org.bonitasoft.engine.business.application.converter.NodeToApplicationConverter;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.profile.ProfileService;

/**
 * @author Elias Ricken de Medeiros
 */
public class NodeToApplicationConverterExt extends NodeToApplicationConverter {

    public NodeToApplicationConverterExt(final ProfileService profileService, final PageService pageService, final ApplicationImportValidator importValidator) {
        super(profileService, pageService, importValidator);
    }

    @Override
    protected String getLayoutName(final ApplicationNode applicationNode) {
        return applicationNode.getLayout() != null ? applicationNode.getLayout() : super.getLayoutName(applicationNode);
    }

    @Override
    protected String getThemeName(final ApplicationNode applicationNode) {
        return applicationNode.getTheme() != null ? applicationNode.getTheme() : super.getThemeName(applicationNode);
    }

    @Override
    protected Long handleMissingPage(final String pageName, final String applicationToken, final ImportStatus importStatus) throws ImportException {
        importStatus.addError(new ImportError(pageName, ImportError.Type.PAGE));
        return null;
    }
}
