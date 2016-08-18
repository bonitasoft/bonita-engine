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

import static org.bonitasoft.platform.setup.DatabaseConfiguration.H2_DB_VENDOR;
import static org.bonitasoft.platform.setup.PlatformSetup.BONITA_SETUP_FOLDER;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
public class BundleConfigurator {

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
        if (H2_DB_VENDOR.equals(standardConfiguration.getDbVendor())) {
            LOGGER.info("H2 environment detected: no auto-configuration needed.");
            return;
        }
        if (!isTomcatEnvironment()) {
            LOGGER.info("This Application Server needs manual configuration.");
            return;
        }

        final Path dbFile = getPath("setup/database.properties");
        LOGGER.info("Tomcat environment detected. Auto-configuring using file " + dbFile.toAbsolutePath().toString() + "...");
        configureTomcat();
        LOGGER.info("Tomcat auto-configuration complete.");
    }

    void configureTomcat() throws PlatformException {
        final Path setEnvUnixFile = getPath("bin/setenv.sh", true);
        final Path setEnvWindowsFile = getPath("bin/setenv.bat", true);
        final Path bonitaXmlFile = getPath("conf/Catalina/localhost/bonita.xml", true);
        final Path bitronixFile = getPath("conf/bitronix-resources.properties", true);
        final File bonitaDbDriverFile = getDriverFile(standardConfiguration.getDbVendor());
        final File bdmDriverFile = getDriverFile(bdmConfiguration.getDbVendor());
        final Path targetDriverDirectory = getPath("lib/bonita");
        try {
            // 1. update setenv(.sh|.bat):
            updateSetEnvFile(setEnvUnixFile, setEnvWindowsFile);

            //2. update bonita.xml:
            updateBonitaXmlFile(bonitaXmlFile);

            // 3. update bitronix-resources.properties
            updateBitronixFile(bitronixFile);

            //4. copy the JDBC drivers:
            copyDriversToTomcat(bonitaDbDriverFile, bdmDriverFile, targetDriverDirectory);

        } catch (PlatformException e) {
            restorePreviousConfiguration(setEnvUnixFile, setEnvWindowsFile, bonitaXmlFile, bitronixFile, bdmDriverFile, targetDriverDirectory);
            throw e;
        }
    }

    private void copyDriversToTomcat(File bonitaDbDriverFile, File bdmDriverFile, Path targetDriverDirectory) throws PlatformException {
        copyDatabaseDrivers(bonitaDbDriverFile, targetDriverDirectory.toFile(), standardConfiguration.getDbVendor());
        if (!bonitaDbDriverFile.equals(bdmDriverFile)) {
            copyDatabaseDrivers(bdmDriverFile, targetDriverDirectory.toFile(), bdmConfiguration.getDbVendor());
        }
    }

    private void updateBitronixFile(Path bitronixFile) throws PlatformException {
        // Backup the original file:
        makeBackupOfFile(bitronixFile, false);

        copyTemplateToFile("/bitronix-resources.template.properties", bitronixFile);

        Map<String, String> replacements = new HashMap<>(14);
        replacements.put("ds1_driver_class_name", standardConfiguration.getXaDriverClassName());
        replacements.put("ds1_database_connection_user", standardConfiguration.getDatabaseUser());
        replacements.put("ds1_database_connection_password", standardConfiguration.getDatabasePassword());
        replacements.put("ds1_database_test_query", standardConfiguration.getTestQuery());

        // Let's uncomment and replace dbvendor-specific values for DS1:
        if (POSTGRES.equals(standardConfiguration.getDbVendor())) {
            replacements.putAll(uncommentLineAndReplace("ds1_postgres_server_name", standardConfiguration.getServerName()));
            replacements.putAll(uncommentLineAndReplace("ds1_postgres_port_number", standardConfiguration.getServerPort()));
            replacements.putAll(uncommentLineAndReplace("ds1_postgres_database_name", standardConfiguration.getDatabaseName()));
        } else {
            replacements.putAll(uncommentLineAndReplace("ds1_database_connection_url", standardConfiguration.getUrl()));
        }

        replacements.put("ds2_driver_class_name", bdmConfiguration.getXaDriverClassName());
        replacements.put("ds2_database_connection_user", bdmConfiguration.getDatabaseUser());
        replacements.put("ds2_database_connection_password", bdmConfiguration.getDatabasePassword());
        replacements.put("ds2_database_test_query", bdmConfiguration.getTestQuery());

        // Let's uncomment and replace dbvendor-specific values for DS2:
        if (POSTGRES.equals(bdmConfiguration.getDbVendor())) {
            replacements.putAll(uncommentLineAndReplace("ds2_postgres_server_name", bdmConfiguration.getServerName()));
            replacements.putAll(uncommentLineAndReplace("ds2_postgres_port_number", bdmConfiguration.getServerPort()));
            replacements.putAll(uncommentLineAndReplace("ds2_postgres_database_name", bdmConfiguration.getDatabaseName()));
        } else {
            replacements.putAll(uncommentLineAndReplace("ds2_database_connection_url", bdmConfiguration.getUrl()));
        }

        replaceContentInFile(bitronixFile, replacements);

        LOGGER.info("Configuring file 'conf/bitronix-resources.properties' with the DB connection values you provided in file 'database.properties'");
    }

    private void updateBonitaXmlFile(Path bonitaXmlFile) throws PlatformException {
        // Backup the original file:
        makeBackupOfFile(bonitaXmlFile, false);

        copyTemplateToFile("/bonita.template.xml", bonitaXmlFile);

        Map<String, String> replacements = new HashMap<>(10);
        replacements.put("ds1.database_connection_user", standardConfiguration.getDatabaseUser());
        replacements.put("ds1.database_connection_password", standardConfiguration.getDatabasePassword());
        replacements.put("ds1.driver_class_name", standardConfiguration.getNonXaDriverClassName());
        replacements.put("ds1.database_connection_url", standardConfiguration.getUrl());
        replacements.put("ds1.database_test_query", standardConfiguration.getTestQuery());

        replacements.put("ds2.database_connection_user", bdmConfiguration.getDatabaseUser());
        replacements.put("ds2.database_connection_password", bdmConfiguration.getDatabasePassword());
        replacements.put("ds2.driver_class_name", bdmConfiguration.getNonXaDriverClassName());
        replacements.put("ds2.database_connection_url", bdmConfiguration.getUrl());
        replacements.put("ds2.database_test_query", bdmConfiguration.getTestQuery());
        replaceContentInFile(bonitaXmlFile, replacements);

        LOGGER.info("Configuring file 'conf/Catalina/localhost/bonita.xml' with the DB connection values you provided in file 'database.properties'");
    }

    private void updateSetEnvFile(Path setEnvUnixFile, Path setEnvWindowsFile) throws PlatformException {
        Map<String, String> replacementMap = new HashMap<>(2);
        replacementMap.put("-Dsysprop.bonita.db.vendor=.*\"", "-Dsysprop.bonita.db.vendor=" + standardConfiguration.getDbVendor() + "\"");
        replacementMap.put("-Dsysprop.bonita.bdm.db.vendor=.*\"", "-Dsysprop.bonita.bdm.db.vendor=" + bdmConfiguration.getDbVendor() + "\"");

        makeBackupOfFile(setEnvUnixFile, true);
        replaceContentInFile(setEnvUnixFile, replacementMap);

        makeBackupOfFile(setEnvWindowsFile, true);
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

    void copyDatabaseDrivers(File driverFile, File targetDriverDirectory, String dbVendor) throws PlatformException {
        if (driverFile != null) {
            try {
                FileUtils.copyFileToDirectory(driverFile, targetDriverDirectory);
                LOGGER.info("Copying your " + dbVendor + " driver file " + driverFile.getName() + " to tomcat lib folder 'lib/bonita'");
            } catch (IOException e) {
                throw new PlatformException("Fail to copy driver file " + driverFile.getName() + " to target directory "
                        + targetDriverDirectory.getAbsolutePath() + ": " + e.getMessage(), e);
            }
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

    private void restorePreviousConfiguration(Path setEnvUnixFile, Path setEnvWindowsFile, Path bonitaXmlFile, Path bitronixFile, File driverFile,
            Path targetDriverDirectory) throws PlatformException {
        LOGGER.warn("Problem encountered, restoring previous configuration");
        // undo file copies:
        restoreOriginalFile(bonitaXmlFile);
        restoreOriginalFile(bitronixFile);
        restoreOriginalFile(setEnvUnixFile);
        restoreOriginalFile(setEnvWindowsFile);
        final String driverFileName = driverFile.getName();
        try {
            Files.deleteIfExists(targetDriverDirectory.resolve(driverFileName));
        } catch (IOException e) {
            throw new PlatformException("Fail to delete copied driver file " + driverFileName + ": " + e.getMessage(), e);
        }
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
        if (!H2_DB_VENDOR.equals(dbVendor)) {
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
        return null;
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
