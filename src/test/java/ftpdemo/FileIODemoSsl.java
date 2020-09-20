package ftpdemo;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.integration.expression.SupplierExpression;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;
import org.springframework.integration.ftp.session.FtpSession;
import org.springframework.messaging.support.GenericMessage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * NOTE:
 * Install the server.crt file in your Java keystore in order to use SSL.
 */
public class FileIODemoSsl {

    private static FtpRemoteFileTemplate ftpTemplate;
    private static Path sandboxDir;
    private static Path downloadDir = Paths.get("sandbox/download");

    @BeforeClass
    public static void setup() throws Exception {
        DefaultFtpSessionFactory ftpSessionFactory = new DefaultFtpSessionFactory() {
            @Override
            protected FTPClient createClientInstance() {
                FTPSClient ftpsClient = new FTPSClient();
                ftpsClient.enterLocalPassiveMode();
                ftpsClient.setBufferSize(1024 * 1024);
                return ftpsClient;
            }

            @Override
            protected void postProcessClientAfterConnect(FTPClient ftpClient) throws IOException {
                super.postProcessClientAfterConnect(ftpClient);

                FTPSClient ftpsClient = (FTPSClient) ftpClient;

                ftpsClient.execPBSZ(0);
                ftpsClient.execPROT("P");
            }
        };
        ftpSessionFactory.setUsername("user");
        ftpSessionFactory.setPassword("password");
        ftpTemplate = new FtpRemoteFileTemplate(ftpSessionFactory);

        Files.createDirectories(downloadDir);
        FileUtils.cleanDirectory(downloadDir.toFile());
    }


    @Test
    public void readFile() {
        ftpTemplate.get("test1.remote.txt", is -> {
            FileUtils.copyInputStreamToFile(is, downloadDir.resolve("test1.download.txt").toFile());
        });
    }

    @Test
    public void writeFileWithClient() {
        ftpTemplate.executeWithClient((FTPClient client) -> {
            try (InputStream is = IOUtils.toInputStream("asdf", "utf-8")) {
                client.storeFile("test1.remote.txt", is);
                return null;
            } catch (Exception e) {
                throw new IllegalStateException("Error sending file", e);
            }
        });
    }

    @Test
    public void writeFileWithMessage() {
        // note that this expression is cheating a bit by using a supplier, rather than parsing the message headers.
        ftpTemplate.setRemoteDirectoryExpression(new SupplierExpression<>(() -> "/"));
        ftpTemplate.send(new GenericMessage<>("qwer"));
        // saves a file as 762fb66f-adb1-d084-166a-438569ea1c46.msg
    }

    @Test
    public void renameFile() {
        ftpTemplate.rename("rename_test/file1.txt", "rename_test/file2.txt");
    }

    @Test
    public void renameFileMovingDir() {
        // this actually moves the file from one dir to another dir
        ftpTemplate.rename("rename_test/file1.txt", "rename_target/file2.txt");
    }

    @Test
    public void deleteFile() {
        ftpTemplate.remove("delete_test/file1.txt");
    }

    @Test
    public void getFileModifiedDate() {
        ftpTemplate.executeWithClient((FTPClient client) -> {
            try {
                String time = client.getModificationTime("modification_time_test/file1.txt");
                System.out.println("Modified Time: " + time);
                // yyyyMMddhhmmss
                // 20200919184858
                return null;
            } catch (Exception e) {
                throw new IllegalStateException("Error getting file modified time", e);
            }
        });
    }

    @Test
    public void getFileSize() {
        ftpTemplate.executeWithClient((FTPClient client) -> {
            try {
                long size = client.mlistFile("file_size_test/file1.txt").getSize();
                System.out.println("Bytes: " + size);
                return null;
            } catch (Exception e) {
                throw new IllegalStateException("Error getting file size", e);
            }
        });
    }
}
