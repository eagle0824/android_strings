
package main.java.com.eagle.mode;

import main.java.com.eagle.Utils;
import main.java.com.eagle.config.Config;
import main.java.com.eagle.config.ConfigItem;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.format.VerticalAlignment;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/*
 * AppDir is for app dir
 * AppName(mAppName  path is mPath)
 *  -->res
 *      -->values
 *          -->string.xml
 *      -->values-zh-rCN
 *          -->string.xml
 *      -->values-ar
 *          -->string.xml
 *      -->values-zh-rTW
 *          -->string.xml
 *       ........
 */
public class App {
    /*
     * mStringFiles is collection sub values name and sub values dir String is
     * values-* dir name StringFile is
     */
    private HashMap<String, StringsFile> mStringFiles;
    /*
     * mPath is The appDir path
     */
    private String mPath;
    /*
     * mAppName is the appDir name
     */
    private String mAppName;

    private enum CellType {
        NORMAL, TITLE, APP_NAME, SPECIAL
    }

    public App(String appDir) {
        File appFile = new File(appDir);
        if (!appFile.isDirectory()) {
            Utils.loge("dir :" + appFile.getAbsolutePath() + " is not dir!");
            return;
        }
        mPath = appDir;
        mAppName = Utils.getAppName(appDir);
        mStringFiles = new HashMap<String, StringsFile>();
    }

    public App(File file) {
        if (!file.isDirectory()) {
            Utils.loge("dir :" + file.getAbsolutePath() + " is not dir!");
            return;
        }
        mPath = file.getAbsolutePath();
        mAppName = file.getName();
        mStringFiles = new HashMap<String, StringsFile>();
    }

    public App(String name, String path) {
        mPath = path;
        mAppName = name;
        mStringFiles = new HashMap<String, StringsFile>();
    }

    public void parserAppDir() {
        // Utils.logd("parserAppDir path :  " + mPath);
        if (mPath == null || mPath.equals("")) {
            return;
        }
        File file = new File(mPath);
        if (!file.isDirectory()) {
            return;
        }
        if (file.exists()) {
            File mResDir = new File(file, Utils.RES);
            if (mResDir.exists()) {
                Utils.loge("AppDir :  " + mResDir.getAbsolutePath() + " app Name : " + Utils.getAppName(mResDir.getAbsolutePath()));
                File[] resFiles = mResDir.listFiles();
                for (int i = 0; i < resFiles.length; i++) {
                    doParserStringDir(resFiles[i]);
                }
            }
        }
        if (mStringFiles.size() != 0) {
            writeToExcel(this);
        }
    }

    public void doParserStringDir(File file) {
        String dirPath = file.getAbsolutePath();
        if (dirPath.contains(Utils.VALUES)) {
            ArrayList<String> mStringFiles = Config.getInstance().getStringFiles();
            int size = mStringFiles.size();
            for (int i = 0; i < size; i++) {
                File stringFile = new File(file.getAbsolutePath(), mStringFiles.get(i));
                if (stringFile.exists()) {
                    StringsFile strFile = new StringsFile(stringFile.getAbsolutePath(), this);
                    strFile.doParserStringsFile();
                }
            }
        }
    }

    public void addStringFile(StringsFile strFile) {
        String key = strFile.getDirName();
        if (key != null && !key.equals("")) {
            if (!mStringFiles.containsKey(key)) {
                mStringFiles.put(key, strFile);
            } else {
                mStringFiles.get(key).addStrings(strFile.getAllStrs());
            }
        }
    }

    public String getDirName() {
        String[] strs = mPath.split("\\/");
        // Utils.logd(Arrays.toString(strs));
        int len = strs.length;
        if (len > 4) {
            Utils.logd(strs[len - 4]);
            return strs[len - 4];
        }
        return mPath;
    }

    public String getPath() {
        return mPath;
    }

