import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.format.Alignment;
import jxl.format.VerticalAlignment;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.WritableFont;
import jxl.write.WritableCellFormat;
import jxl.write.biff.RowsExceededException;

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
public class AppDir {
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

    private static final int CELL_NORMAL = 0;
    private static final int CELL_TITLE = 1;
    private static final int CELL_APP_NAME = 2;
    private static final int CELL_SPECIAL = 3;

    public AppDir(File file) {
        if (!file.isDirectory()) {
            Utils.loge("dir :" + file.getAbsolutePath() + " is not dir!");
            return;
        }
        mPath = file.getAbsolutePath();
        mAppName = file.getName();
        mStringFiles = new HashMap<String, StringsFile>();
    }

    public AppDir(String name, String path) {
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
                Utils.logd("parserAppDir mResDir :  " + mResDir.getAbsolutePath());
                File[] resFiles = mResDir.listFiles();
                for (int i = 0; i < resFiles.length; i++) {
                    doParserStringDir(resFiles[i]);
                }
            }
        }
        if (mStringFiles.size() != 0) {
            writeToExcel(this, Utils.XLS_PATH);
        }
    }

    public void doParserStringDir(File file) {
        String dirPath = file.getAbsolutePath();
        if (dirPath.contains(Utils.VALUES)) {
            int size = Utils.mStringFileNames.size(); 
            for (int i=0; i<size; i++) {
                File stringFile = new File(file.getAbsolutePath(), Utils.mStringFileNames.get(i));
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
            if (!mStringFiles.containsKey(key)){
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

    public void writeToExcel(AppDir appStr, String savePath) {
        WritableWorkbook wb = null;
        WritableSheet sheet = null;
        Workbook rwb = null;
        File xlsFile = new File(savePath, Utils.NEW_XLS_NAME);
        Utils.logd("write package " + appStr.mAppName + " to excel path : "
                + xlsFile.getAbsolutePath());
        try {
            int startRow = 0;
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
            Utils.logd("start rows is : " + (rows + 1));
            startRow = rows + 1;
            StringsFile engFile = mStringFiles.get(Utils.VALUES);
            HashMap<String, Integer> idsMap = new HashMap<String, Integer>();

            // ssheet = engFile.writeToExcel(sheet, rows, false, idsMap);

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
            Utils.logd("end rows is : " + sheet.getRows());
            int endRowIndex = sheet.getRows()-1;
            sheet.mergeCells(0, startRow, 0, endRowIndex);
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
        if (Utils.mLanguageIndexes.get(strFile.getDirName()) == null) {
            return;
        }
        try {
            for (int i = 0; i < size; i++) {
                String[] ids = mStrs.get(i).getAllIds();
                String[] values = mStrs.get(i).getAllValues();
                for (int j = 0; j < ids.length; j++) {
                    if (isEnglish) {
                        if (i == 0 && j == 0) {
                            WritableCellFormat mWritableCellFormat = getCellFormat(CELL_APP_NAME);
                            Label label = new Label(Utils.APP_NAME_COLUMN_INDEX, currenRow,
                                    strFile.getAppName(), mWritableCellFormat);
                            sheet.addCell(label);
                        }
                        WritableCellFormat mWritableCellFormat = getCellFormat(CELL_NORMAL);
                        Label label = new Label(Utils.ID_COLUMN_INDEX, currenRow, ids[j],
                                mWritableCellFormat);
                        sheet.addCell(label);
                        if(Utils.isContainSpecialCharacter(values[j])){
                            mWritableCellFormat = getCellFormat(CELL_SPECIAL);
                        }
                        label = new Label(Utils.mLanguageIndexes.get(strFile.getDirName()),
                                currenRow, values[j],mWritableCellFormat);
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
                        //Utils.logd("string : " + values[j] + " isContainSpecialCharacter :" + Utils.isContainSpecialCharacter(values[j]));
                        if(Utils.isContainSpecialCharacter(values[j])){
                            mWritableCellFormat = getCellFormat(CELL_SPECIAL);
                        }else{
                            mWritableCellFormat = getCellFormat(CELL_NORMAL);
                        }
                        Label label = new Label(Utils.mLanguageIndexes.get(strFile.getDirName()),
                                idsMap.get(ids[j]), values[j],
                                mWritableCellFormat);
                        // Label label = new
                        // Label(Utils.mLanguageIndexes.get(strFile.getDirName()),
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
            WritableCellFormat mWritableCellFormat = getCellFormat(CELL_TITLE);
            Label label = new Label(Utils.APP_NAME_COLUMN_INDEX, Utils.LANGUAGE_ROW, "app name",
                    mWritableCellFormat);
            sheet.addCell(label);
            label = new Label(Utils.ID_COLUMN_INDEX, Utils.LANGUAGE_ROW, "ID",
                    mWritableCellFormat);
            sheet.addCell(label);
            Set<String> mKeys = Utils.mLanguages.keySet();
            for (String key : mKeys) {
                label = new Label(Utils.mLanguageIndexes.get(key), Utils.LANGUAGE_ROW,
                        Utils.mLanguages.get(key), mWritableCellFormat);
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
    private WritableCellFormat getCellFormat(int type) {
        WritableCellFormat mWritableCellFormat = null;
        try {
            switch (type) {
                case CELL_TITLE:
                    WritableFont wfcTitle = new WritableFont(WritableFont.ARIAL, 10,
                            WritableFont.NO_BOLD,
                            false, UnderlineStyle.NO_UNDERLINE, Colour.BLUE);
                    mWritableCellFormat = new WritableCellFormat(wfcTitle);
                    mWritableCellFormat.setAlignment(Alignment.LEFT);
                    mWritableCellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
                    break;
                case CELL_APP_NAME:
                    WritableFont wfcApp = new WritableFont(WritableFont.ARIAL, 10,
                            WritableFont.NO_BOLD,
                            false, UnderlineStyle.NO_UNDERLINE, Colour.BLUE);
                    mWritableCellFormat = new WritableCellFormat(wfcApp);
                    mWritableCellFormat.setAlignment(Alignment.LEFT);
                    mWritableCellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
                    break;
                case CELL_SPECIAL:
                    WritableFont wfcSpecial = new WritableFont(WritableFont.ARIAL, 10,
                            WritableFont.NO_BOLD,
                            false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK);
                    mWritableCellFormat = new WritableCellFormat(wfcSpecial);
                    mWritableCellFormat.setAlignment(Alignment.LEFT);
                    mWritableCellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
                    mWritableCellFormat.setBackground(Colour.LIME);
                    break;
                default:
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
