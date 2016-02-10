/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.process;

/**
 * Use to update the string indexes.
 *
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @version 6.3.5
 * @since 6.0.0
 * @see org.bonitasoft.engine.bpm.process.ProcessInstance#getStringIndex1()
 * @see org.bonitasoft.engine.bpm.process.ProcessInstance#getStringIndex2()
 * @see org.bonitasoft.engine.bpm.process.ProcessInstance#getStringIndex3()
 * @see org.bonitasoft.engine.bpm.process.ProcessInstance#getStringIndex4()
 * @see org.bonitasoft.engine.bpm.process.ProcessInstance#getStringIndex5()
 * @see com.bonitasoft.engine.api.ProcessAPI#updateProcessInstanceIndex(long, Index, String)
 */
public enum Index {

    /**
     * Corresponding to the first string index
     */
    FIRST,
    /**
     * Corresponding to the second string index
     */
    SECOND,
    /**
     * Corresponding to the third string index
     */
    THIRD,
    /**
     * Corresponding to the fourth string index
     */
    FOURTH,
    /**
     * Corresponding to the fifth string index
     */
    FIFTH;
}
