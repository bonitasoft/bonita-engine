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
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.converter.NodeToApplicationConverter;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.persistence.SBonitaReadException;
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
    protected Long handleMissingLayout(final String layoutName, final String applicationToken, final ImportStatus importStatus) throws ImportException, SBonitaReadException {
        importStatus.addError(new ImportError(layoutName, ImportError.Type.LAYOUT));
        SPage layout = getPageService().getPageByName(ApplicationService.DEFAULT_LAYOUT_NAME);
            if(layout == null) {
                throw new ImportException(String.format("Unable to import application with token '%s' because neither the layout '%s', neither the default layout (%s) was found.",
                        applicationToken, layoutName, ApplicationService.DEFAULT_LAYOUT_NAME));
            }

        return layout.getId();
    }

    @Override
    protected Long handleMissingTheme(final String themeName, final String applicationToken, final ImportStatus importStatus) throws ImportException, SBonitaReadException {
        importStatus.addError(new ImportError(themeName, ImportError.Type.THEME));
        SPage theme = getPageService().getPageByName(ApplicationService.DEFAULT_THEME_NAME);
        if(theme == null) {
            throw new ImportException(String.format("Unable to import application with token '%s' because neither the theme '%s', neither the default theme (%s) was found.",
                    applicationToken, themeName, ApplicationService.DEFAULT_THEME_NAME));
        }

        return theme.getId();
    }
}
