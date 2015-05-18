
package main.java.com.eagle;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import main.java.com.eagle.mode.StringObj;
import main.java.com.eagle.mode.StringObj.FormatResult;
import main.java.com.eagle.mode.StringsFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class ExcelHelper {

    private HashMap<String, Integer> mIndex;
    private ArrayList<String> mLanguages;
    private ArrayList<AppExcel> mApps;

    private int start = -1;
    private int end = -1;
    private int emptyRowCount = 0;
    private String mOutputDir;

    public ExcelHelper(String outputDir) {
        mIndex = new HashMap<String, Integer>();
        mLanguages = new ArrayList<String>();
        mApps = new ArrayList<AppExcel>();
        mOutputDir = outputDir;
        Utils.initDir(outputDir);
    }

    public void setRange(int start, int end) {
        Utils.loge("setRange start : " + start + " end : " + end);
        this.start = start;
        this.end = end;
    }

    public void createXmlsFromXlsFile(File xlsFile) {
        Workbook wb = null;
        try {
            WorkbookSettings workbookSettings = new WorkbookSettings();
            workbookSettings.setEncoding(Utils.ISO_8859_1);
            wb = Workbook.getWorkbook(xlsFile, workbookSettings);
            // wb = Workbook.getWorkbook(xlsFile);
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (wb != null) {
            emptyRowCount = 0;
            Sheet[] sheets = wb.getSheets();
            int len = sheets.length;// sheet count
            Utils.logd("sheet size : " + len);
            for (int i = 0; i < len; i++) {
                Sheet sheet = sheets[i];// get sheet i
                int rows = sheet.getRows();
                AppExcel app = null;
                int rowStart = start > 1 ? start : 1;
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
                if (rowStart != 1) {
                    readLanguages(sheet);
                }
                for (int j = rowStart - 1; j < rowEnd; j++) {
                    if (j == 0) {
                        readLanguages(sheet);
                        continue;
                    }
                    // every record
                    Record record = new Record(j + 1);
                    Cell[] cells = sheet.getRow(j);
                    if (cells != null && cells.length > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (int k = 0; k < cells.length; k++) {
                            String content = cells[k].getContents();
                            content = Utils.removeBlankAndQutation(content);
                            switch (k) {
                                case Utils.ID_COLUMN_INDEX: {
                                    Utils.logd("id : " + content.trim());
                                }
                                    break;
                                case Utils.APP_NAME_COLUMN_INDEX: {
                                    Utils.logd("app name : " + content.trim());

                                }
                                    break;
                                case Utils.STRING_FILE_NAME_INDEX: {
                                    Utils.logd("string name : " + content.trim());
                                }
                                    break;
                                default:
                                    break;
                            }
                            if (k == Utils.ID_COLUMN_INDEX && content.trim() == "") {
                                record = null;
                                emptyRowCount++;
                                if (app != null) {
                                    app.writeToFile();
                                    mApps.add(app);
                                    app = null;
                                }
                                break;
                            }
                            if (k == Utils.APP_NAME_COLUMN_INDEX && content.trim() != "") {
                                if (app != null) {
                                    app.writeToFile();
                                    mApps.add(app);
                                    app = null;
                                    // Utils.logd(app.toString());
                                }
                                app = new AppExcel(getAppName(content));
                                // Utils.logd("reading " + app.getAppName());
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
                        app.writeToFile();
                        mApps.add(app);
                    }
                }
                int totalLine = (start == 1 ? (rowEnd - rowStart) : (rowEnd - rowStart + 1));
                Utils.logd("total line is : " + totalLine + " allRecord count : "
                        + getAllReordCount()
                        + " empty count is : " + emptyRowCount);
            }
            wb.close();
        }
        // dumpApps();
    }

    public void createXmlFromXlsFile(File xlsFile) {
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
            emptyRowCount = 0;
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
                readLanguages(sheet);

                AppExcel app = null;
                for (int j = rowStart; j < rowEnd; j++) {
                    Record record = new Record(j + 1);
                    Cell[] cells = sheet.getRow(j);
                    if (cells != null && cells.length > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (int k = 0; k < cells.length; k++) {
                            String content = cells[k].getContents();
                            content = Utils.removeBlankAndQutation(content);
                            // switch (k) {
                            // case Utils.ID_COLUMN_INDEX: {
                            // Utils.logd("id : " + content.trim());
                            // }
                            // break;
                            // case Utils.APP_NAME_COLUMN_INDEX: {
                            // Utils.logd("app name : " + content.trim());
                            //
                            // }
                            // break;
                            // case Utils.STRING_FILE_NAME_INDEX: {
                            // Utils.logd("string name : " + content.trim());
                            // }
                            // break;
                            // default:
                            // break;
                            // }
                            if (k == Utils.ID_COLUMN_INDEX && content.trim() == "") {
                                record = null;
                                emptyRowCount++;
                                if (app != null) {
                                    app.writeToFile();
                                    mApps.add(app);
                                    app = null;
                                }
                                break;
                            }
                            if (k == Utils.APP_NAME_COLUMN_INDEX && content.trim() != "") {
                                if (app != null) {
                                    app.writeToFile();
                                    mApps.add(app);
                                    app = null;
                                    // Utils.logd(app.toString());
                                }
                                app = new AppExcel(getAppName(content));
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
                            //Utils.logd(record.toString());
                        }
                        if (app != null && record != null) {
                            app.add(record);
                        }
                    }
                    if (j == rowEnd - 1 && app != null) {
                        app.writeToFile();
                        mApps.add(app);
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

    public void createXmlByStringsFile(StringsFile enStringsFile) {
        String name = enStringsFile.getAppName();
        ArrayList<String> ids = enStringsFile.getAllIds();
        if (ids.size() == 0) {
            Utils.loge("ids is null please ensure your string.xml has string record!");
            return;
        }
        AppExcel app = findAppByName(name);
        if (app == null) {
            Utils.loge("app " + name + " can't find from xls,please check the xls name is right!");
            return;
        }
        app.writeToFile(enStringsFile, mOutputDir);
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
        int langlen = mLanguages.size();
        for (int i = 0; i < langlen; i++) {
            Utils.logd("mLauguages " + mLanguages.get(i) +
                    " index is :"
                    + mIndex.get(mLanguages.get(i)));
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
            Utils.loge("language :" + content + "no language abbr!");
            return "";
        }
    }

    /*
     * init xml row 0 to language name param:sheet xml sheet object
     */
    private void readLanguages(Sheet sheet) {
        mIndex.clear();
        mLanguages.clear();
        Cell[] cells = sheet.getRow(Utils.LANGUAGE_ROW);
        if (cells != null && cells.length > 0) {
            for (int k = 0; k < cells.length; k++) {
                String content = cells[k].getContents();
                if (k > Utils.ID_COLUMN_INDEX && content.trim() != "") {
                    content = getLanguageName(content);
                    if (content != "") {
                        mIndex.put(content, k);
                        mLanguages.add(content);
                    }
                }
            }
        } else {
            Utils.loge("read Languages failed!");
        }
        // dumpLanguages();
    }

    private AppExcel findAppByName(String name) {
        int size = mApps.size();
        for (int i = 0; i < size; i++) {
            if (mApps.get(i).getAppName().equals(name)) {
                return mApps.get(i);
            }
        }
        return null;
    }

    public class AppExcel {
        private String mAppName;
        private ArrayList<Record> mRecords;
        private ArrayList<String> mStringsFiles;

        public AppExcel(String appName) {
            // Utils.logd("App construct appName:" + appName);
            mAppName = appName;
            mRecords = new ArrayList<Record>();
            mStringsFiles = new ArrayList<String>();
        }

        public void addStringsFileName(String name) {
            mStringsFiles.add(name);
        }

        public void setAppName(String name) {
            mAppName = name;
        }

        public String getAppName() {
            return mAppName;
        }

        public void add(Record record) {
            String name = mStringsFiles.get(mStringsFiles.size() - 1);
            if (Utils.isEmpty(name)) {
                name = Utils.STRING_FILE_NAME;
            }
            record.setStringsFileName(name);
//            Utils.logd("appName: " + mAppName + " string name : " + name  + " addRecord:\n" +
//                    record.toString());
            mRecords.add(record);
        }

        public int getRecordCount() {
            return mRecords.size();
        }

        public Record getRecordById(String id) {
            int size = mRecords.size();
            for (int i = 0; i < size; i++) {
                String idValue = mRecords.get(i).getIdValue();
                if (idValue != null && !idValue.equals("") && idValue.equals(id)) {
                    return mRecords.get(i);
                }
            }
            return null;
        }

        public Record[] getRecordById(String[] ids) {
            int size = mRecords.size();
            int idsCount = ids.length;
            Record[] records = new Record[idsCount];
            int idsIndex = 0;
            for (int i = 0; i < size; i++) {
                String idValue = mRecords.get(i).getIdValue();
                if (idValue != null && !idValue.equals("") && idValue.equals(ids[idsIndex])) {
                    records[idsIndex] = mRecords.get(i);
                    idsIndex++;
                }
                if (idsIndex == idsCount) {
                    return records;
                }
            }
            if (records[0] != null) {
                return records;
            }
            return null;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("AppName " + mAppName + " ")
                    .append("Rcord count : ")
                    .append(getRecordCount())
                    .append("\n");
            for (int i = 0; i < mRecords.size(); i++) {
                sb.append(mRecords.get(i).toString()).append("\n");
            }
            sb.delete(sb.length() - 1, sb.length());
            return sb.toString();
        }

        public void writeToFile(StringsFile stringsFile, String path) {
            Utils.loge("write App " + mAppName + " to dir " + mOutputDir + " record count : "
                    + getRecordCount());
            File appFile = new File(path, mAppName);
            Utils.initDir(appFile);
            int len = mLanguages.size();
            for (int i = 0; i < len; i++) {
                String language = mLanguages.get(i);
                int strIndex = mIndex.get(language);
                if (strIndex < 0) {
                    return;
                }
                File fileValue = new File(appFile, Utils.VALUES + "-" + language);
                if (language.equals("en")) {
                    fileValue = new File(appFile, Utils.VALUES);
                }
                Utils.initDir(fileValue);
                try {
                    OutputStreamWriter out =
                            new OutputStreamWriter(new FileOutputStream(new File(fileValue,
                                    stringsFile.getFileName())), Utils.UTF_8);
                    File errorFile = new File(fileValue, Utils.ERROR_FILE);
                    OutputStreamWriter error =
                            new OutputStreamWriter(new FileOutputStream(errorFile), Utils.UTF_8);
                    out.write(Utils.STRING_HEAD);
                    ArrayList<StringObj> mStrs = stringsFile.getAllStrs();
                    int size = mStrs.size();
                    boolean hasError = false;
                    for (int j = 0; j < size; j++) {
                        StringObj strObj = mStrs.get(j);
                        String[] ids = strObj.getAllIds();
                        // Utils.logd("ids "+ Arrays.toString(ids));
                        String[] values = null;
                        if (ids.length == 1) {
                            Record record = getRecordById(ids[0]);
                            if (record != null && record.getItemByIndex(strIndex) != null) {
                                values = new String[] {
                                        record.getItemByIndex(strIndex)
                                };
                            }
                        } else if (ids.length > 1) {
                            Record[] records = getRecordById(ids);
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

        public void writeToFile() {
            Utils.loge("write " + mAppName + " to dir " + mOutputDir + " count : "
                    + getRecordCount());
            File appFile = new File(mOutputDir, mAppName);
            Utils.initDir(appFile);
            int len = mLanguages.size();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < len; i++) {
                String language = mLanguages.get(i);
                int strIndex = mIndex.get(language);
                if (strIndex < 0) {
                    return;
                }
                // Utils.logd("language is : " + language);
                File fileValue = null;
                if (language.equals("en")) {
                    fileValue = new File(appFile, Utils.VALUES);
                } else {
                    fileValue = new File(appFile, Utils.VALUES + "-" + language);
                }
                Utils.initDir(fileValue);
                try {
                    OutputStreamWriter out =
                            new OutputStreamWriter(new FileOutputStream(new File(fileValue,
                                    Utils.STRING_FILE_NAME)), Utils.UTF_8);
                    File errorFile = new File(fileValue, Utils.ERROR_FILE);
                    OutputStreamWriter error =
                            new OutputStreamWriter(new FileOutputStream(errorFile), Utils.UTF_8);
                    out.write(Utils.STRING_HEAD);
                    boolean hasError = false;
                    String currentArrayName = "";
                    String currentPlurals = "";
                    for (Record record : mRecords) {
                        String name = record.getItemByIndex(Utils.ID_COLUMN_INDEX);
                        String value = record.getItemByIndex(strIndex);
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
        }
    }

    public class Record {

        private ArrayList<String> mItems;
        private int mLine;
        private String mStringsFileName;

        public Record(int line) {
            mLine = line;
            mItems = new ArrayList<String>();
        }

        public void addItem(String str) {
            mItems.add(str);
        }

        public String getItemByIndex(int index) {
            if (index >= mItems.size() || index < 0) {
                return null;
            }
            return mItems.get(index);
        }

        public int getColumnCount() {
            return mItems.size();
        }

        public String getIdValue() {
            return getItemByIndex(Utils.ID_COLUMN_INDEX);
        }

        public void setStringsFileName(String name) {
            mStringsFileName = name;
        }

        public String getStringsFileName() {
            return mStringsFileName;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            int count = getColumnCount();
            sb.append("Record ")
                    .append(" file name : " + mStringsFileName)
                    .append(" column count : " + count).append(" ")
                    .append(" line number : " + mLine + " : ");
            for (int i = 0; i < count; i++) {
                sb.append(Utils.SEPERATOR).append(mItems.get(i));
            }
            return sb.toString();
        }
    }

}
