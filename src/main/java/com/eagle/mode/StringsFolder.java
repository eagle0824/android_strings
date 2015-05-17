
package main.java.com.eagle.mode;

import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import main.java.com.eagle.Utils;
import main.java.com.eagle.config.Config;
import main.java.com.eagle.config.ConfigItem;
import main.java.com.eagle.mode.App.CellType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class StringsFolder {

    private static final String TAG = StringsFolder.class.getSimpleName();

    private ArrayList<StringsFile> mStringsFiles;

    private String mPath;
    private App mApp;

    private static HashMap<String, HashMap<String, Integer>> mIdsMap;

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

    public void writeToExcel(WritableSheet sheet, boolean isEnglish)
            throws RowsExceededException,
            WriteException {
        HashMap<String, ConfigItem> languages = Config.getInstance().getLanguages();
        String folderName = getName();
        if (!isEnglish && languages.size() > 0
                && !languages.containsKey(folderName)) {
            return;
        }

        if (isEnglish) {
            mIdsMap = new HashMap<String, HashMap<String, Integer>>();
        }
        ArrayList<StringsFile> stringsFiles = getStringsFiles();
        int size = stringsFiles.size();
        for (int i = 0; i < size; i++) {
            String fileName = stringsFiles.get(i).getFileName();
            if (isEnglish) {
                mIdsMap.put(fileName, new HashMap<String, Integer>());
            }
            int offset = i == 0 ? 0 : 2;
            int startRowIndex = sheet.getRows() -1  + offset;
            stringsFiles.get(i).writeToExcel(sheet, mIdsMap.get(fileName), offset);
            int endRowIndex = sheet.getRows() - 1;
            if (endRowIndex > startRowIndex) {
                sheet.mergeCells(Utils.STRING_FILE_NAME_INDEX, startRowIndex,
                        Utils.STRING_FILE_NAME_INDEX, endRowIndex);
            }
        }

    }
}
