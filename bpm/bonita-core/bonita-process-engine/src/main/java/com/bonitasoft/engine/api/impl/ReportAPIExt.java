/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
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
