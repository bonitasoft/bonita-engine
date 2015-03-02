/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.core.process.instance.model.builder.impl.business.data;


import org.bonitasoft.engine.core.process.instance.model.builder.business.data.SRefBusinessDataInstanceLogBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.business.data.SRefBusinessDataInstanceLogBuilderFactory;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.CRUDELogBuilderFactory;

/**
 * @author Matthieu Chaffotte
 */
public class SRefBusinessDataInstanceLogBuilderFactoryImpl extends CRUDELogBuilderFactory implements SRefBusinessDataInstanceLogBuilderFactory {

    @Override
    public SRefBusinessDataInstanceLogBuilder createNewInstance() {
        return new SRefBusinessDataInstanceLogBuilderImpl();
    }

    @Override
    public String getObjectIdKey() {
        return "numericIndex2";
    }

}
