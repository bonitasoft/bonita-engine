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
import org.bonitasoft.engine.api.impl.validator.ApplicationTokenValidator;
import org.bonitasoft.engine.api.impl.validator.ValidationStatus;
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
    private final ApplicationTokenValidator tokenValidator;

    public NodeToApplicationConverter(final ProfileService profileService, final PageService pageService, final ApplicationTokenValidator tokenValidator) {
        this.profileService = profileService;
        this.pageService = pageService;
        this.tokenValidator = tokenValidator;
    }

    public ImportResult toSApplication(final ApplicationNode applicationNode, final long createdBy) throws SBonitaReadException, ImportException {
        String token = applicationNode.getToken();
        validateToken(token);
        final ImportStatus importStatus = new ImportStatus(token);
        Long layoutId = getPageId(getLayoutName(applicationNode), token, importStatus);
        Long themeId = getPageId(getThemeName(applicationNode), token, importStatus);
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

    private void validateToken(final String token) throws ImportException {
        ValidationStatus validationStatus = tokenValidator.validate(token);
        if(!validationStatus.isValid()) {
            throw new ImportException(validationStatus.getMessage());
        }
    }

    private Long getPageId(final String pageName, final String applicationToken, final ImportStatus importStatus) throws SBonitaReadException, ImportException {
        SPage layout = pageService.getPageByName(pageName);
        if (layout == null) {
            return handleMissingPage(pageName, applicationToken, importStatus);
        }
        return layout.getId();
    }

    protected Long handleMissingPage(final String pageName, final String applicationToken, final ImportStatus importStatus) throws ImportException {
        throw new ImportException(String.format("Unable to import application with token '%s' because the page '%s' was not found.",
                applicationToken, pageName));
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
            } catch (final SProfileNotFoundException e) {
                importError = new ImportError(applicationNode.getProfile(), ImportError.Type.PROFILE);
            }
        }
        return importError;
    }

}
