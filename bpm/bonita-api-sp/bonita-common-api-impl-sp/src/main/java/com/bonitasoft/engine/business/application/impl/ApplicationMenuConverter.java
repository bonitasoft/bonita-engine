/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application.impl;

import org.bonitasoft.engine.business.application.ApplicationMenu;

import com.bonitasoft.engine.api.converter.Converter;

/**
 * @author Elias Ricken de Medeiros
 * @deprecated this class is only intended to convert BOS to SP model objects during the time the SP API is kept as deprecated, and must be deleted after that.
 */
@Deprecated
public class ApplicationMenuConverter implements Converter<ApplicationMenu, com.bonitasoft.engine.business.application.ApplicationMenu> {

    @Override
    public com.bonitasoft.engine.business.application.ApplicationMenu convert(final ApplicationMenu toConvert) {
        return new ApplicationMenuImpl(toConvert);
    }

}
