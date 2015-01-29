/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.page.impl;

import com.bonitasoft.engine.api.converter.Converter;
import org.bonitasoft.engine.page.Page;

/**
 * @author Elias Ricken de Medeiros
 */
public class PageConverter implements Converter<Page, com.bonitasoft.engine.page.Page> {

    @Override
    public com.bonitasoft.engine.page.Page convert(final Page toConvert) {
        return new PageImpl(toConvert);
    }
}
