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
import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.search.descriptor.FieldDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchArchivedProcessInstancesDescriptor;

import com.bonitasoft.engine.bpm.flownode.ArchivedProcessInstancesSearchDescriptor;
import com.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilderFactory;

/**
 * @author Celine Souchet
 */
public class SearchArchivedProcessInstanceDescriptorExt extends SearchArchivedProcessInstancesDescriptor {

    public SearchArchivedProcessInstanceDescriptorExt() {
        super();

        final SAProcessInstanceBuilderFactory keyProvider = BuilderFactory.get(SAProcessInstanceBuilderFactory.class);

        searchEntityKeys.put(ArchivedProcessInstancesSearchDescriptor.STRING_INDEX_1,
                new FieldDescriptor(SAProcessInstance.class, keyProvider.getStringIndex1Key()));
        searchEntityKeys.put(ArchivedProcessInstancesSearchDescriptor.STRING_INDEX_2,
                new FieldDescriptor(SAProcessInstance.class, keyProvider.getStringIndex2Key()));
        searchEntityKeys.put(ArchivedProcessInstancesSearchDescriptor.STRING_INDEX_3,
                new FieldDescriptor(SAProcessInstance.class, keyProvider.getStringIndex3Key()));
        searchEntityKeys.put(ArchivedProcessInstancesSearchDescriptor.STRING_INDEX_4,
                new FieldDescriptor(SAProcessInstance.class, keyProvider.getStringIndex4Key()));
        searchEntityKeys.put(ArchivedProcessInstancesSearchDescriptor.STRING_INDEX_5,
                new FieldDescriptor(SAProcessInstance.class, keyProvider.getStringIndex5Key()));

        processInstanceFields.add(keyProvider.getStringIndex1Key());
        processInstanceFields.add(keyProvider.getStringIndex2Key());
        processInstanceFields.add(keyProvider.getStringIndex3Key());
        processInstanceFields.add(keyProvider.getStringIndex4Key());
        processInstanceFields.add(keyProvider.getStringIndex5Key());
        // archivedProcessInstanceAllFields.put(SAProcessInstance.class, processInstanceFields);
    }

}
