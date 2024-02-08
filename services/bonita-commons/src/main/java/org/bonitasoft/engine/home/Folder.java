/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.home;

import static org.bonitasoft.engine.commons.io.IOUtil.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;

/**
 * @author Charles Souillard
 */
public class Folder {

    private final File folder;

    public Folder(final File folder) throws IOException {
        if (folder == null) {
            throw new IOException("Folder is null");
        }
        this.folder = folder;
    }

    public Folder(final Folder folder, final String subFolder) throws IOException {
        if (folder == null) {
            throw new IOException("Folder is null");
        }
        this.folder = new File(folder.getFile(), subFolder);
    }

    public File getFile() {
        return this.folder;
    }

    private void checkFolderExists() throws IOException {
        if (!folder.exists()) {
            throw new IOException("Folder denoted by path " + folder.getAbsolutePath() + " does not exist.");
        }
        if (!folder.isDirectory()) {
            throw new IOException("Folder denoted by path " + folder.getAbsolutePath() + " is not a folder.");
        }
    }

    public void delete() throws IOException {
        //System.err.println("DELETING FOLDER: " + folder);
        checkFolderExists();
        deleteDir(folder);
    }

    public File getFile(final String name) throws IOException {
        checkFolderExists();
        return new File(folder, name);
    }

    public File newFile(final String name) throws IOException {
        checkFolderExists();
        final File newFile = new File(folder, name);
        if (!newFile.createNewFile()) {
            throw new IOException("File " + newFile.getAbsolutePath() + " cannot be created");
        }
        return newFile;
    }

    public void create() throws IOException {
        if (!folder.getParentFile().exists()) {
            throw new IOException("Folder denoted by path " + folder.getAbsolutePath()
                    + " cannot be created as its parent does not exist.");
        }
        if (!folder.getParentFile().isDirectory()) {
            throw new IOException("Folder denoted by path " + folder.getAbsolutePath()
                    + " cannot be created as its parent is not a folder.");
        }
        folder.mkdir();
    }

    public void copyTo(Folder destFolder) throws IOException {
        checkFolderExists();
        destFolder.create();
        FileUtils.copyDirectory(this.getFile(), destFolder.getFile());
    }

    public Map<String, byte[]> listFilesAsResources() throws IOException {
        checkFolderExists();
        final Map<String, byte[]> resources = new HashMap<>();
        final File[] files = this.folder.listFiles((FileFilter) FileFileFilter.FILE);
        if (files != null) {
            for (File file : files) {
                resources.put(file.getName(), getFileContent(file));
            }
        }
        return resources;
    }

    protected byte[] getFileContent(File file) throws IOException {
        return getAllContentFrom(file);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Folder{folder=");
        sb.append(folder);
        sb.append(" --- exists:");
        sb.append(folder.exists());
        sb.append(" --- is directory:");
        sb.append(folder.isDirectory());
        sb.append('}');
        return sb.toString();
    }

    public boolean exists() {
        return this.folder.exists();
    }

    public URI toURI() throws IOException {
        checkFolderExists();
        return this.folder.toURI();
    }

    public Folder createIfNotExists() throws IOException {
        if (!this.folder.exists()) {
            create();
        }
        return this;
    }

    public void createAsTemporaryFolder() {
        createTempDirectory(this.folder.toURI());
    }
}
