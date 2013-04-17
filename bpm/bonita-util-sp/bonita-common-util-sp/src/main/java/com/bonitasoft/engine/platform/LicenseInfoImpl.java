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
package com.bonitasoft.engine.platform;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
