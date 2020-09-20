package ftpdemo;

import com.github.robtimus.filesystems.ftp.CustomFTPSEnvironment;
import com.github.robtimus.filesystems.ftp.FTPSEnvironment;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * NOTE:
 * Install the server.crt file in your Java keystore in order to use SSL.
 */
public class FileIODemoFtpFileSystemSsl {

    private static FileSystem ftp;
    private static Path sandboxDir;
    private static Path downloadDir = Paths.get("sandbox/download");

    @BeforeClass
    public static void setup() throws Exception {
        FTPSEnvironment env = new CustomFTPSEnvironment()
                .withCredentials("user", "password".toCharArray());

        ftp = FileSystems.newFileSystem(URI.create("ftp://localhost"), env);

        Files.createDirectories(downloadDir);
        FileUtils.cleanDirectory(downloadDir.toFile());
    }

    @Test
    public void readFile() throws Exception {
        String file = new String(Files.readAllBytes(ftp.getPath("test1.remote.txt")));
        System.out.println("File is: " + file);
    }

    @Test
    public void writeFile() throws Exception {
        Files.write(ftp.getPath("test1.remote.txt"), LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME).getBytes());
        String file = new String(Files.readAllBytes(ftp.getPath("test1.remote.txt")));
        System.out.println("File is: " + file);
    }

    @Test
    public void renameFile() throws Exception {
        Files.move(ftp.getPath("test.upload.txt"), ftp.getPath("test.upload.moved.txt"));
    }

    @Test
    public void copyFile() throws Exception {
        // this actually works ?!!
        Files.copy(ftp.getPath("copy_test/file2.txt"), ftp.getPath("copy_test/file2_copy.txt"));
    }

    @Test
    public void renameFileMovingDir() throws Exception {
        // this actually moves the file from one dir to another dir
        Files.move(ftp.getPath("rename_test/file3.txt"), ftp.getPath("rename_target/file3.txt"));
    }

    @Test
    public void deleteFile() throws Exception {
        Files.delete(ftp.getPath("test1.remote.txt"));
    }

    @Test
    public void getFileModifiedDate() throws Exception {
        FileTime time = Files.getLastModifiedTime(ftp.getPath("test1.remote.txt"));
        System.out.println("Modified time: " + time);
        // was 2020-09-19T17:56:20Z
        // became 2020-09-19T20:15:16Z
    }

    @Test
    public void getFileSize() throws Exception {
        long size = Files.size(ftp.getPath("test1.remote.txt"));
        System.out.println("Bytes: " + size);
    }

    @Test
    public void walkFileTree() throws Exception {
        Files.walkFileTree(ftp.getPath("dir1"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println("dir: " + dir);
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println("file: " + file);
                return super.visitFile(file, attrs);
            }
        });
    }

    @Test
    public void listFiles() throws Exception {
        for (Path file : Files.list(ftp.getPath("dir1")).collect(Collectors.toList())) {
            if (Files.isDirectory(file)) {
                System.out.println("Directory: " + file);
            } else {
                System.out.println("File: " + file);
            }
        }
    }

    @Test
    public void makeNestedDirectories() throws Exception {
        Files.createDirectories(ftp.getPath("dir6/dir7/dir8"));
    }

    @Test
    public void copyNestedDirectories() throws Exception {
        copyFolder(ftp.getPath("dir1"), downloadDir.resolve("dir1.download"));
        copyFolder(downloadDir.resolve("dir1.download"), ftp.getPath("dir1.upload"));
    }

    @Test
    public void downloadNestedDirectories() throws Exception {
        copyFolder(ftp.getPath("dir1"), downloadDir.resolve("dir1.download"));
    }

    public void copyFolder(Path source, Path target, CopyOption... options)
            throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                    throws IOException {
                Files.createDirectories(target.resolve(source.relativize(dir).toString()));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.copy(file, target.resolve(source.relativize(file).toString()), options);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
