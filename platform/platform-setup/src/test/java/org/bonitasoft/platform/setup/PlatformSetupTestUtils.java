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

package org.bonitasoft.platform.setup;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.OS;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang3.text.StrSubstitutor;

/**
 * @author Baptiste Mesta
 */
public class PlatformSetupTestUtils {

    private static void writeZipInputToFile(final ZipInputStream zipInputstream, final File outputFile) throws FileNotFoundException, IOException {
        // The input is a file. An FileOutputStream is created to write the content of the new file.
        outputFile.getParentFile().mkdirs();
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            // The contents of the new file, that is read from the ZipInputStream using a buffer (byte []), is written.
            int bytesRead;
            final byte[] buffer = new byte[1024];
            while ((bytesRead = zipInputstream.read(buffer)) > -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            fileOutputStream.flush();
        } catch (final IOException ioe) {
            // In case of error, the file is deleted
            outputFile.delete();
            throw ioe;
        }
    }

    public static PumpStreamHandler getExecuteStreamHandler(String answer) {
        return new PumpStreamHandler(System.out, System.err, new ByteArrayInputStream(answer.getBytes()));
    }

    private static void extractZipEntries(final ZipInputStream zipInputstream, final File outputFolder) throws IOException {
        ZipEntry zipEntry;
        while ((zipEntry = zipInputstream.getNextEntry()) != null) {
            try {
                // For each entry, a file is created in the output directory "folder"
                final File outputFile = new File(outputFolder.getAbsolutePath(), zipEntry.getName());
                // If the entry is a directory, it creates in the output folder, and we go to the next entry (continue).
                if (zipEntry.isDirectory()) {
                    outputFile.mkdirs();
                    continue;
                }
                writeZipInputToFile(zipInputstream, outputFile);
            } finally {
                zipInputstream.closeEntry();
            }
        }
    }

    private static void unzipToFolder(final InputStream inputStream, final File outputFolder) throws IOException {
        try (ZipInputStream zipInputstream = new ZipInputStream(inputStream)) {
            extractZipEntries(zipInputstream, outputFolder);
        }
    }

    public static void extractDistributionTo(File distFolder) throws IOException {
        File target = new File("target");
        Pattern distribPattern = Pattern.compile("Bonita-platform-setup-.*\\.zip");
        File dist = null;
        for (File file : target.listFiles()) {
            if (distribPattern.matcher(file.getName()).matches()) {
                dist = file;
                break;
            }
        }
        if (dist == null) {
            throw new IllegalStateException("Unable to locate the distribution");
        }

        try (InputStream inputStream = new FileInputStream(dist)) {
            unzipToFolder(inputStream, distFolder);
        }
    }

    public static Connection getJdbcConnection(File distFolder) throws Exception {
        return getJdbcConnection(distFolder, null);
    }

    public static Connection getJdbcConnection(File distFolder, String dbUser) throws IOException, SQLException {
        Properties properties = getDatabaseProperties(distFolder);
        properties.put("h2.database.dir", distFolder.toPath().resolve(properties.getProperty("h2.database.dir")).toString());
        StrSubstitutor strSubstitutor = new StrSubstitutor(new HashMap(properties));
        return DriverManager.getConnection(strSubstitutor.replace(properties.getProperty("h2.url")),
                dbUser != null ? dbUser : properties.getProperty("db.user"), properties.getProperty("db.password"));
    }

    private static Properties getDatabaseProperties(File distFolder) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(distFolder.toPath().resolve("database.properties").toFile()));
        properties.load(new FileInputStream(distFolder.toPath().resolve("internal.properties").toFile()));
        return properties;
    }

    public static DefaultExecutor createExecutor(File distFolder) {
        DefaultExecutor oDefaultExecutor = new DefaultExecutor();
        oDefaultExecutor.setWorkingDirectory(distFolder);
        return oDefaultExecutor;
    }

    public static CommandLine createCommandLine() {
        if (OS.isFamilyWindows() || OS.isFamilyWin9x()) {
            CommandLine oCmdLine = new CommandLine("cmd");
            oCmdLine.addArgument("/c");
            oCmdLine.addArgument("setup.bat");
            return oCmdLine;
        } else {
            CommandLine oCmdLine = new CommandLine("sh");
            oCmdLine.addArgument("setup.sh");
            return oCmdLine;
        }
    }
}
