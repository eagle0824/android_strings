
package main.java.com.eagle.mode;

import main.java.com.eagle.Utils;
import main.java.com.eagle.config.Config;
import main.java.com.eagle.config.ConfigItem;
import main.java.com.eagle.mode.App.CellType;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/*
 * StringsFile is mode strings.xml
 */
public class StringsFile {
    /*
     * <string name="aa">bbb</string> is StringObj
     */
    private ArrayList<StringObj> mStrs;
    /*
     * mPath is strings.xml absolute path
     */
    private String mPath;

    private StringsFolder mFolder;
    private String mFileName;

    public StringsFile(String path, StringsFolder folder) {
        mPath = path;
        mFolder = folder;
        mStrs = new ArrayList<StringObj>();
    }

    public StringsFile(String path) {
        this(path, null);
    }

    public String getAppName() {
        String name = "";
        if (mFolder != null) {
            name = mFolder.getApp().getName();
        } else {
            name = getAppNameByStringFile();
        }
        if (!Utils.isEmpty(name)) {
            return name;
        } else {
            return mPath;
        }
    }

    private String getAppNameByStringFile() {
        String[] strs = mPath.split("\\/");
        int len = strs.length;
        if (len > 4) {
            Utils.logd(strs[len - 4]);
            return strs[len - 4];
        }
        return "";
    }

    public String getFileName() {
        if (Utils.isEmpty(mFileName)) {
            mFileName = new File(mPath).getName();
        }
        return mFileName;
    }

    public String getFolderName() {
        File file = new File(mPath);
        if (file.exists()) {
            return file.getParentFile().getName();
        }
        return mPath;
    }

    public void parser() {
        parserFile(mPath);
    }

    public void writeToExcel(WritableSheet sheet, HashMap<String, Integer> idsMap, int rowOffset)
            throws RowsExceededException,
            WriteException {

        String folderName = getFolderName();
        boolean isEnglish = Utils.VALUES.equals(folderName);
        HashMap<String, ConfigItem> languages = Config.getInstance().getLanguages();
        if (!languages.containsKey(folderName)) {
            return;
        }
        ConfigItem item = languages.get(folderName);

        int currenRow = sheet.getRows() + rowOffset - 1;
        ArrayList<StringObj> mStrs = getAllStrs();
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
                                getFileName(), writableCellFormat);
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

    /*
     * just write to single xls file
     */
    public void writeToExcel() {
        WritableWorkbook wb = null;
        WritableSheet sheet = null;
        Workbook rwb = null;
        String savePath = Config.getInstance().getCommand().getOutputPath();
        File xlsFile = new File(savePath, Utils.XLS_FILE_NAME);
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
            Label label = new Label(0, currenRow, mPath);
            sheet.addCell(label);
            currenRow += 1;
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

    public ArrayList<String> getAllIds() {
        ArrayList<String> ids = new ArrayList<String>();
        int size = mStrs.size();
        for (int i = 0; i < size; i++) {
            String[] strIds = mStrs.get(i).getAllIds();
            for (int j = 0; j < strIds.length; j++) {
                ids.add(strIds[j]);
            }
        }
        return ids;
    }

    public String getPath() {
        return mPath;
    }

    public ArrayList<StringObj> getAllStrs() {
        return mStrs;
    }

    public void setArrayStrs(ArrayList<StringObj> strs) {
        mStrs = strs;
    }

    public void addStrings(ArrayList<StringObj> strs) {
        mStrs.addAll(strs);
    }

    private void parserFile(String stringsFilePath) {
        SAXParserFactory saxfac = SAXParserFactory.newInstance();
        try {
            SAXParser saxparser = saxfac.newSAXParser();
            InputStream is = new FileInputStream(stringsFilePath);
            saxparser.parse(is, new StringHandler(this));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
