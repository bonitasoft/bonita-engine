/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.util.List;

import org.bonitasoft.engine.api.impl.ReportAPIImpl;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.reporting.Report;
import org.bonitasoft.engine.reporting.ReportNotFoundException;

import com.bonitasoft.engine.api.ReportAPI;
import com.bonitasoft.engine.reporting.ReportAlreadyExistsException;
import com.bonitasoft.engine.reporting.ReportCreationException;
import com.bonitasoft.engine.reporting.ReportDeletionException;

/**
 * @author Matthieu Chaffotte
 */
public class ReportAPIExt extends ReportAPIImpl implements ReportAPI {

    @Override
    public Report addReport(final String name, final byte[] content) throws InvalidSessionException, ReportAlreadyExistsException, ReportCreationException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void deleteReport(final long reportId) throws InvalidSessionException, ReportNotFoundException, ReportDeletionException {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteReports(final List<Long> reportIds) throws InvalidSessionException, ReportNotFoundException, ReportDeletionException {
        // TODO Auto-generated method stub

    }

}
