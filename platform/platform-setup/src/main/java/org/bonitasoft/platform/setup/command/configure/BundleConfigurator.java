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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringEscapeUtils;
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
abstract class BundleConfigurator {

    protected final static Logger LOGGER = LoggerFactory.getLogger(BundleConfigurator.class);

    static final String H2 = "h2";
    static final String MYSQL = "mysql";
    static final String ORACLE = "oracle";
    static final String POSTGRES = "postgres";
    static final String SQLSERVER = "sqlserver";

    private static final String TOMCAT_TEMPLATES_FOLDER = "tomcat-templates";

    static final String APPSERVER_FOLDERNAME = "server";

    private Path rootPath;

    DatabaseConfiguration standardConfiguration;
    DatabaseConfiguration bdmConfiguration;
    private Path backupsFolder;
    private String timestamp;

    BundleConfigurator(Path rootPath) throws PlatformException {
        try {
            this.rootPath = rootPath.toRealPath();
        } catch (IOException e) {
            throw new PlatformException("Unable to determine root path for " + getBundleName());
        }
        timestamp = new SimpleDateFormat("yyyy-MM-dd_HH'h'mm'm'ss's'").format(new Date());
    }

    void loadProperties() throws PlatformException {
        final Properties properties = new PropertyLoader().loadProperties();

        standardConfiguration = new DatabaseConfiguration("", properties, rootPath);
        bdmConfiguration = new DatabaseConfiguration("bdm.", properties, rootPath);
        try {
            final Path dbFile = Paths.get(this.getClass().getResource("/database.properties").toURI());
            LOGGER.info(getBundleName() + " environment detected with root " + rootPath);
            LOGGER.info("Running auto-configuration using file " + dbFile.normalize());
        } catch (URISyntaxException e) {
            throw new PlatformException("Configuration file 'database.properties' not found");
        }
    }

    protected abstract String getBundleName();

    abstract void configureApplicationServer() throws PlatformException;

    void createBackupFolderIfNecessary(String backupFolder) throws PlatformException {
        backupsFolder = getPath(backupFolder);
        if (Files.notExists(backupsFolder)) {
            try {
                Files.createDirectory(backupsFolder);
            } catch (IOException e) {
                throw new PlatformException("Could not create backup folder: " + backupFolder, e);
            }
        }
    }

    Path getTemplateFolderPath(String templateFile) throws PlatformException {
        return getPath("setup").resolve(TOMCAT_TEMPLATES_FOLDER).resolve(templateFile);
    }

    void backupAndReplaceContentIfNecessary(Path path, String newContent, String message) throws PlatformException {
        String previousContent = readContentFromFile(path);
        if (!previousContent.equals(newContent)) {
            makeBackupOfFile(path);
            writeContentToFile(path, newContent);
            LOGGER.info(message);
        } else {
            LOGGER.info("Same configuration detected for file '" + getRelativePath(path) + "'. No need to change it.");
        }
    }

    boolean copyDatabaseDriversIfNecessary(Path srcDriverFile, Path targetDriverFile, String dbVendor) throws PlatformException {
        if (srcDriverFile == null || targetDriverFile == null) {
            return false;
        }
        if (Files.exists(targetDriverFile)) {
            LOGGER.info("Your " + dbVendor + " driver file '" + getRelativePath(targetDriverFile) + "' already exists. Skipping the copy.");
            return false;
        }
        copyDriverFile(srcDriverFile, targetDriverFile, dbVendor);
        return true;
    }

    Path getRelativePath(Path pathToRelativize) {
        return rootPath.toAbsolutePath().relativize(pathToRelativize.toAbsolutePath());
    }

    void copyDriverFile(Path srcDriverFile, Path targetDriverFile, String dbVendor) throws PlatformException {
        try {
            final Path targetDriverFolder = targetDriverFile.getParent();
            targetDriverFolder.toFile().mkdirs();
            Files.copy(srcDriverFile, targetDriverFile);
            LOGGER.info("Copying your " + dbVendor + " driver file '" + getRelativePath(srcDriverFile) + "' to tomcat lib folder '"
                    + getRelativePath(targetDriverFolder) + "'");
        } catch (IOException e) {
            throw new PlatformException(
                    "Fail to copy driver file lib/" + srcDriverFile.getFileName() + " to " + targetDriverFile.toAbsolutePath() + ": " + e.getMessage(), e);
        }
    }

