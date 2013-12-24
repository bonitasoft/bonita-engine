/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
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
            return IOUtil.getAllContentFrom(new File(reportPath, name + "-screenshot.png").getAbsoluteFile());
        } catch (IOException e) {
            // stay quiet - a log would be nice
        }
        return null;
    }
}
