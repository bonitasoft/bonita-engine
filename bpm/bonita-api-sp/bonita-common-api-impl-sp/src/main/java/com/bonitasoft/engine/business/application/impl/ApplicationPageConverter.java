/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application.impl;

import com.bonitasoft.engine.api.converter.Converter;
import com.bonitasoft.engine.business.application.ApplicationPage;

/**
 * @author Elias Ricken de Medeiros
 * @deprecated this class is only intended to convert BOS to SP model objects during the time the SP API is kept as deprecated, and must be deleted after that.
 */
@Deprecated
public class ApplicationPageConverter implements Converter<org.bonitasoft.engine.business.application.ApplicationPage, ApplicationPage> {

    @Override
    public ApplicationPage convert(final org.bonitasoft.engine.business.application.ApplicationPage toConvert) {
        return new ApplicationPageImpl(toConvert);
    }
}
