/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.api.impl;

import java.util.Date;

import org.bonitasoft.engine.api.ThemeAPI;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.service.ModelConvertor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.theme.Theme;
import org.bonitasoft.engine.theme.ThemeService;
import org.bonitasoft.engine.theme.ThemeType;
import org.bonitasoft.engine.theme.exception.SThemeNotFoundException;
import org.bonitasoft.engine.theme.model.SThemeType;

/**
 * @author Celine Souchet
 */
public class ThemeAPIImpl implements ThemeAPI {

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
    public Theme getCurrentTheme(final ThemeType type) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ThemeService themeService = tenantAccessor.getThemeService();
        try {
            return ModelConvertor.toTheme(themeService.getLastModifiedTheme(SThemeType.valueOf(type.name())));
        } catch (SThemeNotFoundException e) {
            throw new RuntimeException(e);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public Theme getDefaultTheme(final ThemeType type) {
        return getThemeByType(type, true);
    }

    private Theme getThemeByType(final ThemeType type, final boolean isDefault) {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ThemeService themeService = tenantAccessor.getThemeService();
        try {
            return ModelConvertor.toTheme(themeService.getTheme(SThemeType.valueOf(type.name()), isDefault));
        } catch (SThemeNotFoundException e) {
            throw new RuntimeException(e);
        } catch (final SBonitaException e) {
            throw new RetrieveException(e);
        }
    }

    @Override
    public Date getLastUpdateDate(final ThemeType type) {
        return getCurrentTheme(type).getLastUpdatedDate();
    }

}
