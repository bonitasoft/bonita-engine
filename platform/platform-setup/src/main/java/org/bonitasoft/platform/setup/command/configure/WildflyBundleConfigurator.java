/**
 * Copyright (C) 2016 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

package org.bonitasoft.platform.setup.command.configure;

import static org.bonitasoft.platform.setup.command.configure.DatabaseConfiguration.H2_DB_VENDOR;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import org.bonitasoft.platform.exception.PlatformException;

/**
 * @author Emmanuel Duchastenier
 */
class WildflyBundleConfigurator extends BundleConfigurator {

    private static final String WILDFLY_BACKUP_FOLDER = "wildfly-backups";
    private static final String WILDFLY_TEMPLATES_FOLDER = "wildfly-templates";

    private static final Map<String, String> wildflyModules = new HashMap<>(5);

    static {
        WildflyBundleConfigurator.wildflyModules.put(H2, "com/h2database/h2");
        WildflyBundleConfigurator.wildflyModules.put(POSTGRES, "org/postgresql");
        WildflyBundleConfigurator.wildflyModules.put(ORACLE, "com/oracle");
        WildflyBundleConfigurator.wildflyModules.put(SQLSERVER, "com/sqlserver");
        WildflyBundleConfigurator.wildflyModules.put(MYSQL, "com/mysql");
    }

    WildflyBundleConfigurator(Path rootPath) throws PlatformException {
        super(rootPath);
    }

    @Override
    protected String getBundleName() {
        return "Wildfly";
    }

    @Override
    public void configureApplicationServer() throws PlatformException {
        loadProperties();

        final String dbVendor = standardConfiguration.getDbVendor();
        final String bdmDbVendor = bdmConfiguration.getDbVendor();
        final Path standaloneXmlFile = getPathUnderAppServer("standalone/configuration/standalone.xml", true);
        final File bonitaDbDriverFile = getDriverFile(dbVendor);
        final File bdmDriverFile = getDriverFile(bdmDbVendor);

        try {
            createBackupFolderIfNecessary("setup/" + WILDFLY_BACKUP_FOLDER);

            //1. update standalone/configuration/standalone.xml
            String newContent = readContentFromFile(getPath("setup").resolve(WILDFLY_TEMPLATES_FOLDER).resolve("standalone.xml"));
            newContent = updateStandaloneXmlFile(newContent, standardConfiguration, "");
            newContent = updateStandaloneXmlFile(newContent, bdmConfiguration, "BDM_");
            backupAndReplaceContentIfNecessary(standaloneXmlFile, newContent,
                    "Configuring file 'standalone/configuration/standalone.xml' with your DB values for Bonita internal database on '" + dbVendor
                            + "' and for Business Data database on '" + bdmDbVendor + "'");

            //2. copy the JDBC drivers:
            if (!dbVendor.equals(H2_DB_VENDOR)) {
                final Path srcDriverFile = bonitaDbDriverFile.toPath();
                final Path moduleFolder = getOptionalPathUnderAppServer("modules").resolve(wildflyModules.get(dbVendor)).resolve("main");
                final Path targetBonitaDbDriverFile = moduleFolder.resolve(srcDriverFile.getFileName());
                final boolean driversCopied = copyDatabaseDriversIfNecessary(srcDriverFile, targetBonitaDbDriverFile, dbVendor);
                //3. copy the module files:
                copyDriverModuleFileIfNecessary(targetBonitaDbDriverFile, driversCopied, standardConfiguration, moduleFolder);
            }

            if (!bdmDbVendor.equals(H2_DB_VENDOR)) {
                final Path srcBdmDriverFile = bdmDriverFile.toPath();
                final Path bdmModuleFolder = getOptionalPathUnderAppServer("modules").resolve(wildflyModules.get(bdmDbVendor)).resolve("main");
                final Path targetBdmDriverFile = bdmModuleFolder.resolve(srcBdmDriverFile.getFileName());
                final boolean bdmDriversCopied = copyDatabaseDriversIfNecessary(srcBdmDriverFile, targetBdmDriverFile, bdmDbVendor);
                //3. copy the module files:
                copyDriverModuleFileIfNecessary(targetBdmDriverFile, bdmDriversCopied, bdmConfiguration, bdmModuleFolder);
            }
            LOGGER.info("Wildfly auto-configuration complete.");
        } catch (PlatformException e) {
            restorePreviousConfiguration(standaloneXmlFile);
            throw e;
        }
    }

