/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.search.descriptor;

import org.bonitasoft.engine.core.process.instance.model.archive.SAProcessInstance;
import org.bonitasoft.engine.search.FieldDescriptor;
import org.bonitasoft.engine.search.SearchArchivedProcessInstancesDescriptor;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisorBuilders;

import com.bonitasoft.engine.core.process.instance.model.archive.builder.SAProcessInstanceBuilder;
import com.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import com.bonitasoft.engine.search.ArchivedProcessInstancesSearchDescriptor;

/**
 * @author Celine Souchet
 */
public class SearchArchivedProcessInstancesDescriptorExt extends SearchArchivedProcessInstancesDescriptor {

    public SearchArchivedProcessInstancesDescriptorExt(final BPMInstanceBuilders instanceBuilders, final SProcessSupervisorBuilders sSupervisorBuilders) {
        super(instanceBuilders, sSupervisorBuilders);

        final SAProcessInstanceBuilder instanceBuilder = instanceBuilders.getSAProcessInstanceBuilder();

        searchEntityKeys.put(ArchivedProcessInstancesSearchDescriptor.STRING_INDEX_1,
                new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getStringIndex1Key()));
        searchEntityKeys.put(ArchivedProcessInstancesSearchDescriptor.STRING_INDEX_2,
                new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getStringIndex2Key()));
        searchEntityKeys.put(ArchivedProcessInstancesSearchDescriptor.STRING_INDEX_3,
                new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getStringIndex3Key()));
        searchEntityKeys.put(ArchivedProcessInstancesSearchDescriptor.STRING_INDEX_4,
                new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getStringIndex4Key()));
        searchEntityKeys.put(ArchivedProcessInstancesSearchDescriptor.STRING_INDEX_5,
                new FieldDescriptor(SAProcessInstance.class, instanceBuilder.getStringIndex5Key()));

        processInstanceFields.add(instanceBuilder.getStringIndex1Key());
        processInstanceFields.add(instanceBuilder.getStringIndex2Key());
        processInstanceFields.add(instanceBuilder.getStringIndex3Key());
        processInstanceFields.add(instanceBuilder.getStringIndex4Key());
        processInstanceFields.add(instanceBuilder.getStringIndex5Key());
        // archivedProcessInstanceAllFields.put(SAProcessInstance.class, processInstanceFields);
    }

}
