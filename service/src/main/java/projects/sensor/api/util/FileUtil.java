package projects.sensor.api.util;

import io.reactivex.Single;
import io.vertx.reactivex.core.file.FileSystem;

import java.io.File;

public class FileUtil {

    /**
     * Creates a directory
     * @param fileSystem - Vertx filesystem
     * @param dest - String path to the file
     * @return - A file handle to the created directory
     */
    public static Single<File> createDirectory(FileSystem fileSystem, String dest) {
        return fileSystem.rxMkdir(dest)
                .andThen(Single.just(new File(dest)));
    }

    /**
     * Checks if a directory exists
     * @param fileSystem - Vertx filesystem
     * @param filePath - String path to a file
     * @return - true if the file exists, otherwise false
     */
    public static Single<Boolean> fileExists(FileSystem fileSystem, String filePath) {
        return fileSystem.rxExists(filePath);
    }

    /**
     * Copies all files from the source directory to the destination directory
     * @param fileSystem - Vertx filesystem
     * @param source - String path to the  source directory
     * @param dest - String path to the destination directory
     * @param replaceExisting - if true will replace any files already existing in the destination directory
     * @return - A file handle to the destination
     */
    public static Single<File> copyFiles(FileSystem fileSystem, String source, String dest, boolean replaceExisting) {
        if (replaceExisting) {
            return copyFilesReplaceIfExists(fileSystem, source, dest);
        } else {
            return copyFiles(fileSystem, source, dest);
        }
    }

    /**
     * Copies all files from the source directory to the destination directory
     * @param fileSystem - Vertx filesystem
     * @param source - String path to the  source directory
     * @param dest - String path to the destination directory
     * @return - A file handle to the destination directory
     */
    private static Single<File> copyFiles(FileSystem fileSystem, String source, String dest) {
        return fileSystem.rxCopyRecursive(source, dest, true)
                .andThen(Single.just(new File(dest)));
    }

    /**
     * Copies all files from the source directory to the destination directory, replacing any existing files
     * @param fileSystem - Vertx filesystem
     * @param source - String path to the  source directory
     * @param dest - String path to the destination directory
     * @return - A file handle to the destination directory
     */
    private static Single<File> copyFilesReplaceIfExists(FileSystem fileSystem, String source, String dest) {
        return fileSystem.deleteRecursive(dest, true)
                .rxCopyRecursive(source, dest, true)
                .andThen(Single.just(new File(dest)));
    }

    /**
     * Copies a file
     * @param fileSystem - Vertx filesystem
     * @param sourceFilePath - String path to the source file
     * @param destFilePath - String path to the destination file
     * @param replaceIfExists - if true will replace the destination file if it already exists
     * @return - A file handle to the destination file
     */
    public static Single<File> copyFile(FileSystem fileSystem, String sourceFilePath, String destFilePath, boolean replaceIfExists) {
        if (replaceIfExists) {
            return copyFileReplaceIfExists(fileSystem, sourceFilePath, destFilePath);
        } else {
            return copyFile(fileSystem, sourceFilePath, destFilePath);
        }
    }

    /**
     * Copies a file
     * @param fileSystem - Vertx filesystem
     * @param sourceFilePath - String path to the  source file
     * @param destFilePath - String path to the destination file
     * @return - A file handle to the destination file
     */
    private static Single<File> copyFile(FileSystem fileSystem, String sourceFilePath, String destFilePath) {
        return fileSystem.rxCopy(sourceFilePath, destFilePath)
                .andThen(Single.just(new File(destFilePath)));
    }

    /**
     * Copies a file, replaces if already existing
     * @param fileSystem - Vertx filesystem
     * @param sourceFilePath - String path to the  source file
     * @param destFilePath - String path to the destination file
     * @return - A file handle to the destination file
     */
    private static Single<File> copyFileReplaceIfExists(FileSystem fileSystem, String sourceFilePath, String destFilePath) {
        return fileExists(fileSystem, destFilePath)
                .flatMap(exists -> {
                    if (exists) {
                        return replaceFile(fileSystem, sourceFilePath, destFilePath);
                    } else {
                        return copyFile(fileSystem, sourceFilePath, destFilePath);
                    }
                });
    }

    /**
     * Replaces a file
     * @param fileSystem - Vertx filesystem
     * @param sourceFilePath - String path to the  source file
     * @param destFilePath - String path to the destination file
     * @return - A file handle to the destination file
     */
    public static Single<File> replaceFile(FileSystem fileSystem, String sourceFilePath, String destFilePath) {
        return fileSystem.rxDelete(destFilePath)
                .andThen(copyFile(fileSystem, sourceFilePath, destFilePath));
    }

    /**
     * Extracts all files in the source directory to the dest directory.
     * Creates dest directory if it doesn't already exist.
     * @param fileSystem - Vertx filesystem
     * @param source - String path to the  source directory
     * @param dest - String path to the destination directory
     * @return - A file handle to the destination directory
     */
    public static Single<File> extractFilesToDirectory(FileSystem fileSystem, String source, String dest) {
        return fileExists(fileSystem, dest)
                .flatMap(destExists -> {
                    // If destination doesn't exist, we indicate that files are simply to be copied
                    // Else it exists, so we indicate that the files should be replaced
                    if (!destExists) {
                        return createDirectory(fileSystem, dest)
                                .map(file -> false);
                    } else {
                        return Single.just(true);
                    }
                })
                .flatMap(replaceExisting -> fileExists(fileSystem, source)
                        .flatMap(sourceExist -> {
                            if (sourceExist) {
                                return copyFiles(fileSystem, source, dest, replaceExisting);
                            } else {
                                return null;
                            }
                        }));
    }
}
