/*******************************************************************************
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matthieu Chaffotte
 */
public class ProcessInstanceUpdateDescriptor implements Serializable {

    private static final long serialVersionUID = 728214104237982027L;

    public enum ProcessInstanceField {
        STRING_INDEX_1, STRING_INDEX_2, STRING_INDEX_3, STRING_INDEX_4, STRING_INDEX_5;
    }

    private final Map<ProcessInstanceField, Serializable> fields;

    public ProcessInstanceUpdateDescriptor() {
        fields = new HashMap<ProcessInstanceField, Serializable>(3);
    }

    public void updateStringIndex1(final String stringIndex) {
        fields.put(ProcessInstanceField.STRING_INDEX_1, stringIndex);
    }

    public void updateStringIndex2(final String stringIndex) {
        fields.put(ProcessInstanceField.STRING_INDEX_2, stringIndex);
    }

    public void updateStringIndex3(final String stringIndex) {
        fields.put(ProcessInstanceField.STRING_INDEX_3, stringIndex);
    }

    public void updateStringIndex4(final String stringIndex) {
        fields.put(ProcessInstanceField.STRING_INDEX_4, stringIndex);
    }

    public void updateStringIndex5(final String stringIndex) {
        fields.put(ProcessInstanceField.STRING_INDEX_5, stringIndex);
    }

    public Map<ProcessInstanceField, Serializable> getFields() {
        return fields;
    }

}
