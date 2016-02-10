/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.process.instance.model.archive.builder.impl;

import org.bonitasoft.engine.core.process.instance.model.archive.builder.impl.SAProcessInstanceBuilderFactoryImpl;

import com.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilderFactory;

/**
 * @author Celine Souchet
 */
public class SAProcessInstanceBuilderFactoryExt extends SAProcessInstanceBuilderFactoryImpl implements SAProcessInstanceBuilderFactory {

    private static final String STRING_INDEX_1 = "stringIndex1";

    private static final String STRING_INDEX_2 = "stringIndex2";

    private static final String STRING_INDEX_3 = "stringIndex3";

    private static final String STRING_INDEX_4 = "stringIndex4";

    private static final String STRING_INDEX_5 = "stringIndex5";

    @Override
    public String getStringIndex1Key() {
        return STRING_INDEX_1;
    }

    @Override
    public String getStringIndex2Key() {
        return STRING_INDEX_2;
    }

    @Override
    public String getStringIndex3Key() {
        return STRING_INDEX_3;
    }

    @Override
    public String getStringIndex4Key() {
        return STRING_INDEX_4;
    }

    @Override
    public String getStringIndex5Key() {
        return STRING_INDEX_5;
    }
}
