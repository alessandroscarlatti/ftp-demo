package ftpdemo;

import com.github.robtimus.filesystems.ftp.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.integration.expression.SupplierExpression;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;
import org.springframework.messaging.support.GenericMessage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileIODemoFtpFileSystem {

    private static FileSystem ftp;
    private static Path sandboxDir;
    private static Path downloadDir = Paths.get("sandbox/download");

    @BeforeClass
    public static void setup() throws Exception {
//        FTPClientConfig config = new FTPClientConfig();
//        config.setUnparseableEntries(true);

        FTPEnvironment env = new CustomFTPEnvironment()
//                .withClientConfig(config)
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
        Files.write(ftp.getPath("test.upload.txt"), LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME).getBytes());
        String file = new String(Files.readAllBytes(ftp.getPath("test.upload.txt")));
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
}
