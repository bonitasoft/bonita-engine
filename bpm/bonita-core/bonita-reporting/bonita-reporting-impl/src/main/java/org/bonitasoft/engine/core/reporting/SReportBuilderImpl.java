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

/**
 * @author Matthieu Chaffotte
 */
public class SReportBuilderImpl implements SReportBuilder {

    private SReportImpl report;

    @Override
    public SReportBuilder createNewInstance(final String name, final long installationDate, final long installedBy, final boolean provided) {
        report = new SReportImpl(name, installationDate, installedBy, provided);
        return this;
    }

    @Override
    public SReportBuilder description(final String description) {
        report.setDescription(description);
        return this;
    }

    @Override
    public String getNameKey() {
        return "name";
    }

    @Override
    public String getInstallationDateKey() {
        return "installationDate";
    }

    @Override
    public String getInstalledByKey() {
        return "installedBy";
    }

    @Override
    public SReport done() {
        return report;
    }

    @Override
    public String getIdKey() {
        return "id";
    }

    @Override
    public String getProvidedKey() {
        return "provided";
    }

}
