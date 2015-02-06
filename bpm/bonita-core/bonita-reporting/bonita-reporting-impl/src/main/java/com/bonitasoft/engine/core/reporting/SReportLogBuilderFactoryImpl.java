/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.reporting;

import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilderFactory;

/**
 * @author Matthieu Chaffotte
 */
public class SReportLogBuilderFactoryImpl extends CRUDELogBuilderFactory implements SReportLogBuilderFactory {

    public static final int REPORT_INDEX = 1;

    public static final String REPORT_INDEX_NAME = "numericIndex2";

    @Override
    public String getObjectIdKey() {
        return REPORT_INDEX_NAME;
    }


}
