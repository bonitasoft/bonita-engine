/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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
        checker.checkLicenseAndFeature(feature);
        super.deploy(reportPath, deployer);
    }
}
