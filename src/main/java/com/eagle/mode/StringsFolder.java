
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

    public ArrayList<StringsFile> getStringsFiles() {
        if (mStringsFiles == null) {
            mStringsFiles = findStringsFiles();
        }
        return mStringsFiles;
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
        for(StringsFile stringsFile : mStringsFiles){
            stringsFile.parser();
        }
    }

    public void writeToExcel(WritableSheet sheet, int rows, boolean isEnglish,
            HashMap<String, Integer> idsMap, StringsFile strFile) throws RowsExceededException,
            WriteException {

        int currenRow = rows + 1;
        ArrayList<StringObj> mStrs = strFile.getAllStrs();
        int size = mStrs.size();
        HashMap<String, ConfigItem> languages = Config.getInstance().getLanguages();
        String languageKey = strFile.getValuesFolderName();
        if (!languages.containsKey(languageKey)) {
            return;
        }
        ConfigItem item = languages.get(languageKey);
        for (int i = 0; i < size; i++) {
            String[] ids = mStrs.get(i).getAllIds();
            String[] values = mStrs.get(i).getAllValues();
            for (int j = 0; j < ids.length; j++) {
                if (isEnglish) {
                    if (i == 0 && j == 0) {
                        WritableCellFormat writableCellFormat = mApp.getCellFormat(CellType.APP_NAME);
                        Label label = new Label(Utils.APP_NAME_COLUMN_INDEX, currenRow,
                                strFile.getAppName(), writableCellFormat);
                        sheet.addCell(label);
                        writableCellFormat = mApp.getCellFormat(CellType.NORMAL);
                        label = new Label(Utils.STRING_FILE_NAME_INDEX, currenRow,
                                strFile.getFileName(), writableCellFormat);
                        sheet.addCell(label);
                    }
                    WritableCellFormat writableCellFormat = mApp.getCellFormat(CellType.NORMAL);
                    Label label = new Label(Utils.ID_COLUMN_INDEX, currenRow, ids[j],
                            writableCellFormat);
                    sheet.addCell(label);
                    if (Utils.isContainSpecialCharacter(values[j])) {
                        writableCellFormat = mApp.getCellFormat(CellType.SPECIAL);
                    }
                    label = new Label(item.getIndex(),
                            currenRow, values[j], writableCellFormat);
                    if (ids[j].startsWith(Utils.SURFIX_ARRAY)
                            || ids[j].startsWith(Utils.SURFIX_PLURALS)) {
                        ids[j] = ids[j] + j;
                    }
                    idsMap.put(ids[j], currenRow);
                    sheet.addCell(label);
                    currenRow += 1;
                } else {
                    if (ids[j].startsWith(Utils.SURFIX_ARRAY)
                            || ids[j].startsWith(Utils.SURFIX_PLURALS)) {
                        ids[j] = ids[j] + j;
                    }
                    if (idsMap.get(ids[j]) == null) {
                        Utils.loge("error => " + ids[j] + " can't found in values/strings.xml");
                        break;
                    }
                    WritableCellFormat writableCellFormat = null;
                    if (Utils.isContainSpecialCharacter(values[j])) {
                        writableCellFormat = mApp.getCellFormat(CellType.SPECIAL);
                    } else {
                        writableCellFormat = mApp.getCellFormat(CellType.NORMAL);
                    }
                    Label label = new Label(item.getIndex(),
                            idsMap.get(ids[j]), values[j],
                            writableCellFormat);
                    sheet.addCell(label);
                }
            }
        }
    }
}
