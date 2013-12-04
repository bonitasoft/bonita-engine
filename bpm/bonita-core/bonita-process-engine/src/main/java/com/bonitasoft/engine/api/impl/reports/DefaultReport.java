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

import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.io.PropertiesManager;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Vincent Elcrin
 * Date: 02/12/13
 * Time: 16:41
 */
public class DefaultReport {

    private String name;

    protected DefaultReport(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void deploy(final String reportPath, final ReportDeployer deployer) throws Exception {
        ZipReader zip = new ZipReader(reportPath, name + "-content.zip");
        zip.read(new Reader() {
            @Override
            public void read(File zip, File unzipped) throws Exception {
                deployer.deploy(name,
                        getDescriptionQuietly(unzipped),
                        getScreenShotQuietly(reportPath),
                        IOUtil.getAllContentFrom(zip));
            }
        });
    }

    private String getDescriptionQuietly(File unzipped) {
        Properties properties;
        try {
            properties = PropertiesManager.getProperties(new File(unzipped, name + ".properties"));
            return (String) properties.get(name + ".description");
        } catch (IOException e) {
            // stay quiet - a log would be nice
        }
        return null;
    }

    private byte[] getScreenShotQuietly(String reportPath) {
        try {
            return IOUtil.getAllContentFrom(new File(reportPath, name + "-screenshot").getAbsoluteFile());
        } catch (IOException e) {
            // stay quiet - a log would be nice
        }
        return null;
    }
}
