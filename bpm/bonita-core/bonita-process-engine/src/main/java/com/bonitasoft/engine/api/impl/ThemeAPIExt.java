/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.api.impl.ThemeAPIImpl;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.theme.Theme;
import org.bonitasoft.engine.theme.ThemeService;
import org.bonitasoft.engine.theme.ThemeType;
import org.bonitasoft.engine.theme.builder.SThemeBuilder;
import org.bonitasoft.engine.theme.builder.SThemeBuilderFactory;
import org.bonitasoft.engine.theme.builder.SThemeUpdateBuilder;
import org.bonitasoft.engine.theme.builder.SThemeUpdateBuilderFactory;
import org.bonitasoft.engine.theme.exception.SThemeCreationException;
import org.bonitasoft.engine.theme.exception.SThemeNotFoundException;
import org.bonitasoft.engine.theme.exception.SThemeUpdateException;
import org.bonitasoft.engine.theme.model.STheme;
import org.bonitasoft.engine.theme.model.SThemeType;

import com.bonitasoft.engine.api.ThemeAPI;
import com.bonitasoft.engine.service.SPModelConvertor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;
import com.bonitasoft.engine.theme.exception.RestoreThemeException;
import com.bonitasoft.engine.theme.exception.SetThemeException;

/**
 * @author Celine Souchet
 */
@AvailableWhenTenantIsPaused
public class ThemeAPIExt extends ThemeAPIImpl implements ThemeAPI {

    @Override
    public TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    public Theme setCustomTheme(final byte[] content, final byte[] cssContent, final ThemeType type) throws SetThemeException {

        validateSetCustomThemeInput(content, cssContent, type);

        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ThemeService themeService = tenantAccessor.getThemeService();
        final SThemeType sType = SThemeType.valueOf(type.name());
        try {
            try {
                final STheme sTheme = themeService.getTheme(SThemeType.valueOf(type.name()), false);
                return updateTheme(sTheme, content, cssContent, sType);
            } catch (final SThemeNotFoundException sThemeNotFoundException) {
                return createTheme(content, cssContent, sType);
            }
        } catch (final BonitaException e) {
            throw new SetThemeException(e);
        } catch (final SBonitaException e) {
            throw new SetThemeException(e);
        }
    }

    private void validateSetCustomThemeInput(final byte[] content, final byte[] cssContent, final ThemeType type) throws SetThemeException {
        if (type == null) {
            throw new SetThemeException("Type is required.");
        }

        if (ThemeType.PORTAL.equals(type)) {
            if (content == null || content.length == 0 || cssContent == null || cssContent.length == 0) {
                throw new SetThemeException("Content and cssContent are required.");
            }
        } else if (ThemeType.MOBILE.equals(type)) {
            if (content == null || content.length == 0) {
                throw new SetThemeException("Content is required.");
            }
        }
    }

    @Override
    public Theme restoreDefaultTheme(final ThemeType type) throws RestoreThemeException {
        if (type == null) {
            throw new RestoreThemeException("Type is required.");
        }

        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ThemeService themeService = tenantAccessor.getThemeService();
        final SThemeType sType = SThemeType.valueOf(type.name());
        try {
            themeService.restoreDefaultTheme(sType);
            return SPModelConvertor.toTheme(themeService.getTheme(sType, true));
        } catch (final SBonitaException e) {
            throw new RestoreThemeException(e);
        }
    }

    private Theme createTheme(final byte[] content, final byte[] cssContent, final SThemeType type) throws CreationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ThemeService themeService = tenantAccessor.getThemeService();
        try {
            final long creationDate = System.currentTimeMillis();

            final SThemeBuilder sThemeBuilder = BuilderFactory.get(SThemeBuilderFactory.class).createNewInstance(content, false, type,
                    creationDate);
            sThemeBuilder.setCSSContent(cssContent);
            final STheme theme = themeService.createTheme(sThemeBuilder.done());
            return SPModelConvertor.toTheme(theme);
        } catch (final SThemeCreationException e) {
            throw new CreationException(e);
        }
    }

    private Theme updateTheme(final STheme sTheme, final byte[] content, final byte[] cssContent, final SThemeType type) throws UpdateException {
        if (sTheme.isDefault()) {
            throw new UpdateException("Can't update a default theme. Theme id = <" + sTheme.getId() + ">, type = <" + sTheme.getType() + ">");
        }

        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ThemeService themeService = tenantAccessor.getThemeService();

        final SThemeUpdateBuilder updateBuilder = BuilderFactory.get(SThemeUpdateBuilderFactory.class).createNewInstance();
        updateBuilder.setContent(content);
        updateBuilder.setCSSContent(cssContent);
        updateBuilder.setType(SThemeType.valueOf(type.name()));
        updateBuilder.setLastUpdateDate(System.currentTimeMillis());
        try {
            return SPModelConvertor.toTheme(themeService.updateTheme(sTheme, updateBuilder.done()));
        } catch (final SThemeUpdateException e) {
            throw new UpdateException(e);
        }

    }

}
