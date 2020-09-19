package com.github.robtimus.filesystems.ftp;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;

public class CustomFTPEnvironment extends FTPEnvironment {

    @Override
    void initializePreConnect(FTPClient client) throws IOException {
        super.initializePreConnect(client);

        // if we don't have this, the python ftp server cannot list files
        // apparently, it doesn't properly support the -a option, eg LIST -a dir1
        client.setListHiddenFiles(false);
    }
}
