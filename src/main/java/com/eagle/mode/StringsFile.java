
package main.java.com.eagle.mode;

import main.java.com.eagle.Utils;

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
