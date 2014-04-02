/*******************************************************************************
 * Copyright (C) 2011, 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.process.instance.recorder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.core.process.instance.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;

import com.bonitasoft.engine.core.process.instance.model.SRefBusinessDataInstance;
import com.bonitasoft.engine.core.process.instance.model.breakpoint.SBreakpoint;

/**
 * @author Celine Souchet
 */
public class SelectDescriptorBuilderExt extends SelectDescriptorBuilder {

    public static SelectOneDescriptor<Long> getNumberOfBreakpoints() {
        final Map<String, Object> emptyMap = Collections.emptyMap();
        return new SelectOneDescriptor<Long>("getNumberOfBreakpoints", emptyMap, SBreakpoint.class, Long.class);
    }

    public static SelectOneDescriptor<SRefBusinessDataInstance> getSRefBusinessDataInstance(final String name, final long processInstanceId) {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("name", name);
        parameters.put("processInstanceId", processInstanceId);
        return new SelectOneDescriptor<SRefBusinessDataInstance>("getSRefBusinessDataInstance", parameters, SRefBusinessDataInstance.class);
    }

}
