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
 * @author Laurent Leseigneur
 */
@AvailableWhenTenantIsPaused
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