    public void writeToExcel(App appStr) {
        WritableWorkbook wb = null;
        WritableSheet sheet = null;
        Workbook rwb = null;
        String savePath = Config.getInstance().getCommand().getOutputPath();
        File xlsFile = new File(savePath, Utils.NEW_XLS_NAME);
        // Utils.logd("write package " + appStr.mAppName + " to excel path : " +
        // xlsFile.getAbsolutePath());
        try {
            int startRowIndex = 0;
            if (!xlsFile.exists()) {
                wb = Workbook.createWorkbook(xlsFile);
                sheet = wb.createSheet(Utils.SHEET0_NAME, Utils.SHEET0_INDEX);
            } else {
                rwb = Workbook.getWorkbook(xlsFile);
                wb = Workbook.createWorkbook(xlsFile, rwb);
                sheet = wb.getSheet(Utils.SHEET0_INDEX);
            }
            int rows = sheet.getRows();
            if (rows == Utils.LANGUAGE_ROW) {
                initExcelTitle(sheet);
            }
            startRowIndex = rows + 1;
            StringsFile engFile = mStringFiles.get(Utils.VALUES);
            HashMap<String, Integer> idsMap = new HashMap<String, Integer>();

            writeToExcel(sheet, rows, true, idsMap, engFile);

            Set<String> keys = mStringFiles.keySet();
            for (String fileName : keys) {
                if (!fileName.equals(Utils.VALUES)) {
                    rows = sheet.getRows();
                    StringsFile strFile = mStringFiles.get(fileName);
                    if (strFile != null) {
                        writeToExcel(sheet, rows, false, idsMap, strFile);
                    }
                }
            }
            int endRowIndex = sheet.getRows() - 1;
            Utils.logd("start rows : " + startRowIndex + " end rows : " + endRowIndex);
            if (endRowIndex > startRowIndex) {
                sheet.mergeCells(0, startRowIndex, 0, endRowIndex);
            }
            wb.write();
            wb.close();
            if (rwb != null) {
                rwb.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
        // Utils.logd("write package "+ appStr.getAppName() +" to excel end!");
    }

    public void writeToExcel(WritableSheet sheet, int rows, boolean isEnglish,
            HashMap<String, Integer> idsMap, StringsFile strFile) {

        int currenRow = rows + 1;
        ArrayList<StringObj> mStrs = strFile.getAllStrs();
        int size = mStrs.size();
        // Utils.logd("StringFile :" + strFile.getAppName() +
        // "; StringObjs count : " + size + "dir name : " +
        // strFile.getDirName());
        HashMap<String, ConfigItem> languages = Config.getInstance().getLanguages();
        if (!languages.containsKey(strFile.getDirName())) {
            return;
        }
        ConfigItem item = languages.get(strFile.getDirName());
        try {
            for (int i = 0; i < size; i++) {
                String[] ids = mStrs.get(i).getAllIds();
                String[] values = mStrs.get(i).getAllValues();
                for (int j = 0; j < ids.length; j++) {
                    if (isEnglish) {
                        if (i == 0 && j == 0) {
                            WritableCellFormat mWritableCellFormat = getCellFormat(CellType.APP_NAME);
                            Label label = new Label(Utils.APP_NAME_COLUMN_INDEX, currenRow,
                                    strFile.getAppName(), mWritableCellFormat);
                            sheet.addCell(label);
                        }
                        WritableCellFormat mWritableCellFormat = getCellFormat(CellType.NORMAL);
                        Label label = new Label(Utils.ID_COLUMN_INDEX, currenRow, ids[j],
                                mWritableCellFormat);
                        sheet.addCell(label);
                        if (Utils.isContainSpecialCharacter(values[j])) {
                            mWritableCellFormat = getCellFormat(CellType.SPECIAL);
                        }
                        label = new Label(item.getIndex(),
                                currenRow, values[j], mWritableCellFormat);
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
                        WritableCellFormat mWritableCellFormat = null;
                        // Utils.logd("string : " + values[j] +
                        // " isContainSpecialCharacter :" +
                        // Utils.isContainSpecialCharacter(values[j]));
                        if (Utils.isContainSpecialCharacter(values[j])) {
                            mWritableCellFormat = getCellFormat(CellType.SPECIAL);
                        } else {
                            mWritableCellFormat = getCellFormat(CellType.NORMAL);
                        }
                        Label label = new Label(item.getIndex(),
                                idsMap.get(ids[j]), values[j],
                                mWritableCellFormat);
                        // Label label = new
                        // Label(Main.mLanguageIndexes.get(strFile.getDirName()),
                        // idsMap.get(ids[j]), values[j]);
                        sheet.addCell(label);
                    }
                }
            }
        } catch (RowsExceededException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    private void initExcelTitle(WritableSheet sheet) {
        try {
            WritableCellFormat mWritableCellFormat = getCellFormat(CellType.NORMAL);
            Label label = new Label(Utils.APP_NAME_COLUMN_INDEX, Utils.LANGUAGE_ROW, "app name",
                    mWritableCellFormat);
            sheet.addCell(label);
            label = new Label(Utils.ID_COLUMN_INDEX, Utils.LANGUAGE_ROW, "ID",
                    mWritableCellFormat);
            sheet.addCell(label);
            HashMap<String, ConfigItem> languages = Config.getInstance().getLanguages();
            Set<String> mKeys = languages.keySet();
            for (String key : mKeys) {
                ConfigItem item = languages.get(key);
                label = new Label(item.getIndex(), Utils.LANGUAGE_ROW,
                        item.getValue(), mWritableCellFormat);
                sheet.addCell(label);
            }
        } catch (RowsExceededException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }
    }

    /*
     * getCellFormat
     * @param type
     * @return null or WritableCellFormat
     */
    private WritableCellFormat getCellFormat(CellType type) {
        WritableCellFormat mWritableCellFormat = null;
        try {
            switch (type) {
                case TITLE:
                    WritableFont wfcTitle = new WritableFont(WritableFont.ARIAL, 10,
                            WritableFont.NO_BOLD,
                            false, UnderlineStyle.NO_UNDERLINE, Colour.BLUE);
                    mWritableCellFormat = new WritableCellFormat(wfcTitle);
                    mWritableCellFormat.setAlignment(Alignment.LEFT);
                    mWritableCellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
                    break;
                case APP_NAME:
                    WritableFont wfcApp = new WritableFont(WritableFont.ARIAL, 10,
                            WritableFont.NO_BOLD,
                            false, UnderlineStyle.NO_UNDERLINE, Colour.BLUE);
                    mWritableCellFormat = new WritableCellFormat(wfcApp);
                    mWritableCellFormat.setAlignment(Alignment.LEFT);
                    mWritableCellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
                    break;
                case SPECIAL:
                    WritableFont wfcSpecial = new WritableFont(WritableFont.ARIAL, 10,
                            WritableFont.NO_BOLD,
                            false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK);
                    mWritableCellFormat = new WritableCellFormat(wfcSpecial);
                    mWritableCellFormat.setAlignment(Alignment.LEFT);
                    mWritableCellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
                    mWritableCellFormat.setBackground(Colour.LIME);
                    break;
                case NORMAL:
                    WritableFont wfc = new WritableFont(WritableFont.ARIAL, 10,
                            WritableFont.NO_BOLD,
                            false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK);
                    mWritableCellFormat = new WritableCellFormat(wfc);
                    mWritableCellFormat.setAlignment(Alignment.LEFT);
                    mWritableCellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
                    break;
            }
        } catch (WriteException e) {
            e.printStackTrace();
            WritableFont wfc = new WritableFont(WritableFont.ARIAL, 10,
                    WritableFont.NO_BOLD,
                    false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK);
            mWritableCellFormat = new WritableCellFormat(wfc);
        }
        return mWritableCellFormat;
    }
}
