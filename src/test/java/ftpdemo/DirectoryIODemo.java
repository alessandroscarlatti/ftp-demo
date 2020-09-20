package ftpdemo;

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DirectoryIODemo {

    private static FtpRemoteFileTemplate ftpTemplate;
    private static Path sandboxDir;
    private static Path downloadDir = Paths.get("sandbox/download");

    @BeforeClass
    public static void setup() throws Exception {
        DefaultFtpSessionFactory ftpSessionFactory = new DefaultFtpSessionFactory();
        ftpSessionFactory.setUsername("user");
        ftpSessionFactory.setPassword("password");
        ftpTemplate = new FtpRemoteFileTemplate(ftpSessionFactory);

        Files.createDirectories(downloadDir);
        FileUtils.cleanDirectory(downloadDir.toFile());
    }

    @Test
    public void iterateAllFilesFlat() {
        FTPFile[] files = ftpTemplate.list("dir1");
        for (FTPFile file : files) {
            if (file.isDirectory()) {
                System.out.println("Directory: " + file);
            }
            if (file.isFile()) {
                System.out.println("File: " + file);
            }
        }
    }

    @Test
    public void createDirectories() {
        ftpTemplate.executeWithClient((FTPClient client) -> {
            try {
                client.mkd("asdf");
                return null;
            } catch (Exception e) {
                throw new IllegalStateException("Error creating directory", e);
            }
        });
    }
}
