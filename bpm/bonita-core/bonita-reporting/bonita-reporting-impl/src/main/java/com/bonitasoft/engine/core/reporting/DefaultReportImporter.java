/*
 * *****************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 * ******************************************************************************
 */

package com.bonitasoft.engine.core.reporting;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Baptiste Mesta
 */
public class DefaultReportImporter {


    private ReportingServiceImpl reportingService;
    private TechnicalLoggerService logger;

    public DefaultReportImporter(ReportingServiceImpl reportingService, TechnicalLoggerService logger) {
        this.reportingService = reportingService;
        this.logger = logger;
    }

    public void invoke(String reportName) throws SReportCreationException, SBonitaReadException, SReportNotFoundException,
            SReportDeletionException {
        String zipName = reportName + "-content.zip";
        try {
            // check if the provided pages are here or not up to date and import them from class path if needed
            final InputStream inputStream = getReportFromClassPath(zipName);
            if (inputStream == null)
                return;
            byte[] reportContent = IOUtil.getAllContentFrom(inputStream);
            String description = getDescriptionQuietly(reportContent, reportName);
            byte[] screenShot = getScreenShotQuietly(reportContent, reportName);
            SReport reportByName = reportingService.getReportByName(reportName);
            if (reportByName != null) {
                if (deleteIfNotTheSame(reportContent, reportByName))
                    return;//the content is the same so we do not replace it
            }
            importDefaultReport(reportName, reportContent, description, screenShot);
        } catch (final IOException e) {
            logger.log(getClass(), TechnicalLogSeverity.WARNING, "Provided page " + zipName + "can't be imported");
        }
    }

    InputStream getReportFromClassPath(String zipName) {
        final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(zipName);
        if (inputStream == null) {
            // no provided page
            logger.log(getClass(), TechnicalLogSeverity.DEBUG, "No provided-" + zipName + " found in the class path, nothing will be imported");
            return null;
        }
        return inputStream;
    }

    private void importDefaultReport(String reportName, byte[] reportContent, String description, byte[] screenShot) throws SReportCreationException {
        logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Provided page was not imported, importing it.");
        final SReportBuilder reportBuilder = BuilderFactory.get(SReportBuilderFactory.class).createNewInstance(reportName, /* system user */-1, true,
                description, screenShot);
        reportingService.addReport(reportBuilder.done(), reportContent);
    }

    private boolean deleteIfNotTheSame(byte[] reportContent, SReport reportByName) throws SBonitaReadException, SReportNotFoundException,
            SReportDeletionException {
        final byte[] pageContent = reportingService.getReportContent(reportByName.getId());
        if (reportContent.length != pageContent.length) {
            logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Provided page exists but the content is not up to date, updating it.");
            // think of a better way to check the content are the same or not, it will almost always be the same so....
            reportingService.deleteReport(reportByName.getId());
        } else {
            logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Provided page exists and is up to date, do nothing");
            return true;
        }
        return false;
    }

    private String getDescriptionQuietly(byte[] zipContent, String name) throws IOException {
        Properties properties;
        byte[] zipEntryContent = IOUtil.getZipEntryContent(name + ".properties", zipContent);
        properties = new Properties();
        properties.load(new ByteArrayInputStream(zipEntryContent));
        return (String) properties.get(name + ".description");
    }

    private byte[] getScreenShotQuietly(byte[] zipContent, String name) throws IOException {
        return IOUtil.getZipEntryContent(name + "-screenshot.png", zipContent);
    }

}
