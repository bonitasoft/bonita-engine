/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.InternalProfiles;
import org.bonitasoft.engine.business.application.importer.ImportResult;
import org.bonitasoft.engine.business.application.importer.validator.ApplicationImportValidator;
import org.bonitasoft.engine.business.application.model.SApplicationWithIcon;
import org.bonitasoft.engine.business.application.xml.AbstractApplicationNode;
import org.bonitasoft.engine.business.application.xml.ApplicationLinkNode;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.profile.ProfileService;
import org.bonitasoft.engine.profile.exception.profile.SProfileNotFoundException;
import org.bonitasoft.engine.profile.model.SProfile;
import org.springframework.stereotype.Component;

/**
 * @author Elias Ricken de Medeiros
 */
@Component
public class NodeToApplicationConverter {

    private final ProfileService profileService;
    private final PageService pageService;
    private final ApplicationImportValidator validator;

    public NodeToApplicationConverter(final ProfileService profileService, final PageService pageService,
            final ApplicationImportValidator validator) {
        this.profileService = profileService;
        this.pageService = pageService;
        this.validator = validator;
    }

    public ImportResult toSApplication(final AbstractApplicationNode applicationNode, byte[] iconContent,
            String iconMimeType, final long createdBy, final boolean editable)
            throws SBonitaReadException, ImportException {
        String token = applicationNode.getToken();
        validator.validate(token);

        final ImportStatus importStatus = new ImportStatus(token);
        final long currentDate = System.currentTimeMillis();
        SApplicationWithIcon application = new SApplicationWithIcon();
        application.setToken(token);
        application.setDisplayName(applicationNode.getDisplayName());
        application.setVersion(applicationNode.getVersion());
        application.setCreationDate(currentDate);
        application.setLastUpdateDate(currentDate);
        application.setCreatedBy(createdBy);
        application.setIconPath(applicationNode.getIconPath());
        application.setDescription(applicationNode.getDescription());
        application.setState(applicationNode.getState());
        application.setEditable(editable);
        application.setLink(applicationNode instanceof ApplicationLinkNode);

        if (applicationNode instanceof ApplicationNode legacy) {
            application.setLayoutId(getLayoutId(getLayoutName(legacy), token, importStatus));
            application.setThemeId(getThemeId(getThemeName(legacy), token, importStatus));
            application.setIconContent(iconContent);
            application.setIconMimeType(iconMimeType);
        }

        if (applicationNode.getProfile() != null) {
            setProfile(applicationNode.getProfile(), application, importStatus);
        }

        return new ImportResult(application, importStatus);
    }

    private Long getLayoutId(final String layoutName, final String applicationToken, final ImportStatus importStatus)
            throws SBonitaReadException, ImportException {
        SPage layout = pageService.getPageByName(layoutName);
        if (layout == null) {
            return handleMissingLayout(layoutName, applicationToken, importStatus);
        }
        return layout.getId();
    }

    private Long getThemeId(final String themeName, final String applicationToken, final ImportStatus importStatus)
            throws SBonitaReadException, ImportException {
        SPage theme = pageService.getPageByName(themeName);
        if (theme == null) {
            return handleMissingTheme(themeName, applicationToken, importStatus);
        }
        return theme.getId();
    }

    protected Long handleMissingLayout(final String layoutName, final String applicationToken,
            final ImportStatus importStatus) throws ImportException, SBonitaReadException {
        throw new ImportException(
                String.format("Unable to import application with token '%s' because the layout '%s' was not found.",
                        applicationToken, layoutName));
    }

    protected Long handleMissingTheme(final String themeName, final String applicationToken,
            final ImportStatus importStatus) throws ImportException, SBonitaReadException {
        throw new ImportException(
                String.format("Unable to import application with token '%s' because the theme '%s' was not found.",
                        applicationToken, themeName));
    }

    private String getLayoutName(final ApplicationNode applicationNode) {
        return applicationNode.getLayout() != null ? applicationNode.getLayout()
                : ApplicationService.DEFAULT_LAYOUT_NAME;
    }

    private String getThemeName(final ApplicationNode applicationNode) {
        return applicationNode.getTheme() != null ? applicationNode.getTheme() : ApplicationService.DEFAULT_THEME_NAME;
    }

    private void setProfile(String profileName, SApplicationWithIcon application, ImportStatus importStatus) {
        InternalProfiles internalProfileByApplicationNodeProfile = InternalProfiles
                .getInternalProfileByProfileName(profileName);
        if (internalProfileByApplicationNodeProfile == null) {
            try {
                final SProfile profile = profileService.getProfileByName(profileName);
                application.setProfileId(profile.getId());
            } catch (final SProfileNotFoundException | SBonitaReadException e) {
                importStatus.addError(new ImportError(profileName, ImportError.Type.PROFILE));
            }
        } else {
            application.setInternalProfile(internalProfileByApplicationNodeProfile.getProfileName());
            application.setProfileId(null);
        }
    }

    protected PageService getPageService() {
        return pageService;
    }
}
