/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.process.instance.api;

import java.util.List;

import org.bonitasoft.engine.persistence.SBonitaReadException;

import com.bonitasoft.engine.core.process.instance.api.exceptions.SRefBusinessDataInstanceCreationException;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SRefBusinessDataInstanceModificationException;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SRefBusinessDataInstanceNotFoundException;
import com.bonitasoft.engine.core.process.instance.model.SMultiRefBusinessDataInstance;
import com.bonitasoft.engine.core.process.instance.model.SRefBusinessDataInstance;
import com.bonitasoft.engine.core.process.instance.model.SSimpleRefBusinessDataInstance;

/**
 * @author Matthieu Chaffotte
 */
public interface RefBusinessDataService {

    String NEW_REF_BUISNESS_DATA_INSTANCE_ADDED = "New reference to a business data added";

    String REF_BUSINESS_DATA_INSTANCE = "REF_BUSINESS_DATA_INSTANCE";

    SRefBusinessDataInstance getRefBusinessDataInstance(String name, long processInstanceId) throws SRefBusinessDataInstanceNotFoundException,
    SBonitaReadException;

    SRefBusinessDataInstance addRefBusinessDataInstance(SRefBusinessDataInstance instance) throws SRefBusinessDataInstanceCreationException;

    void updateRefBusinessDataInstance(SSimpleRefBusinessDataInstance refBusinessDataInstance, Long dataId)
            throws SRefBusinessDataInstanceModificationException;

    void updateRefBusinessDataInstance(SMultiRefBusinessDataInstance refBusinessDataInstance, List<Long> dataIds)
            throws SRefBusinessDataInstanceModificationException;

}
