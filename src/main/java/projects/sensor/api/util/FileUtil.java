package projects.sensor.api.util;

import io.reactivex.Single;
import io.vertx.reactivex.core.file.FileSystem;

import java.io.File;

public class FileUtil {

    public static Single<File> createDirectory(FileSystem fileSystem, String dest) {
        return fileSystem.rxMkdir(dest)
                .andThen(Single.just(new File(dest)));
    }
    public static Single<Boolean> fileExists(FileSystem fileSystem, String filePath) {
        return fileSystem.rxExists(filePath);
    }

    public static Single<File> copyFiles(FileSystem fileSystem, String source, String dest, boolean replaceExisting) {
        if (replaceExisting) {
            return copyFilesReplaceIfExists(fileSystem, source, dest);
        } else {
            return copyFiles(fileSystem, source, dest);
        }
    }

    private static Single<File> copyFiles(FileSystem fileSystem, String source, String dest) {
        return fileSystem.rxCopyRecursive(source, dest, true)
                .andThen(Single.just(new File(dest)));
    }

    private static Single<File> copyFilesReplaceIfExists(FileSystem fileSystem, String source, String dest) {
        return fileSystem.deleteRecursive(dest, true)
                .rxCopyRecursive(source, dest, true)
                .andThen(Single.just(new File(dest)));
    }

    public static Single<File> copyFile(FileSystem fileSystem, String sourceFilePath, String destFilePath, boolean replaceIfExists) {
        if (replaceIfExists) {
            return copyFileReplaceIfExists(fileSystem, sourceFilePath, destFilePath);
        } else {
            return copyFile(fileSystem, sourceFilePath, destFilePath);
        }
    }

    private static Single<File> copyFile(FileSystem fileSystem, String sourceFilePath, String destFilePath) {
        return fileSystem.rxCopy(sourceFilePath, destFilePath)
                .andThen(Single.just(new File(destFilePath)));
    }

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

    public static Single<File> replaceFile(FileSystem fileSystem, String sourceFilePath, String destFilePath) {
        return fileSystem.rxDelete(destFilePath)
                .andThen(copyFile(fileSystem, sourceFilePath, destFilePath));
    }
    
}
