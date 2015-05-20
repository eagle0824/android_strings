
package main.java.com.eagle;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import main.java.com.eagle.config.Config;
import main.java.com.eagle.config.ConfigItem;
import main.java.com.eagle.mode.App;
import main.java.com.eagle.mode.App.CellType;
import main.java.com.eagle.mode.ExcelApp;
import main.java.com.eagle.mode.ExcelRecord;
import main.java.com.eagle.mode.StringObj;
import main.java.com.eagle.mode.StringObj.FormatResult;
import main.java.com.eagle.mode.StringsFile;
import main.java.com.eagle.mode.StringsFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class FileHelper {
    private ArrayList<ExcelApp> mApps;
    private HashMap<String, Integer> mExcelTitleLanguages;

    private static FileHelper mInstatnce;

    public static FileHelper getInstance() {
        if (mInstatnce == null) {
            mInstatnce = new FileHelper();
        }
        return mInstatnce;
    }

    private FileHelper() {
        mApps = new ArrayList<ExcelApp>();
        mExcelTitleLanguages = new HashMap<String, Integer>();
        Utils.initDir(Config.getInstance().getCommand().getOutputPath());
    }

    public void readXlsFile(File xlsFile) {
        parserXlsFile(xlsFile, false, -1, -1);
    }

    public void createXmlsFromXlsFile(File xlsFile) {
        parserXlsFile(xlsFile, true, -1, -1);
    }

    public void createXmlByApp(ExcelApp excelApp, App app) {
        String outPutDir = Config.getInstance().getCommand().getOutputPath();
        String appName = excelApp.getAppName();
        Utils.loge("write " + appName + " to dir " + outPutDir + " count : "
                + excelApp.getRecordCount());
        StringsFolder engFolder = app.getStringsFoldersHashMap().get(Utils.VALUES);
        ArrayList<StringsFile> stringsFiles = engFolder.getStringsFiles();
        if (stringsFiles.size() == 0) {
            Utils.loge("can't find strings files on app : " + app.getPath());
            return;
        }
        File appFile = new File(outPutDir, appName);
        Utils.initDir(appFile);
        Set<String> languages = mExcelTitleLanguages.keySet();
        for (String language : languages) {
            int strIndex = mExcelTitleLanguages.get(language);
            File fileValue = new File(appFile, Utils.VALUES + "-" + language);
            if (language.equals("en")) {
                fileValue = new File(appFile, Utils.VALUES);
            }
            Utils.initDir(fileValue);
            HashMap<String, ArrayList<ExcelRecord>> allRecores = excelApp.getStringsRecords();
            Set<String> files = allRecores.keySet();
            for (String fileName : files) {
                StringsFile stringsFile = engFolder.getStringFileByName(fileName);
                if (stringsFile != null) {
                    ArrayList<StringObj> mStrs = stringsFile.getAllStrs();
                    int size = mStrs.size();
                    if (size == 0) {
                        continue;
                    }
                    try {
                        OutputStreamWriter out =
                                new OutputStreamWriter(new FileOutputStream(new File(fileValue,
                                        fileName)), Utils.UTF_8);
                        File errorFile = new File(fileValue, Utils.ERROR_FILE);
                        OutputStreamWriter error =
                                new OutputStreamWriter(new FileOutputStream(errorFile), Utils.UTF_8);
                        out.write(Utils.STRING_HEAD);
                        boolean hasError = false;
                        for (int j = 0; j < size; j++) {
                            StringObj strObj = mStrs.get(j);
                            String[] ids = strObj.getAllIds();
                            // Utils.logd("ids "+ Arrays.toString(ids));
                            String[] values = null;
                            if (ids.length == 1) {
                                ExcelRecord record = excelApp.getRecordById(fileName, ids[0]);
                                if (record != null && record.getItemByIndex(strIndex) != null) {
                                    values = new String[] {
                                            record.getItemByIndex(strIndex)
                                    };
                                }
                            } else if (ids.length > 1) {
                                ExcelRecord[] records = excelApp.getRecordById(fileName, ids);
                                if (records != null) {
                                    int recordLen = records.length;
                                    values = new String[recordLen];
                                    for (int m = 0; m < recordLen; m++) {
                                        if (records[m] != null) {
                                            values[m] = records[m].getItemByIndex(strIndex);
                                        }
                                    }
                                }
                            } else {
                                continue;
                            }
                            FormatResult result = strObj.getXMLFormatStr(values);
                            if (result.mIsError) {
                                error.write(result.mXmlString);
                                hasError = true;
                            } else {
                                // Utils.logd("" + result.mXmlString);
                                out.write(result.mXmlString);
                            }
                        }
                        out.write(Utils.STRING_END);
                        out.flush();
                        out.close();
                        error.flush();
                        error.close();
                        if (!hasError && errorFile.exists()) {
                            errorFile.delete();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void writeAppToExcel(App app) {
        HashMap<String, StringsFolder> stringsFolders = app.getStringsFoldersHashMap();
        if (stringsFolders.size() == 0) {
            return;
        }
        WritableWorkbook wb = null;
        WritableSheet sheet = null;
        Workbook rwb = null;
        String savePath = Config.getInstance().getCommand().getOutputPath();
        File xlsFile = new File(savePath, Utils.NEW_XLS_NAME);
        String appName = app.getName();
        Utils.logd("write package " + appName + " to excel path : "
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
            startRowIndex = sheet.getRows();
            if (startRowIndex == Utils.LANGUAGE_ROW) {
                writeExcelTitle(sheet);
            }
            Utils.logd("app" + app.getName() + " rows : " + sheet.getRows() + " start : "
                    + Utils.ROW_START_INDEX);

            startRowIndex = sheet.getRows() > Utils.ROW_START_INDEX ? sheet.getRows()
                    : Utils.ROW_START_INDEX;
            startRowIndex++;

            // wirte app name
            WritableCellFormat writableCellFormat = Utils.getInstance().getCellFormat(
                    CellType.APP_NAME);
            Label label = new Label(Utils.APP_NAME_COLUMN_INDEX, startRowIndex,
                    appName, writableCellFormat);
            sheet.addCell(label);

            // init id by values strings files
            HashMap<String, HashMap<String, Integer>> idsMap = new HashMap<String, HashMap<String, Integer>>();
            StringsFolder engFolder = stringsFolders.get(Utils.VALUES);
            writeStringsFolderExcel(engFolder, sheet, idsMap);

            Set<String> keys = stringsFolders.keySet();
            for (String folderName : keys) {
                if (!folderName.equals(Utils.VALUES)) {
                    writeStringsFolderExcel(stringsFolders.get(folderName), sheet, idsMap);
                }
            }
            int endRowIndex = sheet.getRows() - 1;
            Utils.logd("start rows : " + startRowIndex + " end rows : " + endRowIndex);
            if (endRowIndex > startRowIndex) {
                sheet.mergeCells(Utils.APP_NAME_COLUMN_INDEX, startRowIndex,
                        Utils.APP_NAME_COLUMN_INDEX, endRowIndex);
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
        Utils.logd(String.format("write package %s to excel end!", appName));

    }

    public void writeStringsFolderExcel(StringsFolder folder, WritableSheet sheet,
            HashMap<String, HashMap<String, Integer>> idsMap)
            throws RowsExceededException,
            WriteException {
        HashMap<String, ConfigItem> languages = Config.getInstance().getLanguages();
        String folderName = folder.getName();
        boolean isEnglish = folderName.equals(Utils.VALUES);
        if (!isEnglish && languages.size() > 0
                && !languages.containsKey(folderName)) {
            return;
        }
        ArrayList<StringsFile> stringsFiles = folder.getStringsFiles();
        int size = stringsFiles.size();
        for (int i = 0; i < size; i++) {
            String fileName = stringsFiles.get(i).getFileName();
            if (isEnglish) {
                idsMap.put(fileName, new HashMap<String, Integer>());
            }
            int offset = i == 0 ? 0 : 2;
            int startRowIndex = sheet.getRows() - 1 + offset;
            writeStringsFileToExcel(stringsFiles.get(i), sheet, idsMap.get(fileName), offset);
            int endRowIndex = sheet.getRows() - 1;
            if (endRowIndex > startRowIndex) {
                sheet.mergeCells(Utils.STRING_FILE_NAME_INDEX, startRowIndex,
                        Utils.STRING_FILE_NAME_INDEX, endRowIndex);
            }
        }

    }

    public void writeStringsFileToExcel(StringsFile stringsFile, WritableSheet sheet,
            HashMap<String, Integer> idsMap, int rowOffset)
            throws RowsExceededException,
            WriteException {

        String folderName = stringsFile.getFolderName();
        boolean isEnglish = Utils.VALUES.equals(folderName);
        HashMap<String, ConfigItem> languages = Config.getInstance().getLanguages();
        if (!languages.containsKey(folderName)) {
            return;
        }
        ConfigItem item = languages.get(folderName);

        int currenRow = sheet.getRows() + rowOffset - 1;
        ArrayList<StringObj> mStrs = stringsFile.getAllStrs();
        int size = mStrs.size();

        for (int i = 0; i < size; i++) {
            String[] ids = mStrs.get(i).getAllIds();
            String[] values = mStrs.get(i).getAllValues();
            for (int j = 0; j < ids.length; j++) {
                if (isEnglish) {
                    if (i == 0 && j == 0) {
                        WritableCellFormat writableCellFormat = Utils.getInstance().getCellFormat(
                                CellType.APP_NAME);
                        writableCellFormat = Utils.getInstance().getCellFormat(CellType.NORMAL);
                        Label label = new Label(Utils.STRING_FILE_NAME_INDEX, currenRow,
                                stringsFile.getFileName(), writableCellFormat);
                        sheet.addCell(label);
                    }
                    WritableCellFormat writableCellFormat = Utils.getInstance().getCellFormat(
                            CellType.NORMAL);
                    Label label = new Label(Utils.ID_COLUMN_INDEX, currenRow, ids[j],
                            writableCellFormat);
                    sheet.addCell(label);
                    if (Utils.isContainSpecialCharacter(values[j])) {
                        writableCellFormat = Utils.getInstance().getCellFormat(CellType.SPECIAL);
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
                        writableCellFormat = Utils.getInstance().getCellFormat(CellType.SPECIAL);
                    } else {
                        writableCellFormat = Utils.getInstance().getCellFormat(CellType.NORMAL);
                    }
                    Label label = new Label(item.getIndex(),
                            idsMap.get(ids[j]), values[j],
                            writableCellFormat);
                    sheet.addCell(label);
                }
            }
        }
    }

    public void writeSingalStringsFileToExcel(StringsFile stringsFile) {
        WritableWorkbook wb = null;
        WritableSheet sheet = null;
        Workbook rwb = null;
        String savePath = Config.getInstance().getCommand().getOutputPath();
        String xlsFileName = stringsFile.getFileName().replace(Utils.SURFIX_XML, Utils.SURFIX_XLS);
        File xlsFile = new File(savePath, xlsFileName);
        Utils.logd("write package StringFile " + " to excel path : " + xlsFile.getAbsolutePath());
        try {
            if (!xlsFile.exists()) {
                wb = Workbook.createWorkbook(xlsFile);
                sheet = wb.createSheet(Utils.SHEET0_NAME, Utils.SHEET0_INDEX);
            } else {
                rwb = Workbook.getWorkbook(xlsFile);
                wb = Workbook.createWorkbook(xlsFile, rwb);
                sheet = wb.getSheet(Utils.SHEET0_INDEX);
            }
            int currenRow = sheet.getRows();
            Label label = new Label(0, currenRow, stringsFile.getPath());
            sheet.addCell(label);
            label = new Label(Utils.ID_COLUMN_INDEX, currenRow, "ID");
            sheet.addCell(label);
            label = new Label(Utils.ID_COLUMN_INDEX + 1, currenRow, "value");
            sheet.addCell(label);
            currenRow += 1;
            ArrayList<StringObj> mStrs = stringsFile.getAllStrs();
            for (int i = 0; i < mStrs.size(); i++) {
                String[] ids = mStrs.get(i).getAllIds();
                String[] values = mStrs.get(i).getAllValues();
                for (int j = 0; j < ids.length; j++) {
                    label = new Label(Utils.ID_COLUMN_INDEX, currenRow, ids[j]);
                    sheet.addCell(label);
                    label = new Label(3, currenRow, values[j]);
                    if (ids[j].startsWith(Utils.SURFIX_ARRAY)
                            || ids[j].startsWith(Utils.SURFIX_PLURALS)) {
                        ids[j] = ids[j] + j;
                    }
                    sheet.addCell(label);
                    currenRow += 1;
                }
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

    private void writeExcelAppToXml(ExcelApp app) {
        String outPutDir = Config.getInstance().getCommand().getOutputPath();
        Utils.loge("write " + app.getAppName() + " to dir " + outPutDir + " count : "
                + app.getRecordCount());
        File appFile = new File(outPutDir, app.getAppName());
        Utils.initDir(appFile);
        Set<String> keySet = mExcelTitleLanguages.keySet();
        for (String key : keySet) {
            int strIndex = mExcelTitleLanguages.get(key);
            // Utils.logd("language is : " + language);
            File dirFolder = null;
            if (key.equals("en")) {
                dirFolder = new File(appFile, Utils.VALUES);
            } else {
                dirFolder = new File(appFile, Utils.VALUES + "-" + key);
            }
            Utils.initDir(dirFolder);
            HashMap<String, ArrayList<ExcelRecord>> mRecords = app.getStringsRecords();
            for (String stringsFileName : mRecords.keySet()) {
                createStringsFile(strIndex, dirFolder, stringsFileName,
                        mRecords.get(stringsFileName));
            }
        }
    }

    private void createStringsFile(int languageId, File parent, String stringFileName,
            ArrayList<ExcelRecord> records) {
        Utils.logd("createstringFile : " + stringFileName + " at dir : " + parent.getAbsolutePath());
        StringBuilder sb = new StringBuilder();
        try {
            OutputStreamWriter out =
                    new OutputStreamWriter(new FileOutputStream(new File(parent,
                            stringFileName)), Utils.UTF_8);
            File errorFile = new File(parent, Utils.ERROR_FILE);
            OutputStreamWriter error =
                    new OutputStreamWriter(new FileOutputStream(errorFile), Utils.UTF_8);
            out.write(Utils.STRING_HEAD);
            boolean hasError = false;
            String currentArrayName = "";
            String currentPlurals = "";
            for (ExcelRecord record : records) {
                String name = record.getItemByIndex(Utils.ID_COLUMN_INDEX);
                String value = record.getItemByIndex(languageId);
                if (name == null || value == null || name == "" || value == "") {
                    sb.append("string name is : ")
                            .append(name)
                            .append("        value is : ")
                            .append(value)
                            .append("\n");
                    error.write(sb.toString());
                    sb.delete(0, sb.length());
                    hasError = true;
                } else {
                    if (name.startsWith(Utils.SURFIX_ARRAY)) {// parser
                                                              // array
                        if (!currentPlurals.equals("")) {
                            sb.append(Utils.PLURALS_END);
                            currentPlurals = "";
                        }
                        String arrayName = name.replace(Utils.SURFIX_ARRAY, "");
                        if (currentArrayName == "") {
                            currentArrayName = arrayName;
                            sb.append(Utils.STRING_ARRAY_HEAD_PREFIX)
                                    .append(currentArrayName)
                                    .append(Utils.STRING_ARRAY_HEAD_SURFIX);
                        } else if (!currentArrayName.equals(arrayName)) {
                            sb.append(Utils.STRING_ARRAY_END);
                            currentArrayName = arrayName;
                            sb.append(Utils.STRING_ARRAY_HEAD_PREFIX)
                                    .append(currentArrayName)
                                    .append(Utils.STRING_ARRAY_HEAD_SURFIX);
                        }
                        sb.append(Utils.ITEM_PREFIX)
                                .append(value)
                                .append(Utils.ITEM_SUFFIX);
                    } else if (name.startsWith(Utils.SURFIX_PLURALS)) {// parser
                                                                       // plurals
                        if (!currentArrayName.equals("")) {
                            sb.append(Utils.STRING_ARRAY_END);
                            currentArrayName = "";
                        }
                        String pluralsName = name.replace(Utils.SURFIX_PLURALS, "");
                        String[] items = pluralsName.split("\\|");
                        pluralsName = items[0];
                        String quantity = "";
                        if (items.length >= 2) {
                            quantity = items[1];
                        }
                        if (currentPlurals == "") {
                            currentPlurals = pluralsName;
                            sb.append(Utils.PLURALS_HEAD_PREFIX)
                                    .append(pluralsName)
                                    .append(Utils.PLURALS_HEAD_SURFIX);
                        } else if (!currentPlurals.equals(pluralsName)) {
                            sb.append(Utils.PLURALS_END);
                            currentPlurals = pluralsName;
                            sb.append(Utils.PLURALS_HEAD_PREFIX)
                                    .append(pluralsName)
                                    .append(Utils.PLURALS_HEAD_SURFIX);
                        }
                        sb.append(Utils.PLURALS_ITEM_PREFIX)
                                .append(quantity)
                                .append(Utils.PLURALS_MIDDLE_PREFIX)
                                .append(value)
                                .append(Utils.PLURALS_ITEM_SUFFIX);
                    } else {// parser normal string
                        if (!currentArrayName.equals("")) {
                            sb.append(Utils.STRING_ARRAY_END);
                            currentArrayName = "";
                        }
                        if (!currentPlurals.equals("")) {
                            sb.append(Utils.PLURALS_END);
                            currentPlurals = "";
                        }
                        sb.append(Utils.STRINT_RECORD_PREFIX)
                                .append(name)
                                .append(Utils.STRINT_RECORD_MIDDLE)
                                .append(value)
                                .append(Utils.STRINT_RECORD_SURFIX);
                    }
                    out.write(sb.toString());
                    sb.delete(0, sb.length());
                }
            }
            out.write(Utils.STRING_END);
            out.flush();
            out.close();
            error.flush();
            error.close();
            if (!hasError && errorFile.exists()) {
                errorFile.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parserXlsFile(File xlsFile, boolean createXml, int start, int end) {
        Workbook wb = null;
        try {
            WorkbookSettings workbookSettings = new WorkbookSettings();
            workbookSettings.setEncoding(Utils.ISO_8859_1);
            wb = Workbook.getWorkbook(xlsFile, workbookSettings);
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (wb != null) {
            int emptyRowCount = 0;
            Sheet[] sheets = wb.getSheets();
            int len = sheets.length;// sheet count
            Utils.logd("sheet size : " + len);
            for (int i = 0; i < len; i++) {
                Sheet sheet = sheets[i];// get sheet i
                int rows = sheet.getRows();
                int rowStart = start > Utils.ROW_START_INDEX ? start : Utils.ROW_START_INDEX;
                if (rows < 1) {
                    Utils.loge("row count is zero !");
                    return;
                }
                int rowEnd = rows;
                if (end > 1) {
                    rowEnd = Math.min(rows, end);
                }
                Utils.logd("sheet : " + i + " total rows is : " + rows + " read row form : "
                        + rowStart
                        + " to : " + rowEnd);
                readExcelTitleLine(sheet);
                ExcelApp app = null;
                for (int j = rowStart; j < rowEnd; j++) {
                    ExcelRecord record = new ExcelRecord(j + 1);
                    Cell[] cells = sheet.getRow(j);
                    if (cells != null && cells.length > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (int k = 0; k < cells.length; k++) {
                            String content = cells[k].getContents();
                            content = Utils.removeBlankAndQutation(content);
                            if (k == Utils.ID_COLUMN_INDEX && content.trim() == "") {
                                record = null;
                                emptyRowCount++;
                                if (app != null) {
                                    mApps.add(app);
                                    if (createXml) {
                                        writeExcelAppToXml(app);
                                    }
                                    app = null;
                                }
                                break;
                            }
                            if (k == Utils.APP_NAME_COLUMN_INDEX && content.trim() != "") {
                                if (app != null) {
                                    mApps.add(app);
                                    if (createXml) {
                                        writeExcelAppToXml(app);
                                    }
                                    app = null;
                                    // Utils.logd(app.toString());
                                }
                                app = new ExcelApp(getAppName(content));
                                // Utils.logd("reading " + app.getAppName());
                            }
                            if (k == Utils.STRING_FILE_NAME_INDEX && content.trim() != "") {
                                String fileName = content.trim();
                                if (app != null) {
                                    app.addStringsFileName(fileName);
                                }
                            }
                            record.addItem(content);
                            sb.append(content).append("|");
                        }
                        if (record != null) {
                            // Utils.logd(record.toString());
                        }
                        if (app != null && record != null) {
                            app.add(record);
                        }
                    }
                    if (j == rowEnd - 1 && app != null) {
                        mApps.add(app);
                        if (createXml) {
                            writeExcelAppToXml(app);
                        }
                    }
                }
                int totalLine = (start == Utils.ROW_START_INDEX ? (rowEnd - rowStart) : (rowEnd
                        - rowStart + Utils.ROW_START_INDEX));
                Utils.logd("total line is : " + totalLine + " allRecord count : "
                        + getAllReordCount()
                        + " empty count is : " + emptyRowCount);
            }
            wb.close();
        }
        // dumpApps();
    }

    private int getAllReordCount() {
        int count = 0;
        int len = mApps.size();
        for (int i = 0; i < len; i++) {
            count += mApps.get(i).getRecordCount();
        }
        return count;
    }

    private void dumpLanguages() {
        Set<String> keysSet = mExcelTitleLanguages.keySet();
        for (String key : keysSet) {
            Utils.logd("mLauguages " + key +
                    " index is :"
                    + mExcelTitleLanguages.get(key));
        }
    }

    public void dumpApps() {
        int len = mApps.size();
        for (int i = 0; i < len; i++) {
            Utils.logd("Apps total count is : " + len + "\n" +
                    mApps.get(i).toString());
        }
    }

    private String getAppName(String content) {
        String[] langs = content.split("\\|");
        if (langs.length == 2 && langs[1] != "") {
            return langs[1];
        } else {
            return content;
        }
    }

    private String getLanguageName(String content) {
        String[] langs = content.split("\\|");
        if (langs.length == 2 && langs[1] != "") {
            return langs[1];
        } else {
            Utils.loge("can't got language name on title, value is  " + content);
            return "";
        }
    }

    private void writeExcelTitle(WritableSheet sheet) throws RowsExceededException, WriteException {
        WritableCellFormat writableCellFormat = Utils.getInstance().getCellFormat(CellType.NORMAL);
        Label label = new Label(Utils.APP_NAME_COLUMN_INDEX, Utils.LANGUAGE_ROW, "app name",
                writableCellFormat);
        sheet.addCell(label);

        label = new Label(Utils.STRING_FILE_NAME_INDEX, Utils.LANGUAGE_ROW, "file name",
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

    private void readExcelTitleLine(Sheet sheet) {
        Cell[] cells = sheet.getRow(Utils.LANGUAGE_ROW);
        if (cells != null && cells.length > 0) {
            for (int k = 0; k < cells.length; k++) {
                String content = cells[k].getContents();
                if (k > Utils.ID_COLUMN_INDEX && content.trim() != "") {
                    content = getLanguageName(content);
                    if (content != "") {
                        mExcelTitleLanguages.put(content, k);
                    }
                }
            }
        } else {
            Utils.loge("read Languages failed!");
        }
        // dumpLanguages()
    }

    public ExcelApp getExcelAppByName(String name) {
        int size = mApps.size();
        for (int i = 0; i < size; i++) {
            if (mApps.get(i).getAppName().equals(name)) {
                return mApps.get(i);
            }
        }
        return null;
    }

}
