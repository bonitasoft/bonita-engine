/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.search.descriptor;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.search.descriptor.FieldDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor;

import com.bonitasoft.engine.bpm.process.impl.ProcessInstanceSearchDescriptor;
import com.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceBuilderFactory;

/**
 * @author Celine Souchet
 */
public class SearchProcessInstanceDescriptorExt extends SearchProcessInstanceDescriptor {

    public SearchProcessInstanceDescriptorExt() {
        super();

        final SProcessInstanceBuilderFactory keyProvider = BuilderFactory.get(SProcessInstanceBuilderFactory.class);

        searchEntityKeys.put(ProcessInstanceSearchDescriptor.STRING_INDEX_1, new FieldDescriptor(SProcessInstance.class, keyProvider.getStringIndex1Key()));
        searchEntityKeys.put(ProcessInstanceSearchDescriptor.STRING_INDEX_2, new FieldDescriptor(SProcessInstance.class, keyProvider.getStringIndex2Key()));
        searchEntityKeys.put(ProcessInstanceSearchDescriptor.STRING_INDEX_3, new FieldDescriptor(SProcessInstance.class, keyProvider.getStringIndex3Key()));
        searchEntityKeys.put(ProcessInstanceSearchDescriptor.STRING_INDEX_4, new FieldDescriptor(SProcessInstance.class, keyProvider.getStringIndex4Key()));
        searchEntityKeys.put(ProcessInstanceSearchDescriptor.STRING_INDEX_5, new FieldDescriptor(SProcessInstance.class, keyProvider.getStringIndex5Key()));

        processFields.add(keyProvider.getStringIndex1Key());
        processFields.add(keyProvider.getStringIndex2Key());
        processFields.add(keyProvider.getStringIndex3Key());
        processFields.add(keyProvider.getStringIndex4Key());
        processFields.add(keyProvider.getStringIndex5Key());
        // processInstanceAllFields.put(SProcessInstance.class, processFields);
    }
}