    static String replaceValues(String content, Map<String, String> replacementMap) {
        for (Map.Entry<String, String> entry : replacementMap.entrySet()) {
            content = content.replaceAll(entry.getKey(), entry.getValue());
        }
        return content;
    }

    // Visible for testing
    static String convertWindowsBackslashes(String value) {
        // forward slashes is valid in database connection URLs on Windows, and and easier and more homogeneous to manage in
        return value.replaceAll("\\\\", "/");
    }

    void writeContentToFile(Path path, String content) throws PlatformException {
        try {
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new PlatformException("Fail to replace content in file " + path + ": " + e.getMessage(), e);
        }
    }

    String readContentFromFile(Path bonitaXmlFile) throws PlatformException {
        try {
            return new String(Files.readAllBytes(bonitaXmlFile), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PlatformException("Cannot read content of text file " + bonitaXmlFile.toAbsolutePath().toString(), e);
        }
    }

    void restoreOriginalFile(Path bonitaXmlFile) throws PlatformException {
        try {
            if (Files.exists(bonitaXmlFile)) {
                Files.delete(bonitaXmlFile);
            }
            final Path backupFile = getBackupFile(bonitaXmlFile);
            if (Files.exists(backupFile)) {
                Files.move(backupFile, bonitaXmlFile);
            }
        } catch (IOException e) {
            throw new PlatformException("Fail to restore original file for " + bonitaXmlFile + ": " + e.getMessage(), e);
        }
    }

    private Path makeBackupOfFile(Path originalFile) throws PlatformException {
        final Path originalFileName = originalFile.getFileName();
        final Path backup = getBackupFile(originalFile);
        LOGGER.info(
                "Creating a backup of configuration file '" + getRelativePath(originalFile).normalize() + "' to '" + getRelativePath(backup).normalize() + "'");
        try {
            Files.copy(originalFile, backup);
            return backup;
        } catch (IOException e) {
            throw new PlatformException("Fail to make backup file for " + originalFileName + ": " + e.getMessage(), e);
        }
    }

    private Path getBackupFile(Path originalFile) {
        return backupsFolder.resolve(originalFile.getFileName() + "." + getTimestamp());
    }

    private String getTimestamp() {
        return timestamp;
    }

    File getDriverFile(String dbVendor) throws PlatformException {
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
        if (SQLSERVER.equals(dbVendor)) {
            return ".*(sqlserver|mssql|sqljdbc).*\\.(jar|zip)";
        }
        return ".*" + dbVendor + ".*";
    }

    /**
     * Constructs path relative to rootPath.
     *
     * @param partialPath path relative to rootPath
     * @return the real path, constructed from rootPath, appending the partialPath
     */
    protected Path getPath(String partialPath) throws PlatformException {
        return getPath(partialPath, false);
    }

    /**
     * Constructs path relative to rootPath.
     *
     * @param partialPath path relative to rootPath
     * @param failIfNotExist should we fail if file does not exist?
     * @return the real path, constructed from rootPath, appending the partialPath
     */
    protected Path getPath(String partialPath, boolean failIfNotExist) throws PlatformException {
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

    // Visible for testing
    static String escapeXmlCharacters(String url) {
        return StringEscapeUtils.escapeXml11(url);
    }

    protected Path getOptionalPathUnderAppServer(String path) throws PlatformException {
        return getPath(APPSERVER_FOLDERNAME + "/" + path, false);
    }

    protected Path getPathUnderAppServer(String path, boolean failIfNotExist) throws PlatformException {
        return getPath(APPSERVER_FOLDERNAME + "/" + path, failIfNotExist);
    }

    protected static String getDatabaseConnectionUrlForXmlFile(DatabaseConfiguration configuration) {
        return escapeXmlCharacters(Matcher.quoteReplacement(getDatabaseConnectionUrl(configuration)));
    }

    protected static String getDatabaseConnectionUrlForPropertiesFile(DatabaseConfiguration configuration) {
        String url = getDatabaseConnectionUrl(configuration);
        if (H2.equals(configuration.getDbVendor())) {
            url = StringEscapeUtils.escapeJava(url);
        }
        return Matcher.quoteReplacement(url);
    }

    static String getDatabaseConnectionUrl(DatabaseConfiguration configuration) {
        String url = configuration.getUrl();
        if (H2.equals(configuration.getDbVendor())) {
            url = convertWindowsBackslashes(url);
        }
        return url;
    }

}
