package org.bonitasoft.engine.bpm.bar;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

import org.bonitasoft.engine.io.IOUtil;

/**
 * @author Laurent Leseigneur
 */
class BarResourceVisitor extends SimpleFileVisitor<Path> {

    private BusinessArchive businessArchive;
    private final Path barRootFolder;
    private Path barResourceFolder;
    private int resourcesCount;

    public BarResourceVisitor(BusinessArchive businessArchive, Path barRootFolder) {
        this.businessArchive = businessArchive;
        this.barRootFolder = barRootFolder;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (dir.equals(Paths.get(barRootFolder + "/" + ExternalResourceContribution.EXTERNAL_RESOURCE_FOLDER))) {
            this.barResourceFolder = dir;
            return FileVisitResult.CONTINUE;
        }
        if (dir.equals(barRootFolder)) {
            return FileVisitResult.CONTINUE;
        }
        if (barResourceFolder != null && dir.startsWith(barResourceFolder)) {
            return FileVisitResult.CONTINUE;
        }
        return FileVisitResult.SKIP_SUBTREE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Objects.requireNonNull(file);
        Objects.requireNonNull(attrs);
        businessArchive.addResource(barRootFolder.relativize(file).toString().replace(File.separator, "/"), IOUtil.getAllContentFrom(file.toFile()));
        resourcesCount++;
        return FileVisitResult.CONTINUE;
    }

    public int getResourcesCount() {
        return resourcesCount;
    }

}
