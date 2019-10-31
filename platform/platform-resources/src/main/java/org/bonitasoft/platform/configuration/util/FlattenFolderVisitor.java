package org.bonitasoft.platform.configuration.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Objects;

/**
 * @author Laurent Leseigneur
 */
public class FlattenFolderVisitor extends SimpleFileVisitor<Path> {

    private final Map<String, File> flatFileMap;

    public FlattenFolderVisitor(Map<String, File> flatFileMap) {
        this.flatFileMap = flatFileMap;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(basicFileAttributes);
        final File file = path.toFile();
        if (file.isFile()) {
            flatFileMap.put(file.getName(), file);
        }
        return FileVisitResult.CONTINUE;
    }

}
