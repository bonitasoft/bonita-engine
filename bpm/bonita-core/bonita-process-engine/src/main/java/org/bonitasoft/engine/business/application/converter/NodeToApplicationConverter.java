/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.business.application.converter;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.api.impl.validator.ApplicationImportValidator;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.importer.ImportResult;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.builder.SApplicationBuilder;
import org.bonitasoft.engine.business.application.model.builder.SApplicationBuilderFactory;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.exception.profile.SProfileNotFoundException;
import org.bonitasoft.engine.profile.model.SProfile;

/**
 * @author Elias Ricken de Medeiros
 */
public class NodeToApplicationConverter {

    private final ProfileService profileService;
    private final PageService pageService;
    private final ApplicationImportValidator validator;

    public NodeToApplicationConverter(final ProfileService profileService, final PageService pageService, final ApplicationImportValidator validator) {
        this.profileService = profileService;
        this.pageService = pageService;
        this.validator = validator;
    }

    public ImportResult toSApplication(final ApplicationNode applicationNode, final long createdBy) throws SBonitaReadException, ImportException {
        String token = applicationNode.getToken();
        validator.validate(token);
        final ImportStatus importStatus = new ImportStatus(token);
        Long layoutId = getLayoutId(getLayoutName(applicationNode), token, importStatus);
        Long themeId = getThemeId(getThemeName(applicationNode), token, importStatus);
        final SApplicationBuilder builder = BuilderFactory.get(SApplicationBuilderFactory.class).createNewInstance(token,
                applicationNode.getDisplayName(), applicationNode.getVersion(), createdBy, layoutId, themeId);
        builder.setIconPath(applicationNode.getIconPath());
        builder.setDescription(applicationNode.getDescription());
        builder.setState(applicationNode.getState());

        final ImportError importError = setProfile(applicationNode, builder);
        if (importError != null) {
            importStatus.addError(importError);
        }

        final SApplication application = builder.done();
        return new ImportResult(application, importStatus);
    }

    private Long getLayoutId(final String layoutName, final String applicationToken, final ImportStatus importStatus) throws SBonitaReadException, ImportException {
        SPage layout = pageService.getPageByName(layoutName);
        if (layout == null) {
            return handleMissingLayout(layoutName, applicationToken, importStatus);
        }
        return layout.getId();
    }

    private Long getThemeId(final String themeName, final String applicationToken, final ImportStatus importStatus) throws SBonitaReadException, ImportException {
        SPage theme = pageService.getPageByName(themeName);
        if (theme == null) {
            return handleMissingTheme(themeName, applicationToken, importStatus);
        }
        return theme.getId();
    }

    protected Long handleMissingLayout(final String layoutName, final String applicationToken, final ImportStatus importStatus) throws ImportException, SBonitaReadException {
        throw new ImportException(String.format("Unable to import application with token '%s' because the layout '%s' was not found.",
                applicationToken, layoutName));
    }

    protected Long handleMissingTheme(final String themeName, final String applicationToken, final ImportStatus importStatus) throws ImportException, SBonitaReadException {
        throw new ImportException(String.format("Unable to import application with token '%s' because the theme '%s' was not found.",
                applicationToken, themeName));
    }

    protected String getLayoutName(final ApplicationNode applicationNode) {
        return ApplicationService.DEFAULT_LAYOUT_NAME;
    }

    protected String getThemeName(final ApplicationNode applicationNode) {
        return ApplicationService.DEFAULT_THEME_NAME;
    }

    private ImportError setProfile(final ApplicationNode applicationNode, final SApplicationBuilder builder) {
        ImportError importError = null;
        if (applicationNode.getProfile() != null) {
            try {
                final SProfile profile = profileService.getProfileByName(applicationNode.getProfile());
                builder.setProfileId(profile.getId());
            } catch (final SProfileNotFoundException | SBonitaReadException e) {
                importError = new ImportError(applicationNode.getProfile(), ImportError.Type.PROFILE);
            }
        }
        return importError;
    }

    protected PageService getPageService() {
        return pageService;
    }
}
