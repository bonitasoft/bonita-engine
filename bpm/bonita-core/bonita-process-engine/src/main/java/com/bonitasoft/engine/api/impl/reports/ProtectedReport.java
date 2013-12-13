/*
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.bonitasoft.engine.api.impl.reports;

import com.bonitasoft.engine.service.impl.LicenseChecker;

/**
 * Created by Vincent Elcrin
 * Date: 02/12/13
 * Time: 17:55
 */
public class ProtectedReport extends DefaultReport {

    private String feature;
    private LicenseChecker checker;

    protected ProtectedReport(String name, String feature, LicenseChecker checker) {
        super(name);
        this.feature = feature;
        this.checker = checker;
    }

    @Override
    public void deploy(String reportPath, ReportDeployer deployer) throws Exception {
        checker.checkLicenceAndFeature(feature);
        super.deploy(reportPath, deployer);
    }
}
