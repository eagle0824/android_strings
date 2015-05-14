
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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
    private HashMap<String, StringsFolder> mStringsFolders;

    /*
     * mPath is The appDir path
     */
    private String mPath;

    private String mName;

    public enum CellType {
        NORMAL, TITLE, APP_NAME, SPECIAL
    }

    public App(String appDir) {
        mPath = appDir;
        mName = Utils.getAppName(mPath);
    }

    public App(String name, String path) {
        mPath = path;
        mName = Utils.getAppName(mPath);
        // mStringsContainers = new HashMap<String, ArrayList<StringsFile>>();
    }

    public void parser() {
        Utils.loge(String.format("app : %s path : %s", mName, mPath));
        Collection<StringsFolder> stringsFolders = getStringsFolders();
        for (StringsFolder stringsFolder : stringsFolders) {
            stringsFolder.parser();
        }
        dumpStringsFiles();
        // if (mStringsFolders.size() != 0) {
        // writeToExcel();
        // }
    }

    public Collection<StringsFolder> getStringsFolders() {
        if (mStringsFolders == null) {
            mStringsFolders = findAppStringsFolders(mPath);
        }
        return mStringsFolders.values();
    }

    public ArrayList<String> findAppStringsFiles(String appDir, boolean onlyEnString) {
        ArrayList<String> stringsFiles = new ArrayList<String>();
        Collection<StringsFolder> stringsFolders = getStringsFolders();
        ArrayList<String> defaultStringsNames = Config.getInstance().getStringFiles();
        File stringsFolderFile;
        for (StringsFolder stringsFolder : stringsFolders) {
            stringsFolderFile = new File(stringsFolder.getPath());
            if (onlyEnString) {
                if (!stringsFolderFile.getName().equals(Utils.VALUES)) {
                    continue;
                }
            }
            for (String defaultStringName : defaultStringsNames) {
                File stringsFile = new File(stringsFolder.getPath(), defaultStringName);
                if (stringsFile.exists() && stringsFile.isFile()) {
                    stringsFiles.add(stringsFile.getAbsolutePath());
                }
            }
        }
        return stringsFiles;
    }

    public HashMap<String, StringsFolder> findAppStringsFolders(String appDir) {
        HashMap<String, StringsFolder> stringsFolders = new HashMap<String, StringsFolder>();
        File appFile = new File(appDir);
        File[] resFiles = appFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (dir.isDirectory() && name.equals(Utils.RES)) {
                    return true;
                }
                return false;
            }
        });
        if (resFiles != null && resFiles.length == 1) {
            File[] valuesFiles = resFiles[0].listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (dir.isDirectory() && name.contains(Utils.VALUES)) {
                        return true;
                    }
                    return false;
                }
            });
            if (valuesFiles != null && valuesFiles.length > 0) {
                Set<String> mKeys = Config.getInstance().getLanguages().keySet();
                for (File valuesDir : valuesFiles) {
                    if (mKeys.contains(valuesDir.getName())) {
                        stringsFolders.put(valuesDir.getName(),
                                new StringsFolder(valuesDir.getAbsolutePath(), this));
                    }
                }
            }
        }
        return stringsFolders;
    }

    private void dumpStringsFiles() {
        Set<String> keySet = mStringsFolders.keySet();
        for (String key : keySet) {
            Utils.loge(String.format("key : %s StringsFile path %s ", key,
                    mStringsFolders.get(key).getPath()));
        }
    }

    // public void addStringFile(StringsFile strFile) {
    // String key = strFile.getValuesFolderName();
    // if (key != null && !key.equals("")) {
    // if (!mStringsContainers.containsKey(key)) {
    // mStringsContainers.put(key, n);
    // } else {
    // mStringFiles.get(key).addStrings(strFile.getAllStrs());
    // }
    // }
    // }

    public String getPath() {
        return mPath;
    }

    public String getName() {
        return mName;
    }

    public void writeToExcel() {
        WritableWorkbook wb = null;
        WritableSheet sheet = null;
        Workbook rwb = null;
        String savePath = Config.getInstance().getCommand().getOutputPath();
        File xlsFile = new File(savePath, Utils.NEW_XLS_NAME);
        Utils.logd("write package " + getName() + " to excel path : "
                + xlsFile.getAbsolutePath());
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
            HashMap<String, Integer> idsMap = new HashMap<String, Integer>();
            StringsFolder engFolder = mStringsFolders.get(Utils.VALUES);
            ArrayList<StringsFile> stringsFiles = engFolder.getStringsFiles();
            for (StringsFile stringsFile : stringsFiles) {
                writeToExcel(sheet, rows, true, idsMap, stringsFile);
            }
            Set<String> keys = mStringsFolders.keySet();
            for (String folderName : keys) {
                if (!folderName.equals(Utils.VALUES)) {
                    stringsFiles = mStringsFolders.get(folderName).getStringsFiles();
                    for (StringsFile stringsFile : stringsFiles) {
                        rows = sheet.getRows();
                        writeToExcel(sheet, rows, true, idsMap, stringsFile);
                    }
                }
            }
            int endRowIndex = sheet.getRows() - 1;
            Utils.logd("start rows : " + startRowIndex + " end rows : " + endRowIndex);
            if (endRowIndex > startRowIndex) {
                sheet.mergeCells(Utils.APP_NAME_COLUMN_INDEX, startRowIndex, Utils.APP_NAME_COLUMN_INDEX, endRowIndex);
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
        Utils.logd(String.format("write package %s to excel end!", getName()));
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
                        WritableCellFormat writableCellFormat = getCellFormat(CellType.APP_NAME);
                        Label label = new Label(Utils.APP_NAME_COLUMN_INDEX, currenRow,
                                strFile.getAppName(), writableCellFormat);
                        sheet.addCell(label);
                        writableCellFormat = getCellFormat(CellType.NORMAL);
                        label = new Label(Utils.STRING_FILE_NAME_INDEX, currenRow,
                                strFile.getFileName(), writableCellFormat);
                        sheet.addCell(label);
                    }
                    WritableCellFormat writableCellFormat = getCellFormat(CellType.NORMAL);
                    Label label = new Label(Utils.ID_COLUMN_INDEX, currenRow, ids[j],
                            writableCellFormat);
                    sheet.addCell(label);
                    if (Utils.isContainSpecialCharacter(values[j])) {
                        writableCellFormat = getCellFormat(CellType.SPECIAL);
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
                        writableCellFormat = getCellFormat(CellType.SPECIAL);
                    } else {
                        writableCellFormat = getCellFormat(CellType.NORMAL);
                    }
                    Label label = new Label(item.getIndex(),
                            idsMap.get(ids[j]), values[j],
                            writableCellFormat);
                    sheet.addCell(label);
                }
            }
        }
    }

    private void initExcelTitle(WritableSheet sheet) throws RowsExceededException, WriteException {
        WritableCellFormat writableCellFormat = getCellFormat(CellType.NORMAL);
        Label label = new Label(Utils.APP_NAME_COLUMN_INDEX, Utils.LANGUAGE_ROW, "app name",
                writableCellFormat);
        sheet.addCell(label);

        label = new Label(Utils.STRING_FILE_NAME_INDEX, Utils.LANGUAGE_ROW, "name",
                writableCellFormat);
        sheet.addCell(label);

        label = new Label(Utils.ID_COLUMN_INDEX, Utils.LANGUAGE_ROW, "ID",
                writableCellFormat);
        sheet.addCell(label);

        HashMap<String, ConfigItem> languages = Config.getInstance().getLanguages();
        Set<String> mKeys = languages.keySet();
        for (String key : mKeys) {
            ConfigItem item = languages.get(key);
            label = new Label(item.getIndex(), Utils.LANGUAGE_ROW,
                    item.getValue(), writableCellFormat);
            sheet.addCell(label);
        }
    }

    /*
     * getCellFormat
     * @param type
     * @return null or WritableCellFormat
     */
    public WritableCellFormat getCellFormat(CellType type) {
        WritableCellFormat writableCellFormat = null;
        try {
            switch (type) {
                case TITLE:
                    WritableFont wfcTitle = new WritableFont(WritableFont.ARIAL, 10,
                            WritableFont.NO_BOLD,
                            false, UnderlineStyle.NO_UNDERLINE, Colour.BLUE);
                    writableCellFormat = new WritableCellFormat(wfcTitle);
                    writableCellFormat.setAlignment(Alignment.LEFT);
                    writableCellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
                    break;
                case APP_NAME:
                    WritableFont wfcApp = new WritableFont(WritableFont.ARIAL, 10,
                            WritableFont.NO_BOLD,
                            false, UnderlineStyle.NO_UNDERLINE, Colour.BLUE);
                    writableCellFormat = new WritableCellFormat(wfcApp);
                    writableCellFormat.setAlignment(Alignment.LEFT);
                    writableCellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
                    break;
                case SPECIAL:
                    WritableFont wfcSpecial = new WritableFont(WritableFont.ARIAL, 10,
                            WritableFont.NO_BOLD,
                            false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK);
                    writableCellFormat = new WritableCellFormat(wfcSpecial);
                    writableCellFormat.setAlignment(Alignment.LEFT);
                    writableCellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
                    writableCellFormat.setBackground(Colour.LIME);
                    break;
                case NORMAL:
                    WritableFont wfc = new WritableFont(WritableFont.ARIAL, 10,
                            WritableFont.NO_BOLD,
                            false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK);
                    writableCellFormat = new WritableCellFormat(wfc);
                    writableCellFormat.setAlignment(Alignment.LEFT);
                    writableCellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
                    break;
            }
        } catch (WriteException e) {
            e.printStackTrace();
            WritableFont wfc = new WritableFont(WritableFont.ARIAL, 10,
                    WritableFont.NO_BOLD,
                    false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK);
            writableCellFormat = new WritableCellFormat(wfc);
        }
        return writableCellFormat;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof App) {
            App o = (App) obj;
            return getPath().equals(o.getPath());
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append("name : ").append(getName());
        sBuilder.append("path : ").append(getPath());
        return sBuilder.toString();
    }

}
