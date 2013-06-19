/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.bonitasoft.engine.api.NodeAPI;
import com.bonitasoft.engine.platform.LicenseInfo;
import com.bonitasoft.engine.platform.impl.LicenseInfoImpl;
import com.bonitasoft.manager.Manager;

/**
 * @author Baptiste Mesta
 */
public class NodeAPIImpl implements NodeAPI {

    @Override
    public LicenseInfo getLicenseInfo() {
        final Map<String, String> info = new Manager().getInfo();
        final String edition = info.get("subscriptionType");
        final String licensee = info.get("customerName");
        final List<String> features = new Manager().activeFeatures();
        final Date expirationDate = new Date(Long.valueOf(info.get("expirationDate")));
        final String numberOfCPUCoresAsString = info.get("numberOfCPUCores");
        int numberOfCPUCores = -1;
        if (numberOfCPUCoresAsString != null) {
            numberOfCPUCores = Integer.valueOf(numberOfCPUCoresAsString);
        }
        return new LicenseInfoImpl(licensee, expirationDate, edition, features, numberOfCPUCores);
    }
}
