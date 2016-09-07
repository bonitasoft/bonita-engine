/*
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
 */
package org.bonitasoft.platform.setup;

import static org.bonitasoft.platform.setup.PlatformSetup.BONITA_SETUP_FOLDER;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.bonitasoft.platform.exception.PlatformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class configures a supported Bundle with configuration values given in file database.properties.
 * For Tomcat bundle:
 * <ul>
 * <li>sets db vendor + bdm db vendor in file setenv.sh / .bat</li>
 * <li>sets all database configuration in file conf/bitronix-resources.properties</li>
 * <li>sets all database configuration in file conf/Catalina/localhost/bonita.xml</li>
 * <li>copies the required drivers from setup/lib to lib/bonita</li>
 * </ul>
 *
 * @author Emmanuel Duchastenier
 */
class BundleConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(BundleConfigurator.class);

    private static final String POSTGRES = "postgres";
    private static final String ORACLE = "oracle";

    private Path rootPath;

    private DatabaseConfiguration standardConfiguration;
    private DatabaseConfiguration bdmConfiguration;

    private void loadProperties() throws PlatformException {
        final Properties properties = new Properties();
        try {
            properties.load(this.getClass().getResourceAsStream("/database.properties"));
            properties.load(this.getClass().getResourceAsStream("/internal.properties"));
        } catch (IOException e) {
            throw new PlatformException("Error reading configuration file database.properties." +
                    " Please make sure the file is present at the root of the Platform Setup Tool folder, and that is has not been moved of deleted", e);
        }

        standardConfiguration = new DatabaseConfiguration("", properties);
        bdmConfiguration = new DatabaseConfiguration("bdm.", properties);

        final String setupFolder = System.getProperty(BONITA_SETUP_FOLDER);
        if (setupFolder != null) {
            rootPath = Paths.get(setupFolder).getParent();
        } else {
            rootPath = Paths.get("..");
        }
    }

    private boolean fileExists(Path filePath) {
        final boolean exists = Files.exists(filePath);
        if (!exists) {
            LOGGER.info("File " + filePath.toString() + " does not exist.");
        }
        return exists;
    }

    private boolean isTomcatEnvironment() throws PlatformException {
        return fileExists(getPath("bin/catalina.sh")) || fileExists(getPath("bin/catalina.bat"));
    }

    void configureApplicationServer() throws PlatformException {
        loadProperties();
        if (!isTomcatEnvironment()) {
            LOGGER.info("No Application Server detected. You may need to manually configure the access to the database." +
                    " Supported App Servers are: Tomcat 7"); //, Wildfly 9");
            return;
        }
        try {
            final Path dbFile = Paths.get(this.getClass().getResource("/database.properties").toURI());
            LOGGER.info("Tomcat environment detected. Auto-configuring using file " + dbFile.toRealPath().toString() + "...");
            configureTomcat();
            LOGGER.info("Tomcat auto-configuration complete.");
        } catch (URISyntaxException | IOException e) {
            throw new PlatformException("Configuration file 'database.properties' not found");
        }
    }

    void configureTomcat() throws PlatformException {
        final String dbVendor = standardConfiguration.getDbVendor();
        final String bdmDbVendor = bdmConfiguration.getDbVendor();
        final Path setEnvUnixFile = getPath("bin/setenv.sh", true);
        final Path setEnvWindowsFile = getPath("bin/setenv.bat", true);
        final Path bonitaXmlFile = getPath("conf/Catalina/localhost/bonita.xml", true);
        final Path bitronixFile = getPath("conf/bitronix-resources.properties", true);
        final File bonitaDbDriverFile = getDriverFile(dbVendor);
        final File bdmDriverFile = getDriverFile(bdmDbVendor);

        try {
            makeBackupOfFile(setEnvUnixFile, true);
            makeBackupOfFile(setEnvWindowsFile, true);
            makeBackupOfFile(bonitaXmlFile, false);
            copyTemplateToFile("/bonita.template.xml", bonitaXmlFile);
            makeBackupOfFile(bitronixFile, false);
            copyTemplateToFile("/bitronix-resources.template.properties", bitronixFile);

            ////////////////////////////
            // STANDARD BONITA DATABASE:
            ////////////////////////////

            // 1. update setenv(.sh|.bat):
            updateSetEnvFile(setEnvUnixFile, setEnvWindowsFile, dbVendor, "sysprop.bonita.db.vendor");

            //2. update bonita.xml:
            updateBonitaXmlFile(bonitaXmlFile, standardConfiguration, "ds1");

            // 3. update bitronix-resources.properties
            updateBitronixFile(bitronixFile, standardConfiguration, "ds1");

            //4. copy the JDBC drivers:
            final Path srcDriverFile = bonitaDbDriverFile.toPath();
            final Path targetBonitaDbDriverFile = getPath("lib/bonita").resolve(srcDriverFile.getFileName());
            copyDatabaseDriversIfNecessary(srcDriverFile, targetBonitaDbDriverFile, dbVendor);

            ////////////////
            // BDM DATABASE:
            ////////////////

            // 1. update setenv(.sh|.bat):
            updateSetEnvFile(setEnvUnixFile, setEnvWindowsFile, bdmDbVendor, "sysprop.bonita.bdm.db.vendor");

            //2. update bonita.xml:
            updateBonitaXmlFile(bonitaXmlFile, bdmConfiguration, "ds2");

            // 3. update bitronix-resources.properties
            updateBitronixFile(bitronixFile, bdmConfiguration, "ds2");

            //4. copy the JDBC drivers:
            final Path srcBdmDriverFile = bdmDriverFile.toPath();
            final Path targetBdmDriverFile = getPath("lib/bonita").resolve(srcBdmDriverFile.getFileName());
            copyDatabaseDriversIfNecessary(srcBdmDriverFile, targetBdmDriverFile, bdmDbVendor);

        } catch (PlatformException e) {
            restorePreviousConfiguration(setEnvUnixFile, setEnvWindowsFile, bonitaXmlFile, bitronixFile);
            throw e;
        }
    }

    private void updateBitronixFile(Path bitronixFile, DatabaseConfiguration configuration, final String bitronixDatasourceAlias) throws PlatformException {
        Map<String, String> replacements = new HashMap<>(7);
        replacements.put("@@" + bitronixDatasourceAlias + "_driver_class_name@@", configuration.getXaDriverClassName());
        replacements.put("@@" + bitronixDatasourceAlias + "_database_connection_user@@", configuration.getDatabaseUser());
        replacements.put("@@" + bitronixDatasourceAlias + "_database_connection_password@@", configuration.getDatabasePassword());
        replacements.put("@@" + bitronixDatasourceAlias + "_database_test_query@@", configuration.getTestQuery());

        // Let's uncomment and replace dbvendor-specific values:
        if (POSTGRES.equals(configuration.getDbVendor())) {
            replacements.putAll(uncommentLineAndReplace("@@" + bitronixDatasourceAlias + "_postgres_server_name@@", configuration.getServerName()));
            replacements.putAll(uncommentLineAndReplace("@@" + bitronixDatasourceAlias + "_postgres_port_number@@", configuration.getServerPort()));
            replacements.putAll(uncommentLineAndReplace("@@" + bitronixDatasourceAlias + "_postgres_database_name@@", configuration.getDatabaseName()));
        } else {
            replacements.putAll(uncommentLineAndReplace("@@" + bitronixDatasourceAlias + "_database_connection_url@@", configuration.getUrl()));
        }

        replaceContentInFile(bitronixFile, replacements);

        LOGGER.info("Configuring file 'conf/bitronix-resources.properties' with the DB connection values you provided in file 'database.properties'");
    }

    private void updateBonitaXmlFile(Path bonitaXmlFile, DatabaseConfiguration configuration, final String bitronixDatasourceAlias) throws PlatformException {
        Map<String, String> replacements = new HashMap<>(5);
        replacements.put("@@" + bitronixDatasourceAlias + ".database_connection_user@@", configuration.getDatabaseUser());
        replacements.put("@@" + bitronixDatasourceAlias + ".database_connection_password@@", configuration.getDatabasePassword());
        replacements.put("@@" + bitronixDatasourceAlias + ".driver_class_name@@", configuration.getNonXaDriverClassName());
        replacements.put("@@" + bitronixDatasourceAlias + ".database_connection_url@@", configuration.getUrl());
        replacements.put("@@" + bitronixDatasourceAlias + ".database_test_query@@", configuration.getTestQuery());
        replaceContentInFile(bonitaXmlFile, replacements);

        LOGGER.info("Configuring file 'conf/Catalina/localhost/bonita.xml' with the DB connection values you provided in file 'database.properties'");
    }

    private void updateSetEnvFile(Path setEnvUnixFile, Path setEnvWindowsFile, String dbVendor, final String systemPropertyName) throws PlatformException {
        final Map<String, String> replacementMap = Collections.singletonMap("-D" + systemPropertyName + "=.*\"",
                "-D" + systemPropertyName + "=" + dbVendor + "\"");

        replaceContentInFile(setEnvUnixFile, replacementMap);
        replaceContentInFile(setEnvWindowsFile, replacementMap);
    }

    private void copyTemplateToFile(String templateFile, Path destinationFilePath) throws PlatformException {
        try (InputStream templateStream = this.getClass().getResourceAsStream(templateFile)) {
            FileUtils.copyToFile(templateStream, destinationFilePath.toFile());
        } catch (IOException e) {
            throw new PlatformException("Fail to create file " + destinationFilePath.getFileName() + " from template " + templateFile + ": "
                    + e.getMessage(), e);
        }
    }

    boolean copyDatabaseDriversIfNecessary(Path srcDriverFile, Path targetDriverFile, String dbVendor) throws PlatformException {
        if (Files.exists(targetDriverFile)) {
            LOGGER.info("Your " + dbVendor + " driver file " + targetDriverFile.toAbsolutePath() + " already exists. Skipping the copy.");
            return false;
        }
        copyDriverFile(srcDriverFile, targetDriverFile, dbVendor);
        return true;
    }

    void copyDriverFile(Path srcDriverFile, Path targetDriverFile, String dbVendor) throws PlatformException {
        try {
            Files.copy(srcDriverFile, targetDriverFile);
            LOGGER.info("Copying your " + dbVendor + " driver file " + srcDriverFile.getFileName() + " to tomcat lib folder 'lib/bonita'");
        } catch (IOException e) {
            throw new PlatformException(
                    "Fail to copy driver file lib/" + srcDriverFile.getFileName() + " to " + targetDriverFile.toAbsolutePath() + ": " + e.getMessage(), e);
        }
    }

    private void replaceContentInFile(Path setEnvUnixFile, Map<String, String> replacementMap) throws PlatformException {
        try {
            String content = readContentFromFile(setEnvUnixFile);
            for (Map.Entry<String, String> entry : replacementMap.entrySet()) {
                content = replace(content, entry.getKey(), entry.getValue());
            }
            writeContentToFile(setEnvUnixFile, content);
        } catch (IOException e) {
            throw new PlatformException("Fail to replace content in file " + setEnvUnixFile + ": " + e.getMessage(), e);
        }
    }

    private void writeContentToFile(Path bonitaXmlFile, String content) throws IOException {
        Files.write(bonitaXmlFile, content.getBytes(StandardCharsets.UTF_8));
    }

    private String readContentFromFile(Path bonitaXmlFile) throws IOException {
        return new String(Files.readAllBytes(bonitaXmlFile), StandardCharsets.UTF_8);
    }

    void restorePreviousConfiguration(Path setEnvUnixFile, Path setEnvWindowsFile, Path bonitaXmlFile, Path bitronixFile) throws PlatformException {
        LOGGER.warn("Problem encountered, restoring previous configuration");
        // undo file copies:
        restoreOriginalFile(bonitaXmlFile);
        restoreOriginalFile(bitronixFile);
        restoreOriginalFile(setEnvUnixFile);
        restoreOriginalFile(setEnvWindowsFile);
    }

    private void restoreOriginalFile(Path bonitaXmlFile) throws PlatformException {
        try {
            if (Files.exists(bonitaXmlFile)) {
                Files.delete(bonitaXmlFile);
            }
            final Path backupFile = bonitaXmlFile.resolveSibling(bonitaXmlFile.getFileName() + ".original");
            if (Files.exists(backupFile)) {
                Files.move(backupFile, bonitaXmlFile);
            }
        } catch (IOException e) {
            throw new PlatformException("Fail to restore original file for " + bonitaXmlFile + ": " + e.getMessage(), e);
        }
    }

    private void makeBackupOfFile(Path originalFile, boolean keepOriginal) throws PlatformException {
        final Path originalFileName = originalFile.getFileName();
        LOGGER.info("Creating a backup of configuration file '" + originalFile + "' => '" + originalFile.getFileName() + ".original'");
        final Path backup = originalFile.resolveSibling(originalFileName + ".original");
        try {
            Files.deleteIfExists(backup);
            if (keepOriginal) {
                Files.copy(originalFile, backup);
            } else {
                Files.move(originalFile, backup);
            }
        } catch (IOException e) {
            throw new PlatformException("Fail to make backup file for " + originalFileName + ": " + e.getMessage(), e);
        }
    }

    private File getDriverFile(String dbVendor) throws PlatformException {
        final Path driverFolder = getPath("setup/lib");
        if (!Files.exists(driverFolder)) {
            throw new PlatformException("Drivers folder not found: " + driverFolder.toString()
                    + ". Make sure it exists and put a jar or zip file containing drivers there.");
        }
        final Collection<File> driversFiles = FileUtils.listFiles(driverFolder.toFile(), getDriverFilter(dbVendor), null);
        if (driversFiles.size() == 0) {
            throw new PlatformException("No " + dbVendor + " drivers found in folder " + driverFolder.toString()
                    + ". Make sure to put a jar or zip file containing drivers there.");
        } else if (driversFiles.size() == 1) {
            return (File) driversFiles.toArray()[0];
        } else {
            throw new PlatformException(
                    "Found more than 1 file containing " + dbVendor + " drivers  in folder " + driverFolder.toString()
                            + ". Make sure to put only 1 jar or zip file containing drivers there.");
        }
    }

    RegexFileFilter getDriverFilter(String dbVendor) {
        return new RegexFileFilter(getDriverPattern(dbVendor), IOCase.INSENSITIVE);
    }

    private String getDriverPattern(String dbVendor) {
        if (ORACLE.equals(dbVendor)) {
            return ".*(ojdbc|oracle).*\\.(jar|zip)";
        }
        return ".*" + dbVendor + ".*";
    }

    private Map<String, String> uncommentLineAndReplace(String originalValue, String replacement) {
        return Collections.singletonMap("#[ ]*(.*)=" + originalValue, "$1=" + replacement);
    }

    private String replace(String content, String originalValue, String replacement) {
        return content.replaceAll(originalValue, replacement);
    }

    /**
     * Constructs path relative to rootPath.
     *
     * @param partialPath path relative to rootPath
     * @return the real path, constructed from rootPath, appending the partialPath
     */
    private Path getPath(String partialPath) throws PlatformException {
        return getPath(partialPath, false);
    }

    /**
     * Constructs path relative to rootPath.
     *
     * @param partialPath path relative to rootPath
     * @param failIfNotExist should we fail if file does not exist?
     * @return the real path, constructed from rootPath, appending the partialPath
     */
    private Path getPath(String partialPath, boolean failIfNotExist) throws PlatformException {
        final String[] paths = partialPath.split("/");
        Path build = rootPath;
        for (String path : paths) {
            build = build.resolve(path);
        }
        if (failIfNotExist && Files.notExists(build)) {
            throw new PlatformException("File " + build.getFileName() + " is mandatory but is not found");
        }
        return build;
    }

}
