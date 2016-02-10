/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.process.impl;

/**
 * Search descriptors are used to filter / sort results of a generic search. <br>
 * ProcessInstanceSearchDescriptor defines the fields that can be used as filters or sort fields on <code>List&lt;ProcessInstance&gt;</code> returning
 * methods.
 *
 * @author Celine Souchet
 * @version 6.3.5
 * @since 6.0.0
 * @see org.bonitasoft.engine.bpm.process.ProcessInstance
 * @see com.bonitasoft.engine.api.ProcessRuntimeAPI
 */
public class ProcessInstanceSearchDescriptor extends org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor {

    /**
     * The field corresponding to the first string index of the process instance.
     */
    public static final String STRING_INDEX_1 = "index1";

    /**
     * The field corresponding to the second string index of the process instance.
     */
    public static final String STRING_INDEX_2 = "index2";

    /**
     * The field corresponding to the third string index of the process instance.
     */
    public static final String STRING_INDEX_3 = "index3";

    /**
     * The field corresponding to the fourth string index of the process instance.
     */
    public static final String STRING_INDEX_4 = "index4";

    /**
     * The field corresponding to the fifth string index of the process instance.
     */
    public static final String STRING_INDEX_5 = "index5";

}
