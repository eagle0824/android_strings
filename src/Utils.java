
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class Utils {

    private static final boolean DEBUG = true;

    public static final String STRING_HEAD = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\n" +
            "<resources xmlns:android=\"http://schemas.android.com/apk/res/android\"" + "\n" +
            "    xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\">\n";
    public static final String STRING_END = "</resources>";

    public static final String VALUES = "values";

    public static final String STRING_FILE_NAME = "strings.xml";

    public static final String STRINT_RECORD_PREFIX = "    <string name=\"";
    public static final String STRINT_RECORD_MIDDLE = "\">\"";
    public static final String STRINT_RECORD_SURFIX = "\"</string>\n";
    public static final String ERROR_FILE = "error.txt";
    public static final String UTF_8 = "utf-8";
    public static final String ISO_8859_1 = "ISO-8859-1";
    public static final String SEPERATOR = "|";
    public static final String STR_PATH = "data";
    public static final String XLS_PATH = "xls";
    public static final String RW_PATH = "rwdata";

    public static final String STRING_ARRAY_HEAD_PREFIX = "    <string-array name=\"";
    public static final String STRING_ARRAY_HEAD_SURFIX = "\">\n";
    public static final String STRING_ARRAY_END = "    </string-array>\n";

    public static final String ITEM_PREFIX = "         <item>\"";
    public static final String ITEM_SUFFIX = "\"</item>\n";

    public static final String PLURALS_HEAD_PREFIX = "    <plurals name=\"";
    public static final String PLURALS_HEAD_SURFIX = "\">\n";
    public static final String PLURALS_END = "    </plurals>\n";

    public static final String PLURALS_ITEM_PREFIX = "         <item quantity=\"";
    public static final String PLURALS_MIDDLE_PREFIX = "\">\"";
    public static final String PLURALS_ITEM_SUFFIX = "\"</item>\n";

    public static int APP_NAME_COLUMN_INDEX = 0;
    public static int ID_COLUMN_INDEX = 1;
    public static int LANGUAGE_ROW = 0;
    public static final String SURFIX_XLS = ".xls";
    public static final String NEW_XLS_NAME = "allstrings.xls";
    public static int SHEET0_INDEX = 0;

    public static final char NEWLINE = '\n';
    public static final char TAB = '\t';
    public static final char NEWLINE_REPLACE_CHAR = '*';
    public static final char NEWLINE_REPLACE_TAB = '@';

    public static final String COMMAND_CREATE_XLS = "cxls";
    public static final String COMMAND_CREATE_XML = "cxml";
    public static final String COMMAND_READ_CREATE_XML = "rcxml";

    public static final String SURFIX_ARRAY = "-array-";
    public static final String SURFIX_PLURALS = "-plurals-";
    public static final String QUANTITY = "quantity";
    public static final String FRAMEWORKS = "frameworks";
    public static final String RES = "res";
    public static final String FRAMEWORK_BASE_PACKAGE = "frameworks/base/packages";
    public static final String FRAMEWORK_BASE_RES = "frameworks/base/core/res";
    public static final String PACKAGES = "packages";

    public static final String[] BUID_STRING_XML_PATH = {
        /*FRAMEWORK_BASE_PACKAGE,*/
        FRAMEWORK_BASE_RES,
        PACKAGES,
    };

    public static final String SHEET0_NAME = "多国语言";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_TRANSLATABLE = "translatable";
    public static final String ATTR_TRANSLATE = "translate";
    public static final String ATTR_PRODUCT = "product";

    public static final boolean isRepaceTabAndNewLine = false;

    public static HashMap<String,String> mLanguages = new HashMap<String,String>();
    public static HashMap<String,Integer> mLanguageIndexes = new HashMap<String,Integer>();
    public static ArrayList<String> mAppNames = new ArrayList<String>();

    /*
     * init languages titile name and index
     * mLanguages size must equal mLanguageIndexes size
     */
    static{
        mLanguages.put("values", "英语|en");
        mLanguages.put("values-zh-rCN", "中文|zh-rCN");
        mLanguages.put("values-ru", "俄语|ru");
        mLanguages.put("values-el", "希腊语|el");
        mLanguages.put("values-pl", "波兰语|pl");
        mLanguages.put("values-tr", "土耳其语|tr");
        mLanguages.put("values-ar", "阿拉伯文|ar");
        mLanguages.put("values-fa", "波斯语|fa");
        mLanguages.put("values-ro", "罗马尼亚|ro");
        mLanguages.put("values-fr", "法语|fr");
        mLanguages.put("values-hu", "匈牙利语|hu");
        mLanguages.put("values-it", "意大利语|it");
        mLanguages.put("values-th", "泰语|th");
        mLanguages.put("values-de", "德语|de");
        mLanguages.put("values-uk", "乌克兰语|uk");
        mLanguages.put("values-es", "西班牙|es");
        mLanguages.put("values-pt", "葡萄牙|pt");
        mLanguages.put("values-zh-rTW", "繁体|zh-rTW");

        /*
        mLanguages.put("values-bg", "保加利亚");
        mLanguages.put("values-ca", "加泰罗尼亚");
        mLanguages.put("values-cs", "捷克");
        mLanguages.put("values-da", "丹麦");
        mLanguages.put("values-en-rGB", "英语(英国)");
        mLanguages.put("values-es-rUS", "英语(美国)");
        mLanguages.put("values-fi", "芬兰文");
        mLanguages.put("values-hr", "克罗里亚");
        mLanguages.put("values-in", "印度尼西亚");
        mLanguages.put("values-iw", "希伯来");
        mLanguages.put("values-ja", "日文");
        mLanguages.put("values-ko", "韩文");
        mLanguages.put("values-lt", "立陶宛");
        mLanguages.put("values-lv", "拉脱维亚");
        mLanguages.put("values-nb", "挪威博克马尔");
        mLanguages.put("values-nl", "荷兰文");
        mLanguages.put("values-pt-rPT", "葡萄牙(葡萄牙");
        mLanguages.put("values-sk", "斯洛伐克");
        mLanguages.put("values-sl", "斯洛文尼亚");
        mLanguages.put("values-sr", "塞尔维亚文");
        mLanguages.put("values-sv", "瑞典文");
        mLanguages.put("values-tl", "塔加洛语");
        mLanguages.put("values-vi", "越南");
        */

        mLanguageIndexes.put("values", 2);
        mLanguageIndexes.put("values-zh-rCN", 4);
        mLanguageIndexes.put("values-ru", 5);
        mLanguageIndexes.put("values-el", 6);
        mLanguageIndexes.put("values-pl", 7);
        mLanguageIndexes.put("values-tr", 8);
        mLanguageIndexes.put("values-ar", 9);
        mLanguageIndexes.put("values-fa", 10);
        mLanguageIndexes.put("values-ro", 11);
        mLanguageIndexes.put("values-fr", 12);
        mLanguageIndexes.put("values-hu", 13);
        mLanguageIndexes.put("values-it", 14);
        mLanguageIndexes.put("values-th", 15);
        mLanguageIndexes.put("values-de", 16);
        mLanguageIndexes.put("values-uk", 17);
        mLanguageIndexes.put("values-es", 18);
        mLanguageIndexes.put("values-pt", 19);
        mLanguageIndexes.put("values-zh-rTW",20);
        /*
        mLanguageIndexes.put("values-bg", 6);
        mLanguageIndexes.put("values-ca", 7);
        mLanguageIndexes.put("values-cs", 8);
        mLanguageIndexes.put("values-da", 9);
        mLanguageIndexes.put("values-en-rGB", 12);
        mLanguageIndexes.put("values-es-rUS", 14);
        mLanguageIndexes.put("values-fi", 16);
        mLanguageIndexes.put("values-hr", 18);
        mLanguageIndexes.put("values-in", 20);
        mLanguageIndexes.put("values-iw", 22);
        mLanguageIndexes.put("values-ja", 23);
        mLanguageIndexes.put("values-ko", 24);
        mLanguageIndexes.put("values-lt", 25);
        mLanguageIndexes.put("values-lv", 26);
        mLanguageIndexes.put("values-nb", 27);
        mLanguageIndexes.put("values-nl", 28);
        mLanguageIndexes.put("values-pt-rPT", 31);
        mLanguageIndexes.put("values-rm", 32);
        mLanguageIndexes.put("values-sk", 35);
        mLanguageIndexes.put("values-sl", 36);
        mLanguageIndexes.put("values-sr", 37);
        mLanguageIndexes.put("values-sv", 38);
        mLanguageIndexes.put("values-tl", 40);
        mLanguageIndexes.put("values-vi", 43);
        */
        mAppNames.add("AUX");
        mAppNames.add("BootLogo");
        mAppNames.add("BTMusic");
        mAppNames.add("Calendar2");
        mAppNames.add("CarDVR");
        mAppNames.add("Contacts");
        mAppNames.add("DVD");
        mAppNames.add("Ebook");
        mAppNames.add("FactoryMode");
        mAppNames.add("FileManager");
        mAppNames.add("FMAM");
        mAppNames.add("Gallery2");
        mAppNames.add("GlobalTime");
        mAppNames.add("IPOD");
        mAppNames.add("Launcher2");
        mAppNames.add("Music");
        mAppNames.add("Navigation");
        mAppNames.add("Phone");
        mAppNames.add("RearZone");
        mAppNames.add("Settings");
        mAppNames.add("SteeringWheel");
        mAppNames.add("TaskManager");
        mAppNames.add("tscalibration");
        mAppNames.add("TV");
        mAppNames.add("VCDC");
        mAppNames.add("VideoPlayer");
        mAppNames.add("VideoRecord");
        //some wincalcd app
        mAppNames.add("AUX2");
        mAppNames.add("Disk");
        mAppNames.add("Gallery3D");
        mAppNames.add("GpsMonitor");
        mAppNames.add("MagicLink");
        mAppNames.add("MagicLinkCore");
        mAppNames.add("Maintainment");
        mAppNames.add("PackageInstaller");
        mAppNames.add("Scheduler");
        mAppNames.add("Email");
        mAppNames.add("TsCalibrate");
        mAppNames.add("MagicLink");
        mAppNames.add("MagicLinkCore");
        mAppNames.add("MirrorLinkMobile");
    }


    public static void logd(String str) {
        if (DEBUG) {
            System.out.println("    "+str);
        }
    }

    public static void loge(String str) {
        System.out.println("    "+str);
    }

    /*
     * parameter c --create xls must and source path parameter r --carete
     * parameter w start end --write string.xml
     */
    public static void main(String args[]) {
        int len = args.length;
        logd(Arrays.toString(args));
        if (len > 0) {
            parserParams(args, len);
        } else {
            printHelp();
        }
    }

    private static void parserParams(String args[], int len) {
        Utils mUtils = new Utils();
        Command command = Command.getInstace();
        command.resetCommand();
        String arg0 = args[0].toLowerCase();
        if(arg0.equals(COMMAND_READ_CREATE_XML)){
            command.isCW = true;
            if (len >= 2) {
                command.mPath = args[1];
                for (int i = 2; i < len; i++) {
                    if (args[i].equals("-a")) {
                        command.isBuildPath = true;
                    } else if (args[i].equals("-t")) {
                        command.isIncludeNotTranslate = true;
                    } else {
                        Utils.printHelp();
                        return;
                    }
                }
            } else {
                Utils.printHelp();
                return;
            }
            mUtils.doCmdRCreateXml(command);
        }else if (arg0.equals(COMMAND_CREATE_XLS)) {
            if (len >= 2) {
                command.mPath = args[1];
                for (int i = 2; i < len; i++) {
                    if (args[i].equals("-a")) {
                        command.isBuildPath = true;
                    } else if (args[i].equals("-t")) {
                        command.isIncludeNotTranslate = true;
                    } else {
                        Utils.printHelp();
                        return;
                    }
                }
            } else {
                Utils.printHelp();
                return;
            }
            mUtils.doCmdCreateXls(command);
        } else if (arg0.equals(COMMAND_CREATE_XML)) {
            int start = -1;
            int end = -1;
            if (len == 3) {
                try {
                    start = Integer.parseInt(args[1]);
                    end = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    // e.printStackTrace();
                    Utils.loge("");
                    Utils.loge("please sure start " + args[1] + " and end " + args[2]
                            + " must be number!");
                    Utils.loge("");
                }
            }
            new RExcel(start, end);
        } else {
            printHelp();
        }
    }

    private void doCmdCreateXls(Command cmd){
        Utils.logd("doCmdCreateXls ===> fileUri is : " + cmd.mPath + " isBuildPath : " + cmd.isBuildPath + " ; save path : "
                + cmd.savePath + " ; isRw :" + cmd.isCW + " ; include not translate :"
                + cmd.isIncludeNotTranslate);
        String fileUri = cmd.mPath;
        if (fileUri == "") {
            Utils.loge("file uri is empty!");
            return;
        }
        File file = new File(fileUri);
        if (file.exists()) {
            //allStringFiles.clear();
            Utils.deleteFile(new File(Utils.XLS_PATH,Utils.NEW_XLS_NAME));
            Utils.logd("finding strings.xml file.Please wait ...");
            Utils.initDir(Utils.XLS_PATH);
            if (cmd.isBuildPath) {
                getBuildFiles(file);
            } else {
                getStringFiles(file);
            }
            Utils.logd("finding strings.xml file finished!");
        } else {
            Utils.loge("File " + file.getPath() + "not exist!");
            return;
        }
    }

    ArrayList<String> stringPaths = new ArrayList<String>();
    private void doCmdRCreateXml(Command cmd){
        Utils.logd("doCmdRCreateXml ===> fileUri is : " + cmd.mPath + " isBuildPath : " + cmd.isBuildPath + " ; save path : "
                + cmd.savePath + " ; isRw :" + cmd.isCW + " ; include not translate :"
                + cmd.isIncludeNotTranslate);
        String fileUri = cmd.mPath;
        if (fileUri == "") {
            Utils.loge("file uri is empty!");
            return;
        }
        File file = new File(fileUri);
        if (file.exists()) {
            stringPaths.clear();
            Utils.logd("finding strings.xml file.Please wait ...");
            if(cmd.isBuildPath){
                getEnBuildFiles(file);
            }else{
                getEnStringFiles(file);
            }
            Utils.logd("finding strings.xml file finished! size : " + stringPaths.size());
            int size = stringPaths.size();
            if (size > 0 ) {
                Utils.initDir(Utils.RW_PATH);
                RExcel mRExcel = new RExcel(-1, -1);
                for (int i = 0; i < size; i++) {
                    //Utils.logd(stringPaths.get(i));
                    StringsFile strFile = new StringsFile(stringPaths.get(i));
                    strFile.doParserStringsFile();
                    mRExcel.createXmlByStringsFile(strFile);
                }
            }
        } else {
            Utils.loge("File " + file.getPath() + "not exist!");
            return;
        }
    }

    private void getEnStringFiles(File file) {
        String path = file.getAbsolutePath();
        //Utils.logd("getEnStringFiles ===> " + path);
        if (file.isDirectory() && !path.contains("values-") &&!path.contains("test/res")
                && !path.contains("tests/res")) {
            File[] files = file.listFiles();
            int len = files.length;
            if (len > 0) {
                for (int i = 0; i < len; i++) {
                    getEnStringFiles(files[i]);
                }
            }
        } else {
            if (path.endsWith("values"+"/"+Utils.STRING_FILE_NAME)) {
                if(mAppNames.contains(getAppNameByStringPath(path))){
                    stringPaths.add(path);
                }else if(path.endsWith("res/res/values"+"/"+Utils.STRING_FILE_NAME)){
                    stringPaths.add(path);
                }
            }
        }
    }

    private void getEnBuildFiles(File file) {
        Utils.logd("getEnBuildFiles :" + file);
        int len = Utils.BUID_STRING_XML_PATH.length;
        for (int i = 0; i < len; i++) {
            String path = Utils.BUID_STRING_XML_PATH[i];
            getEnStringFiles(new File(file,path));
        }
    }

    private void getStringFiles(File file){
        String path = file.getAbsolutePath();
        if(file.isFile() && path.endsWith(Utils.STRING_FILE_NAME)){
            StringsFile stringsFile = new StringsFile(path);
            stringsFile.doParserStringsFile();
            stringsFile.writeToExcel(Utils.XLS_PATH);
        }else if(file.isDirectory()){
            File[] files = file.listFiles();
            for(int i=0;i<files.length;i++){
                File subFile = files[i];
                if(subFile.isDirectory()){
                    if(subFile.getAbsolutePath().endsWith("res")){
                        File parent = subFile.getParentFile();
                        if(parent.isDirectory() && !parent.getAbsolutePath().endsWith("bin")){
                            AppDir appDir = new AppDir(parent);
                            appDir.parserAppDir();
                            break;
                        }else {
                            break;
                        }
                    } else {
                        getStringFiles(subFile);
                    }
                }else{
                    getStringFiles(subFile);
                }
            }
        }
    }


    public String getAppNameByStringPath(String path) {
        String[] strs = path.split("\\/");
        int len = strs.length;
        if (len > 4) {
            //Utils.logd(strs[len - 4]);
            return strs[len - 4];
        }
        return path;
    }

    public String getAppNameByAppPath(String path) {
        String[] strs = path.split("\\/");
        int len = strs.length;
        if (len > 1) {
            //Utils.logd(strs[len - 4]);
            return strs[len - 1];
        }
        return path;
    }

    private void getBuildFiles(File file) {
        int len = Utils.BUID_STRING_XML_PATH.length;
        for (int i = 0; i < len; i++) {
            String path = Utils.BUID_STRING_XML_PATH[i];
            getInitAppDir(new File(file, path));
        }
    }

    private void getInitAppDir(File file){
        if(file.isDirectory()){
            String filePath = file.getAbsolutePath();
            if(filePath.endsWith(Utils.FRAMEWORK_BASE_RES)){
                AppDir mAppDir = new AppDir(Utils.FRAMEWORKS,file.getAbsolutePath());
                mAppDir.parserAppDir();
            }else if(filePath.endsWith(Utils.PACKAGES)){
                dealDirectory(file);
            }else{
                Utils.logd("not treat current :dir:" + filePath);
            }
        }
    }

    private void dealDirectory(File file){
        if(file.isDirectory()){
            AppDir appDir = null;
            String path = file.getAbsolutePath();
            //Utils.logd("dealDir file : " + path);
            if(path.endsWith(Utils.FRAMEWORK_BASE_PACKAGE)){
                File[] files = file.listFiles();
                int size = files.length;
                for(int i=0;i<size;i++){
                    appDir = new AppDir(files[i]);
                    appDir.parserAppDir();
                    
                }
            }else if(path.endsWith(Utils.PACKAGES)){
                File[] files = file.listFiles();
                int size = files.length;
                for(int i=0;i<size;i++){
                    dealDirectory(files[i]);
                }
            }else {
                String[] paths = path.split("\\/");
                int length = paths.length;
                if(length>2 && paths[length-2].equals(Utils.PACKAGES)){
                    File[] files = file.listFiles();
                    int size = files.length;
                    for(int i=0;i<size;i++){
                        if(mAppNames.contains(getAppNameByAppPath(files[i].getAbsolutePath()))){
                            appDir = new AppDir(files[i]);
                            appDir.parserAppDir();
                        }
                    }
                }
            }
        }else{
            Utils.logd("deal packages not is dir path is : " +file.getAbsolutePath());
        }
    }

    /*
     * delete all files which at path and create directory according path
     * param:path is directory's path
     */
    public static void initDir(String path) {
        if (!path.equals("")) {
            File file = new File(path);
            initDir(file);
        }
    }

    /*
     * delete all files at dirFile and create directory dirFile
     * param:dirFile is directory file
     */
    public static void initDir(File dirFile) {
        if (dirFile != null) {
            if (dirFile.exists() && dirFile.isDirectory()) {
                deleteFile(dirFile);
            }
            dirFile.mkdir();
        }
    }

    public static void deleteDir(File dirFile){
        
    }
    /*
     * delete File or all files at file's directory
     */
    public static void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            int size = files.length;
            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    deleteFile(files[i]);
                    files[i].delete();
                }
            } else {
                boolean isSuccess = file.delete();
                //Utils.logd("delete file :" + file.getAbsolutePath() + " ; delete successful : " +isSuccess);
            }
            file.delete();
        } else {
            boolean isSuccess = file.delete();
            //Utils.logd("delete data file :" + file.getAbsolutePath()+ " ; delete successful : " + isSuccess);
        }
    }

    public static String replaceNewLine(String str) {
       String line = removeBlankAndQutation(str);
        if (isRepaceTabAndNewLine) {
            char[] chars = line.toCharArray();
            int len = chars.length;
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if (c == Utils.NEWLINE && i != len - 1) {
                    chars[i] = Utils.NEWLINE_REPLACE_CHAR;
                } else if (c == Utils.TAB && i != len - 1) {
                    chars[i] = Utils.NEWLINE_REPLACE_TAB;
                }
            }
            return new String(chars);
        } else {
            return line;
        }
    }

    private static final String REGULAR_SPECIAL_CHARS = ".*<.*>.*";
    private static final String REGULAR_REPLACE = ".*%.$[sd].*";

    public static boolean isContainSpecialCharacter(String str){
        boolean isSpecial = false;
        Pattern pat = Pattern.compile(REGULAR_SPECIAL_CHARS);
        Matcher mat = pat.matcher(str);
        isSpecial = mat.find();
        if(isSpecial){
            return isSpecial;
        }

        if(str.indexOf("\\n")!=-1){
            return isSpecial;
        }

        if(str.indexOf('\u2026')!=-1){
            return isSpecial;
        }
//        pat = Pattern.compile(REGULAR_REPLACE);
//        mat= pat.matcher(str.toLowerCase());
//        isSpecial = mat.find();
//        if(isSpecial){
//            logd("str : " + str + " %N$s");
//        }
        return isSpecial;
    }
    /*
     * remove the string's blank and qutation
     */
    public static String removeBlankAndQutation(String str){
        String line = str.trim();
        int length = line.length();
        if ((length > 1) && (line.charAt(0) == '"')
                && (line.charAt(length - 1) == '"')) {
            return line.substring(1, length - 1);
        }
        return line;
    }

    public static void printHelp() {
        Utils.loge("");
        Utils.loge("=========================================================");
        Utils.loge("USAGE:");
        Utils.loge("      ./build.sh [command] [path] {params}");
        Utils.loge(">>>>>>>    command  "+COMMAND_CREATE_XLS+"    <<<<<<<<<<<<<<<");
        Utils.loge("       command " + COMMAND_CREATE_XLS
                + " will create a xxx.xls at xls direcotry according the path of user input!");
        Utils.loge("");
        Utils.loge("       path is the app dir (absolute path) which contains strings.xml or strings.xml");
        Utils.loge("       params is Optional -a or -t ");
        Utils.loge("       params -a is stand for the path is android root path");
        Utils.loge("       params -t is stand for include all strings of strings.xml(include translatable=false)");
        Utils.loge("");
        Utils.loge("");
        Utils.loge(">>>>>>>    command  "+COMMAND_CREATE_XML+"    <<<<<<<<<<<<<<<");
        Utils.loge("       command " + COMMAND_CREATE_XML
                + " will create some string.xml at data dirctory from the xxx.xls which in current directory!");
        Utils.loge("       you must put your xls file in the current dir");
        Utils.loge("       if has start and end,start must gratter end,start and end can't has only one,");
        Utils.loge("       if not has start and end default is all xls files");
        Utils.loge("");
        Utils.loge("");
        Utils.loge(">>>>>>>    command  "+COMMAND_READ_CREATE_XML+"    <<<<<<<<<<<<<<<");
        Utils.loge("       command " + COMMAND_READ_CREATE_XML
                + " will create some string.xml which the id come from the path " +
                " value/strings.xml and value from the xxx.xls ");
        Utils.loge("       path is the dir which contains strings.xml or strings.xml absolute path");
        Utils.loge("       params is Optional -a or -t ");
        Utils.loge("       params -a is stand for the path is android root path");
        Utils.loge("       params -t is stand for include all strings of strings.xml(include translatable=false)");
        Utils.loge("       the output dir "+STR_PATH+"(id and vlaue are both from xls file) " +
                "and output dir "+RW_PATH+"(which id from values/string and value from xls file)");
        
        Utils.loge("");
        Utils.loge("some times you can find an error.txt int strings.xml dir,it records some error when write strings.xml");
        Utils.loge("=========================================================");
        Utils.loge("");
    }
}
