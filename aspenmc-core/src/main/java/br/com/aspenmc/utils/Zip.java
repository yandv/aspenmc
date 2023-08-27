package br.com.aspenmc.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Zip {

    private static final char WINDOWS_SEPARATOR = '\\';
    private static final char UNIX_SEPARATOR = '/';

    public static void make(File source, File output, Predicate<Path> predicate) {
        if (source == null) {
            throw new IllegalArgumentException("Source cannot be null");
        } else if (!source.isDirectory()) {
            throw new IllegalArgumentException("Source is not directory");
        }

        final Path sourcePath = source.toPath();

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(output.toPath()))) {
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (!sourcePath.equals(dir)) {
                        if (predicate != null && !predicate.test(dir))
                            return FileVisitResult.SKIP_SUBTREE;
                        ZipEntry ze = new ZipEntry(relativize(sourcePath, dir) + UNIX_SEPARATOR);
                        ze.setCreationTime(attrs.creationTime());
                        ze.setLastAccessTime(attrs.lastAccessTime());
                        ze.setLastModifiedTime(attrs.lastModifiedTime());
                        zos.putNextEntry(ze);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (predicate == null || predicate.test(file)) {
                        ZipEntry ze = new ZipEntry(relativize(sourcePath, file));
                        ze.setCreationTime(attrs.creationTime());
                        ze.setLastAccessTime(attrs.lastAccessTime());
                        ze.setLastModifiedTime(attrs.lastModifiedTime());
                        zos.putNextEntry(ze);
                        zos.write(Files.readAllBytes(file));
                        zos.closeEntry();
                    }
                    return FileVisitResult.CONTINUE;
                }

            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean unzip(File zip, File output) {
        if (zip == null) {
            throw new IllegalArgumentException("Zip File cannot be null");
        } else if (!zip.isFile()) {
            throw new IllegalArgumentException("Zip File doesn't exists in this specified directory");
        }

        if (output == null) {
            throw new IllegalArgumentException("Output cannot be null");
        } else if (!output.isDirectory()) {
            if (output.exists()) {
                throw new IllegalArgumentException("Output is not directory");
            } else if (!output.mkdir()) {
                throw new RuntimeException("The Map Generator can't create the default folder");
            }
        }

        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zip.toPath()))) {
            ZipEntry ze;

            while ((ze = zis.getNextEntry()) != null) {
                Path path = Paths.get(output.getPath() + File.separator + separatorsToSystem(ze.getName()));

                if (ze.isDirectory()) {
                    if (!Files.exists(path)) {
                        Files.createDirectory(path);
                    }
                } else {
                    Files.copy(zis, path, StandardCopyOption.REPLACE_EXISTING);
                }

                Files.getFileAttributeView(path, BasicFileAttributeView.class).setTimes(ze.getLastModifiedTime(),
                        ze.getLastAccessTime(), ze.getCreationTime());
            }

            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public static void deleteDirectory(File directory) {
        File[] allContents = directory.listFiles();
        if (allContents != null) {
            File[] files = allContents;

            int contentLenght = allContents.length;

            for (int i = 0; i < contentLenght; i++) {
                File file = files[i];

                deleteDirectory(file);
            }
        }
        directory.delete();
    }

    private static String relativize(Path start, Path end) {
        return separatorsToUnix(start.relativize(end).toString());
    }

    public static boolean isSystemWindows() {
        return File.separatorChar == WINDOWS_SEPARATOR;
    }

    public static String separatorsToUnix(final String path) {
        if (path == null || path.indexOf(WINDOWS_SEPARATOR) == -1) {
            return path;
        }
        return path.replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
    }

    public static String separatorsToWindows(final String path) {
        if (path == null || path.indexOf(UNIX_SEPARATOR) == -1) {
            return path;
        }
        return path.replace(UNIX_SEPARATOR, WINDOWS_SEPARATOR);
    }

    public static String separatorsToSystem(final String path) {
        if (path == null) {
            return null;
        }
        return isSystemWindows() ? separatorsToWindows(path) : separatorsToUnix(path);
    }

}
