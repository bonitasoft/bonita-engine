/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import java.util.List;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.reporting.Report;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public interface ReportingAPI extends org.bonitasoft.engine.api.ReportingAPI {

    /**
     * 
     * @param name
     * @param description
     * @param content
     * @return
     * @throws AlreadyExistsException
     * @throws CreationException
     */
    Report createReport(String name, final String description, byte[] content) throws AlreadyExistsException, CreationException;

    /**
     * 
     * @param id
     *            the report identifier to delete
     * @throws DeletionException
     */
    void deleteReport(long id) throws DeletionException;

    /**
     * 
     * @param reportIds
     *            a list of report identifiers to delete
     * @throws DeletionException
     */
    void deleteReports(List<Long> reportIds) throws DeletionException;

}
