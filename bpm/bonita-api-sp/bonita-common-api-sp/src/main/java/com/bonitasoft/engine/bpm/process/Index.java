/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bpm.process;

/**
 * Represent a search index that contains arbitrary String information on a process instance, on which search can be performed.
 * It is tipically made to allow search on data that are external to Bonita BPM platform, but related to processes, that the process stores for search purposes.
 *
 * @author Matthieu Chaffotte
 */
public enum Index {

    /**
     * First search index
     */
    FIRST,

    /**
     * Second search index
     */
    SECOND,

    /**
     * Third search index
     */
    THIRD,

    /**
     * Fourth search index
     */
    FOURTH,

    /**
     * Fifth search index
     */
    FIFTH;
}
