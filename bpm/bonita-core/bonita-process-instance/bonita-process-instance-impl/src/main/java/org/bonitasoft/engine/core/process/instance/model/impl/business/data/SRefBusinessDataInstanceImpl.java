/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.core.process.instance.model.impl.business.data;

import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.impl.SPersistenceObjectImpl;



/**
 * @author Matthieu Chaffotte
 */
public abstract class SRefBusinessDataInstanceImpl extends SPersistenceObjectImpl implements SRefBusinessDataInstance {

    private static final long serialVersionUID = 6616497495062704471L;

    private String name;

    private String dataClassName;

    public SRefBusinessDataInstanceImpl() {
        super();
    }

    @Override
    public String getDiscriminator() {
        return SRefBusinessDataInstanceImpl.class.getName();
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getDataClassName() {
        return dataClassName;
    }

    public void setDataClassName(final String dataClassName) {
        this.dataClassName = dataClassName;
    }

}