    void restorePreviousConfiguration(Path standaloneXmlFile) throws PlatformException {
        LOGGER.warn("Problem encountered, restoring previous configuration");
        // undo file copies:
        restoreOriginalFile(standaloneXmlFile);
    }

    private void copyDriverModuleFileIfNecessary(Path driverFile, boolean driversCopied, DatabaseConfiguration configuration, Path moduleXmlFolder)
            throws PlatformException {
        if (driversCopied) {
            String moduleFileContent = readContentFromFile(getPath("setup").resolve(WILDFLY_TEMPLATES_FOLDER).resolve("module.xml"));
            moduleFileContent = updateModuleFile(moduleFileContent, configuration, driverFile);
            writeContentToFile(moduleXmlFolder.resolve("module.xml"), moduleFileContent);
            LOGGER.info("Creating module.xml file in folder '" + getRelativePath(moduleXmlFolder) + "' for " + configuration.getDbVendor());
        }
    }

    private String updateModuleFile(String moduleFileContent, DatabaseConfiguration configuration, Path driverFilename) throws PlatformException {
        Map<String, String> replacements = new HashMap<>(2);
        replacements.put("@@MODULE_NAME@@", wildflyModules.get(configuration.getDbVendor()).replaceAll("/", "."));
        replacements.put("@@DRIVERFILE_NAME@@", driverFilename.getFileName().toString());
        return replaceValues(moduleFileContent, replacements);
    }

    private String updateStandaloneXmlFile(String content, DatabaseConfiguration configuration, final String databasePrefix) throws PlatformException {
        Map<String, String> replacements = new HashMap<>(12);

        replacements.put("@@" + databasePrefix + "MODULE_NAME@@", wildflyModules.get(configuration.getDbVendor()).replaceAll("/", "."));
        replacements.put("@@" + databasePrefix + "XA_DRIVER_CLASSNAME@@", configuration.getXaDriverClassName());

        // We can have only once a driver declaration for each DB Vendor:
        if (!standardConfiguration.getDbVendor().equals(bdmConfiguration.getDbVendor())) {
            replacements.put("<!-- BDM_DRIVER_TEMPLATE (.*) BDM_DRIVER_TEMPLATE -->", "<$1>");
        }

        replacements.put("@@" + databasePrefix + "DB_VENDOR@@", configuration.getDbVendor());
        replacements.put("@@" + databasePrefix + "USERNAME@@", Matcher.quoteReplacement(configuration.getDatabaseUser()));
        replacements.put("@@" + databasePrefix + "PASSWORD@@", Matcher.quoteReplacement(configuration.getDatabasePassword()));
        replacements.put("@@" + databasePrefix + "TESTQUERY@@", configuration.getTestQuery());
        replacements.put("<connection-url>@@" + databasePrefix + "DB_URL@@", "<connection-url>" + getDatabaseConnectionUrlForXmlFile(configuration));

        // Let's uncomment and replace dbvendor-specific values:
        if (POSTGRES.equals(configuration.getDbVendor())) {
            replacements.putAll(uncommentXmlLineAndReplace("@@" + databasePrefix + "DB_SERVER_NAME@@", configuration.getServerName()));
            replacements.putAll(uncommentXmlLineAndReplace("@@" + databasePrefix + "DB_SERVER_PORT@@", configuration.getServerPort()));
            replacements.putAll(uncommentXmlLineAndReplace("@@" + databasePrefix + "DB_DATABASE_NAME@@", Matcher.quoteReplacement(configuration.getDatabaseName())));
        } else {
            replacements.putAll(uncommentXmlLineAndReplace("@@" + databasePrefix + "DB_URL@@", getDatabaseConnectionUrlForXmlFile(configuration)));
        }
        if (ORACLE.equals(configuration.getDbVendor())) {
            replacements.putAll(uncommentXmlLineAndReplace("@@" + databasePrefix + "IS_SAME_RM_OVERRIDE@@", "false"));//forced to false
            replacements.putAll(uncommentXmlLineAndReplace("@@" + databasePrefix + "NO_TX_SEPARATE_POOL@@", "true"));//forced to true
        }

        return replaceValues(content, replacements);
    }

    private Map<String, String> uncommentXmlLineAndReplace(String originalValue, String replacement) {
        return Collections.singletonMap("<!--[ ]*(.*)" + originalValue + "(.*)[ ]*-->", "<$1" + replacement + "$2>");
    }

}
