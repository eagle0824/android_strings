
package main.java.com.eagle.mode;

import main.java.com.eagle.Utils;
import main.java.com.eagle.config.Config;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

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
    /*
     * mAppDir is which the strings.xml belong to
     */
    private App mAppDir;

    private StringsFolder mFolder;
    private String mFileName;

    public StringsFile(String path, StringsFolder folder) {
        mPath = path;
        mFolder = folder;
        mStrs = new ArrayList<StringObj>();
    }

    public StringsFile(String path, App appDir) {
        mPath = path;
        mStrs = new ArrayList<StringObj>();
        mAppDir = appDir;
    }

    public StringsFile(String path) {
        mPath = path;
        mAppDir = new App("StringFile", path);
    }

    public String getAppName() {
        String name = mAppDir.getName();
        if (!Utils.isEmpty(name)) {
            return name;
        }
        return mPath;
    }

    public String getFileName() {
        if (Utils.isEmpty(mFileName)) {
            mFileName = new File(mPath).getName();
        }
        return mFileName;
    }

    public String getValuesFolderName() {
        File file = new File(mPath);
        if (file.exists()) {
            return file.getParentFile().getName();
        }
        return mPath;
    }

    public void parser() {
        parserFile(mPath, mFolder);
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

    private void parserFile(String stringsFilePath, StringsFolder folder) {
        SAXParserFactory saxfac = SAXParserFactory.newInstance();
        try {
            SAXParser saxparser = saxfac.newSAXParser();
            InputStream is = new FileInputStream(stringsFilePath);
            saxparser.parse(is, new StringHandler(this, folder));
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
