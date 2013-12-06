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
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.looknfeel.Theme;
import org.bonitasoft.engine.looknfeel.ThemeType;
import org.bonitasoft.engine.looknfeel.exception.ThemeNotFoundException;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

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
    public Theme getCurrentTheme(ThemeType type) throws ThemeNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Theme getDefaultTheme(ThemeType type) throws ThemeNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getLastUpdateDate(ThemeType type) throws ThemeNotFoundException {
        // TODO Auto-generated method stub
        return null;
    }

}
