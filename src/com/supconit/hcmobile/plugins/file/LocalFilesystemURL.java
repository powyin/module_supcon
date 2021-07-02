package com.supconit.hcmobile.plugins.file;

import android.net.Uri;

public class LocalFilesystemURL {
	
	public static final String FILESYSTEM_PROTOCOL = "cdvfile";

    public final Uri uri;
    public final String fsName;
    public final String path;
    public final boolean isDirectory;

	private LocalFilesystemURL(Uri uri, String fsName, String fsPath, boolean isDirectory) {
		this.uri = uri;
        this.fsName = fsName;
        this.path = fsPath;
        this.isDirectory = isDirectory;
	}

    public static LocalFilesystemURL parse(Uri uri) {
        if (!FILESYSTEM_PROTOCOL.equals(uri.getScheme())) {
            return null;
        }
        String path = uri.getPath();
        if (path.length() < 1) {
            return null;
        }
        int firstSlashIdx = path.indexOf('/', 1);
        if (firstSlashIdx < 0) {
            return null;
        }
        String fsName = path.substring(1, firstSlashIdx);
        path = path.substring(firstSlashIdx);
        boolean isDirectory = path.charAt(path.length() - 1) == '/';
        return new LocalFilesystemURL(uri, fsName, path, isDirectory);
    }

    public static LocalFilesystemURL parse(String uri) {
        return parse(Uri.parse(uri));
    }

    public String toString() {
        return uri.toString();
    }
}
