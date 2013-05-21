/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.search.descriptor;

import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.search.descriptor.FieldDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchProcessInstanceDescriptor;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisorBuilders;

import com.bonitasoft.engine.bpm.process.impl.ProcessInstanceSearchDescriptor;
import com.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import com.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceBuilder;

/**
 * @author Celine Souchet
 */
public class SearchProcessInstanceDescriptorExt extends SearchProcessInstanceDescriptor {

    public SearchProcessInstanceDescriptorExt(final BPMInstanceBuilders instanceBuilders, final SProcessSupervisorBuilders sSupervisorBuilders) {
        super(instanceBuilders, sSupervisorBuilders);

        final SProcessInstanceBuilder instanceBuilder = instanceBuilders.getSProcessInstanceBuilder();

        searchEntityKeys.put(ProcessInstanceSearchDescriptor.STRING_INDEX_1, new FieldDescriptor(SProcessInstance.class, instanceBuilder.getStringIndex1Key()));
        searchEntityKeys.put(ProcessInstanceSearchDescriptor.STRING_INDEX_2, new FieldDescriptor(SProcessInstance.class, instanceBuilder.getStringIndex2Key()));
        searchEntityKeys.put(ProcessInstanceSearchDescriptor.STRING_INDEX_3, new FieldDescriptor(SProcessInstance.class, instanceBuilder.getStringIndex3Key()));
        searchEntityKeys.put(ProcessInstanceSearchDescriptor.STRING_INDEX_4, new FieldDescriptor(SProcessInstance.class, instanceBuilder.getStringIndex4Key()));
        searchEntityKeys.put(ProcessInstanceSearchDescriptor.STRING_INDEX_5, new FieldDescriptor(SProcessInstance.class, instanceBuilder.getStringIndex5Key()));

        processFields.add(instanceBuilder.getStringIndex1Key());
        processFields.add(instanceBuilder.getStringIndex2Key());
        processFields.add(instanceBuilder.getStringIndex3Key());
        processFields.add(instanceBuilder.getStringIndex4Key());
        processFields.add(instanceBuilder.getStringIndex5Key());
        // processInstanceAllFields.put(SProcessInstance.class, processFields);
    }

}
