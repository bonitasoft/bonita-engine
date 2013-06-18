/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.reporting;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Matthieu Chaffotte
 */
public class SReportBuilderImplTest {

    @Test
    public void getNullReport() {
        final SReportBuilderImpl builder = new SReportBuilderImpl();
        Assert.assertNull(builder.done());
    }

    @Test
    public void getSimpleReport() {
        final SReportBuilderImpl builder = new SReportBuilderImpl();
        final String name = "report";
        final long installationDate = System.currentTimeMillis();
        final int installedBy = 10;
        builder.createNewInstance(name, installationDate, installedBy, false);
        final SReport report = builder.done();
        final SReportImpl expected = new SReportImpl(name, installationDate, installedBy, false);
        Assert.assertEquals(expected, report);
    }

    @Test
    public void getComplexReport() {
        final SReportBuilderImpl builder = new SReportBuilderImpl();
        final String name = "report";
        final long installationDate = System.currentTimeMillis();
        final int installedBy = 10;
        final String description = "description";
        builder.createNewInstance(name, installationDate, installedBy, true).description(description);
        final SReport report = builder.done();
        final SReportImpl expected = new SReportImpl(name, installationDate, installedBy, true);
        expected.setDescription(description);
        Assert.assertEquals(expected, report);
    }

}
