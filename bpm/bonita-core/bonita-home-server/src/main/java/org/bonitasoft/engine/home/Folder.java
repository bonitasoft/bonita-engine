package org.bonitasoft.engine.home;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.bonitasoft.engine.DeepRegexFileFilter;
import org.bonitasoft.engine.commons.io.IOUtil;

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
        IOUtil.deleteDir(folder);
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
            throw new IOException("Folder denoted by path " + folder.getAbsolutePath() + " cannot be created as its parent does not exist.");
        }
        if (!folder.getParentFile().isDirectory()) {
            throw new IOException("Folder denoted by path " + folder.getAbsolutePath() + " cannot be created as its parent is not a folder.");
        }
        folder.mkdir();
    }

    public void copyTo(Folder destFolder) throws IOException {
        checkFolderExists();
        destFolder.create();
        FileUtils.copyDirectory(this.getFile(), destFolder.getFile());
    }

    public byte[] zip(Folder destFolder) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ZipOutputStream zos = new ZipOutputStream(baos);
        try {
            org.bonitasoft.engine.io.IOUtil.zipDir(this.folder.getAbsolutePath(), zos, destFolder.getFile().getAbsolutePath());
            return baos.toByteArray();
        } finally {
            zos.close();
            baos.close();
        }
    }

    public Map<String, byte[]> getResources(String filenamesPattern) throws IOException {
        final Collection<File> files = FileUtils.listFiles(getFile(), new DeepRegexFileFilter(getFile(), filenamesPattern),
                DirectoryFileFilter.DIRECTORY);
        final Map<String, byte[]> res = new HashMap<>(files.size());
        for (final File file : files) {
            final String key = Util.generateRelativeResourcePath(getFile(), file);
            final byte[] value = IOUtil.getAllContentFrom(file);
            res.put(key, value);
        }
        return res;
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
        return IOUtil.getAllContentFrom(file);
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
        org.bonitasoft.engine.io.IOUtil.createTempDirectory(this.folder.toURI());
    }
}
