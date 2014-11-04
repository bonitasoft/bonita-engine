/*******************************************************************************
 * Copyright (C) 2012, 2014 BonitaSoft S.A.
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
 * Updater object to update the string indexes.
 * 
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @version 6.3.5
 * @since 6.0.0
 * @see ProcessInstance
 */
public class ProcessInstanceUpdater implements Serializable {

    private static final long serialVersionUID = 728214104237982027L;

    public enum ProcessInstanceField {
        /**
         * Corresponding to the first string index
         */
        STRING_INDEX_1,
        /**
         * Corresponding to the second string index
         */
        STRING_INDEX_2,
        /**
         * Corresponding to the third string index
         */
        STRING_INDEX_3,
        /**
         * Corresponding to the fourth string index
         */
        STRING_INDEX_4,
        /**
         * Corresponding to the fifth string index
         */
        STRING_INDEX_5;
    }

    private final Map<ProcessInstanceField, Serializable> fields;

    /**
     * Default Constructor with no field to update.
     */
    public ProcessInstanceUpdater() {
        fields = new HashMap<ProcessInstanceField, Serializable>(3);
    }

    /**
     * Set the new value for the first string index.
     * 
     * @param stringIndex
     *        The new value for the first string index.
     */
    public void setStringIndex1(final String stringIndex) {
        fields.put(ProcessInstanceField.STRING_INDEX_1, stringIndex);
    }

    /**
     * Set the new value for the second string index.
     * 
     * @param stringIndex
     *        The new value for the second string index.
     */
    public void setStringIndex2(final String stringIndex) {
        fields.put(ProcessInstanceField.STRING_INDEX_2, stringIndex);
    }

    /**
     * Set the new value for the third string index.
     * 
     * @param stringIndex
     *        The new value for the third string index.
     */
    public void setStringIndex3(final String stringIndex) {
        fields.put(ProcessInstanceField.STRING_INDEX_3, stringIndex);
    }

    /**
     * Set the new value for the fourth string index.
     * 
     * @param stringIndex
     *        The new value for the fourth string index.
     */
    public void setStringIndex4(final String stringIndex) {
        fields.put(ProcessInstanceField.STRING_INDEX_4, stringIndex);
    }

    /**
     * Set the new value for the fifth string index.
     * 
     * @param stringIndex
     *        The new value for the fifth string index.
     */
    public void setStringIndex5(final String stringIndex) {
        fields.put(ProcessInstanceField.STRING_INDEX_5, stringIndex);
    }

    /**
     * Get the map of the fields to update and their new value.
     * 
     * @return The map of the fields to update and their new value.
     */
    public Map<ProcessInstanceField, Serializable> getFields() {
        return fields;
    }

}
