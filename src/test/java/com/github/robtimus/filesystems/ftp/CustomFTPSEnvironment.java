package com.github.robtimus.filesystems.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.IOException;

public class CustomFTPSEnvironment extends FTPSEnvironment {

    @Override
    void initializePostConnect(FTPClient client) throws IOException {
        super.initializePostConnect(client);

        // if we don't have this, the python ftp server cannot list files
        // apparently, it doesn't properly support the -a option, eg LIST -a dir1
        client.setListHiddenFiles(false);

        FTPSClient ftpsClient = ((FTPSClient) client);
        ftpsClient.execPBSZ(0);
        ftpsClient.execPROT("P");
    }
}
