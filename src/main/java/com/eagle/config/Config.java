
package main.java.com.eagle.config;

import main.java.com.eagle.Utils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class Config {

    private static final String TAG_CONFING = "config";

    public enum ConfigType {
        STRING_FILE, APP_NAME, LANGUAGE, BASE_DIR
    }

    private static final String TAG_ITEM = "item";
    private static final String TAG_ATTR_KEY = "key";
    private static final String TAG_ATTR_INDEX = "index";

    private ArrayList<String> mStringFileNames;
    private HashMap<String, ConfigItem> mLanguages;
    private ArrayList<String> mAppNames;
    private ArrayList<String> mBaseDirs;
    private Command mCommand = null;

    private static Config mInstance;

    private Config() {
        mStringFileNames = new ArrayList<String>();
        mLanguages = new HashMap<String, ConfigItem>();
        mAppNames = new ArrayList<String>();
        mBaseDirs = new ArrayList<String>();
        initConfig(Utils.CONFIG_FILE);
    }

    public static Config getInstance() {
        if (mInstance == null) {
            mInstance = new Config();
        }
        return mInstance;
    }

    public ArrayList<String> getStringFiles() {
        return mStringFileNames;
    }

    public HashMap<String, ConfigItem> getLanguages() {
        return mLanguages;
    }

    public ArrayList<String> getAppNames() {
        return mAppNames;
    }

    public ArrayList<String> getBaseDirs() {
        return mBaseDirs;
    }

    public Command getCommand() {
        return mCommand;
    }

    // [xml/xls] path -a -r -t -d path
    public Command parserCommand(String[] args) {
        int length = args.length;
        if (length >= 1) {
            try {
                mCommand = Command.valueOf(args[0].toUpperCase());
                for (int i = 1; i < length; i++) {
                    if (args[i].equals("-a")) {
                        mCommand.setIsBuidlPath(true);
                    } else if (args[i].equals("-t")) {
                        mCommand.setIncludeNotTranslate(true);
                    } else if (args[i].equals("-r")) {
                        mCommand.setReadEnStringId(true);
                    } else if (args[i].endsWith("-o")) {
                        if (i + 1 < length) {
                            if (!args[i + 1].startsWith("-")) {
                                mCommand.setOutputPath(args[i + 1]);
                                i = i + 1;
                            }
                        }
                    } else if (args[i].equals("-d")) {
                        mCommand.setDebug(true);
                    } else if (args[i].equals("-x")){
                        if (i + 1 < length) {
                            if (!args[i + 1].startsWith("-")) {
                                mCommand.setXlsPath(args[i + 1]);
                                i = i + 1;
                            }
                        }
                    } else {
                        mCommand.setSourcePath(args[i]);
                    }
                }
                updateCommandDefaultValue();
            } catch (Exception e) {
                e.printStackTrace();
                mCommand = null;
            }
        } else {
            mCommand = null;
        }
        return mCommand;
    }

    private void updateCommandDefaultValue(){
        switch (mCommand) {
            case XLS:
                if (Utils.isEmpty(mCommand.getOutputPath())) {
                    mCommand.setOutputPath(Utils.XLS_PATH);
                }
                break;
            case XML:
                if (Utils.isEmpty(mCommand.getOutputPath())) {
                    mCommand.setOutputPath(mCommand.isReadEnStringId() ? Utils.XML_RW_PATH
                            : Utils.XML_PATH);
                }
                if(Utils.isEmpty(mCommand.getXlsPath())){
                    mCommand.setXlsPath("./");
                }
                break;
        }
    }

    public void initConfig(String filePath) {
        File configFile = new File(filePath);
        Utils.logd("initConfig config file : " + configFile.toString() + " exists : "
                + configFile.exists());
        SAXParserFactory saxfac = SAXParserFactory.newInstance();
        try {
            SAXParser saxparser = saxfac.newSAXParser();
            InputStream is = new FileInputStream(filePath);
            saxparser.parse(is, new StringHandler());
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //dumpConfig();
    }

    private void updateConfig(ConfigItem item) {
        switch (item.getType()) {
            case STRING_FILE:
                mStringFileNames.add(item.getValue());
                break;
            case APP_NAME:
                mAppNames.add(item.getValue());
                break;
            case LANGUAGE:
                mLanguages.put(item.getKey(), item);
                break;
            case BASE_DIR:
                mBaseDirs.add(item.getValue());
                break;
        }
    }

    private class StringHandler extends DefaultHandler {

        private String value;
        private String key;
        private int index;
        private ConfigType type;
        private ConfigItem item;

        public StringHandler() {
        }

        @Override
        public void startDocument() throws SAXException {
        }

        @Override
        public void startElement(String namespaceURI, String localName, String qName,
                Attributes attrs)
                throws SAXException {
            // Utils.logd("startElement qName is : " + qName);
            if (qName.toLowerCase().equals(TAG_CONFING)) {
            } else if (qName.toUpperCase().equals(ConfigType.STRING_FILE.toString())) {
                type = ConfigType.STRING_FILE;
            } else if (qName.toUpperCase().equals(ConfigType.APP_NAME.toString())) {
                type = ConfigType.APP_NAME;
            } else if (qName.toUpperCase().equals(ConfigType.LANGUAGE.toString())) {
                type = ConfigType.LANGUAGE;
            } else if (qName.toUpperCase().equals(ConfigType.BASE_DIR.toString())) {
                type = ConfigType.BASE_DIR;
            } else if (qName.toLowerCase().equals(TAG_ITEM)) {
                item = new ConfigItem(type);
                if (attrs.getLength() > 0) {
                    int keyIndex = attrs.getIndex(TAG_ATTR_KEY);
                    if (keyIndex >= 0) {
                        key = attrs.getValue(keyIndex);
                        item.setKey(key);
                    }
                    int indexIndex = attrs.getIndex(TAG_ATTR_INDEX);
                    if (indexIndex >= 0) {
                        index = Integer.valueOf(attrs.getValue(indexIndex));
                        item.setIndex(index);
                    }
                }
            }
        }

        @Override
        public void characters(char[] chars, int start, int length) throws SAXException {
            value = new String(chars, start, length);
            if (item != null) {
                item.setValue(value);
            }
        }

        @Override
        public void endElement(String namespaceURI, String localName, String qName)
                throws SAXException {
            // Utils.logd("endElement qName is : " + qName);
            if (item != null) {
                updateConfig(item);
            }
            if (qName.toLowerCase().equals(TAG_CONFING)) {
            } else if (qName.toUpperCase().equals(ConfigType.STRING_FILE.toString())) {
                type = null;
            } else if (qName.toUpperCase().equals(ConfigType.APP_NAME.toString())) {
                type = null;
            } else if (qName.toUpperCase().equals(ConfigType.LANGUAGE.toString())) {
                type = null;
            } else if (qName.toUpperCase().equals(ConfigType.BASE_DIR.toString())) {
                type = null;
            } else if (qName.toLowerCase().equals(TAG_ITEM)) {
                item = null;
            }
        }

        @Override
        public void endDocument() throws SAXException {
        }
    }

    private void dumpConfig() {
        Utils.logd("");
        Utils.logd(ConfigType.STRING_FILE.toString());
        Utils.logd("");
        for (String stringFileName : mStringFileNames) {
            Utils.logd(stringFileName);
        }
        Utils.logd("");
        Utils.logd(ConfigType.APP_NAME.toString());
        Utils.logd("");
        for (String app : mAppNames) {
            Utils.logd(app);
        }
        Utils.logd("");
        Utils.logd(ConfigType.LANGUAGE.toString());
        Utils.logd("");
        Set<String> keys = mLanguages.keySet();
        for (String key : keys) {
            Utils.logd(mLanguages.get(key).toString());
        }
        Utils.logd("");
        Utils.logd(ConfigType.BASE_DIR.toString());
        Utils.logd("");
        for (String dir : mBaseDirs) {
            Utils.logd(dir);
        }
    }

    public enum Command {
        XLS(), XML();
        private boolean debug = true;
        private boolean isReadEnStringId = false;
        private boolean isIncludeNotTranslate = false;
        private boolean isBuildPath = false;
        public String sourcePath;
        public String outputPath;
        public String xlsPath;

        private Command() {
        }

        public void setReadEnStringId(boolean read) {
            isReadEnStringId = read;
        }

        public boolean isReadEnStringId() {
            return isReadEnStringId;
        }

        public void setIncludeNotTranslate(boolean include) {
            isIncludeNotTranslate = include;
        }

        public boolean isIncludeNotTranslate() {
            return isIncludeNotTranslate;
        }

        public void setIsBuidlPath(boolean buildPath) {
            isBuildPath = buildPath;
        }

        public boolean isBuildPath() {
            return isBuildPath;
        }

        public void setSourcePath(String path) {
            sourcePath = path;
        }

        public String getSourcePath() {
            return sourcePath;
        }

        public void setOutputPath(String outPath) {
            outputPath = outPath;
        }

        public String getOutputPath() {
            return outputPath;
        }

        public void setDebug(boolean debug) {
            this.debug = debug;
            if (Utils.DEBUG ^ debug) {
                Utils.DEBUG = debug;
            }
        }

        public boolean needDebug() {
            return debug;
        }

        public void setXlsPath(String path) {
            xlsPath = path;
        }

        public String getXlsPath() {
            return xlsPath;
        }

        public void dumpCommand() {
            StringBuilder sb = new StringBuilder("Command : ");
            sb.append(this.toString().toLowerCase());
            sb.append(" dir : ").append(getSourcePath());
            sb.append(" isBuildPath : ").append(isBuildPath());
            sb.append(" isReadEnStringId : ").append(isReadEnStringId());
            sb.append(" isIncludeNotTranslate : ").append(isIncludeNotTranslate());
            sb.append(" outputPath : ").append(getOutputPath());
            sb.append(" xlsPath : ").append(getXlsPath());
            sb.append(" debug : ").append(needDebug());
            Utils.logd(sb.toString());
        }
    }

}
