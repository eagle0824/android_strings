
package main.java.com.eagle.mode;

import main.java.com.eagle.config.Config;

import java.io.File;
import java.util.ArrayList;

public class StringsFolder {

    private static final String TAG = StringsFolder.class.getSimpleName();

    private ArrayList<StringsFile> mStringsFiles;

    private String mPath;
    private App mApp;

    public StringsFolder(String folderPath, App app) {
        mPath = folderPath;
        mApp = app;
    }

    public String getPath() {
        return mPath;
    }

    public App getApp() {
        return mApp;
    }

    public String getName() {
        return new File(mPath).getName();
    }

    public ArrayList<StringsFile> getStringsFiles() {
        if (mStringsFiles == null) {
            mStringsFiles = findStringsFiles();
        }
        return mStringsFiles;
    }

    public StringsFile getStringFileByName(String fileName) {
        ArrayList<StringsFile> stringFiles = getStringsFiles();
        for (StringsFile stringsFile : stringFiles) {
            if (stringsFile.getFileName().equals(fileName)) {
                return stringsFile;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(TAG).append(" : ").append(mPath);
        return sb.toString();
    }

    public ArrayList<StringsFile> findStringsFiles() {
        ArrayList<StringsFile> stringsFiles = new ArrayList<StringsFile>();
        ArrayList<String> defaultStringsNames = Config.getInstance().getStringFiles();
        for (String defaultStringName : defaultStringsNames) {
            File stringsFile = new File(getPath(), defaultStringName);
            if (stringsFile.exists() && stringsFile.isFile()) {
                stringsFiles.add(new StringsFile(stringsFile.getAbsolutePath(), this));
            }
        }
        return stringsFiles;
    }

    public void parser() {
        ArrayList<StringsFile> mStringsFiles = getStringsFiles();
        for (StringsFile stringsFile : mStringsFiles) {
            stringsFile.parser();
        }
    }
}
