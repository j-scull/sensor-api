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

    public static Single<File> copyFiles(FileSystem fileSystem, String source, String dest) {
        return fileSystem.rxCopyRecursive(source, dest, true)
                .andThen(Single.just(new File(dest)));
    }

    public static Single<File> copyFile(FileSystem fileSystem, String sourceFilePath, String destFilePath) {
        return fileSystem.rxCopy(sourceFilePath, destFilePath)
                .andThen(Single.just(new File(destFilePath)));
    }

    public static Single<File> replaceFile(FileSystem fileSystem, String sourceFilePath, String destFilePath) {
        return fileSystem.rxDelete(destFilePath)
                .andThen(copyFile(fileSystem, sourceFilePath, destFilePath));
    }
    
}
