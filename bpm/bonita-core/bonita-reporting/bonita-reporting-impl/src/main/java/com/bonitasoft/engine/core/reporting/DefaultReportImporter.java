/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.core.reporting;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

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
                updateIfDifferent(reportName, reportContent, reportByName, description, screenShot);
            }else{
                importDefaultReport(reportName, reportContent, description, screenShot);
            }
        } catch (final Exception e) {
            logger.log(getClass(), TechnicalLogSeverity.WARNING, "Report "+reportName+" can't be imported");
        }
    }

    private void updateIfDifferent(String reportName, byte[] reportContent, SReport existingReport, String description, byte[] screenShot) throws SBonitaReadException, SReportNotFoundException, SObjectModificationException {
        final byte[] existingReportContent = reportingService.getReportContent(existingReport.getId());
        if (reportContent.length != existingReportContent.length){
            logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Report "+reportName+" exists but the content is not up to date, updating it.");
            EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
            entityUpdateDescriptor.addField(SReportFields.NAME,reportName);
            entityUpdateDescriptor.addField(SReportFields.DESCRIPTION,description);
            entityUpdateDescriptor.addField(SReportFields.SCREENSHOT,screenShot);
            entityUpdateDescriptor.addField(SReportFields.CONTENT,reportContent);
            reportingService.update(existingReport, entityUpdateDescriptor);
        }else{
            logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Report "+reportName+" exists and is up to date, do nothing");
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
        logger.log(getClass(), TechnicalLogSeverity.DEBUG, "Report "+reportName+" was not imported, importing it.");
        final SReportBuilder reportBuilder = BuilderFactory.get(SReportBuilderFactory.class).createNewInstance(reportName, /* system user */-1, true,
                description, screenShot);
        reportingService.addReport(reportBuilder.done(), reportContent);
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
