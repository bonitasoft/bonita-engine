/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.page.impl;

import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilderFactory;

import com.bonitasoft.engine.page.SPageLogBuilderFactory;

/**
 * @author Matthieu Chaffotte
 */
public class SPageLogBuilderFactoryImpl extends CRUDELogBuilderFactory implements SPageLogBuilderFactory {

    public static final int PAGE_INDEX = 1;

    public static final String PAGE_INDEX_NAME = "numericIndex2";

    @Override
    public String getObjectIdKey() {
        return PAGE_INDEX_NAME;
    }

}
