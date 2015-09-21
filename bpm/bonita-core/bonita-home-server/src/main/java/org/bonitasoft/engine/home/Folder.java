package org.bonitasoft.engine.home;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.bonitasoft.engine.DeepRegexFileFilter;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
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
        this.folder = new File(folder.getAbsolutePath());
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

    public File[] listFiles() throws IOException {
        checkFolderExists();
        return folder.listFiles();
    }

    public void delete()throws IOException  {
        //System.err.println("DELETING FOLDER: " + folder);
        checkFolderExists();
        IOUtil.deleteDir(folder);
    }

    public File getFile(final String name) throws IOException {
        checkFolderExists();
        return new File(folder, name);
    }

    public byte[] getFileContent(final String name) throws IOException {
        checkFolderExists();
        final File file = new File(folder, name);
        return IOUtil.getAllContentFrom(file);
    }

    public File newFile(final String name) throws IOException {
        checkFolderExists();
        final File newFile = new File(folder, name);
        newFile.createNewFile();
        return newFile;
    }

    public File[] listFiles(FileFilter filter) throws IOException {
        checkFolderExists();
        return folder.listFiles(filter);
    }

    public void deleteFile(String fileName) throws IOException {
        checkFolderExists();
        final File file = getFile(fileName);
        if (!file.exists()) {
            //FIXME WARN ?
            return;
            //throw new IOException("File denoted by path " + file.getAbsolutePath() + " does not exist.");
        }
        file.delete();
    }

    public List<File> listFiles(FilenameFilter filter) throws IOException {
        checkFolderExists();
        File[] filesArray = folder.listFiles(filter);
        List<File> files = Arrays.asList(filesArray);
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File file, File t1) {
                return file.getName().compareTo(t1.getName());
            }
        });
        return files;
    }

    public void create() throws IOException {
        //System.err.println("CREATING FOLDER: " + folder);
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
        final Map<String, byte[]> res = new HashMap<String, byte[]>(files.size());
            for (final File file : files) {
                final String key = Util.generateRelativeResourcePath(getFile(), file);
                final byte[] value = IOUtil.getAllContentFrom(file);
                res.put(key, value);
            }
        return res;
    }

    public void writeBusinessArchive(BusinessArchive businessArchive) throws IOException {
        BusinessArchiveFactory.writeBusinessArchiveToFolder(businessArchive, getFile());
    }

    public Map<String, byte[]> listFilesAsResources() throws IOException {
        checkFolderExists();
        final Map<String, byte[]> resources = new HashMap<String, byte[]>();
        for (File file : this.folder.listFiles()) {
            resources.put(file.getName(), IOUtil.getAllContentFrom(file));
        }
        return resources;
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

    public void createIfNotExists() throws IOException {
        if (!this.folder.exists()) {
            create();
        }
    }

    public void createAsTemporaryFolder() {
        org.bonitasoft.engine.io.IOUtil.createTempDirectory(this.folder.toURI());
    }
}
