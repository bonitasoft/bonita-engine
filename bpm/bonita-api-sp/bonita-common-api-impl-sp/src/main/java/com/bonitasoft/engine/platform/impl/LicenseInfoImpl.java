/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.platform.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.bonitasoft.engine.platform.LicenseInfo;

/**
 * @author Matthieu Chaffotte
 */
public class LicenseInfoImpl implements LicenseInfo {

    private static final long serialVersionUID = -6223352080153977840L;

    private final String licensee;

    private final Date expirationDate;

    private final String edition;

    private final List<String> features;

    private final int numberOfCPUCores;

    public LicenseInfoImpl(final String licensee, final Date expirationDate, final String edition, final List<String> features, final int numberOfCPUCores) {
        super();
        this.licensee = licensee;
        this.expirationDate = expirationDate;
        this.edition = edition;
        this.features = new ArrayList<String>(features);
        this.numberOfCPUCores = numberOfCPUCores;
    }

    @Override
    public String getLicensee() {
        return licensee;
    }

    @Override
    public Date getExpirationDate() {
        return expirationDate;
    }

    @Override
    public String getEdition() {
        return edition;
    }

    @Override
    public List<String> getFeatures() {
        return features;
    }

    @Override
    public int getNumberOfCPUCores() {
        return numberOfCPUCores;
    }

}
