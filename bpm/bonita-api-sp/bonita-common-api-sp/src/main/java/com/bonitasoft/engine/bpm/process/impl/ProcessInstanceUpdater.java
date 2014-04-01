/*******************************************************************************
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.process.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matthieu Chaffotte
 */
public class ProcessInstanceUpdater implements Serializable {

    private static final long serialVersionUID = 728214104237982027L;

    public enum ProcessInstanceField {
        STRING_INDEX_1, STRING_INDEX_2, STRING_INDEX_3, STRING_INDEX_4, STRING_INDEX_5;
    }

    private final Map<ProcessInstanceField, Serializable> fields;

    public ProcessInstanceUpdater() {
        fields = new HashMap<ProcessInstanceField, Serializable>(3);
    }

    public void setStringIndex1(final String stringIndex) {
        fields.put(ProcessInstanceField.STRING_INDEX_1, stringIndex);
    }

    public void setStringIndex2(final String stringIndex) {
        fields.put(ProcessInstanceField.STRING_INDEX_2, stringIndex);
    }

    public void setStringIndex3(final String stringIndex) {
        fields.put(ProcessInstanceField.STRING_INDEX_3, stringIndex);
    }

    public void setStringIndex4(final String stringIndex) {
        fields.put(ProcessInstanceField.STRING_INDEX_4, stringIndex);
    }

    public void setStringIndex5(final String stringIndex) {
        fields.put(ProcessInstanceField.STRING_INDEX_5, stringIndex);
    }

    public Map<ProcessInstanceField, Serializable> getFields() {
        return fields;
    }

}
