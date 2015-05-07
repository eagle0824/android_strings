
package main.java.com.eagle;

import main.java.com.eagle.config.Config;
import main.java.com.eagle.config.Config.Command;
import main.java.com.eagle.mode.App;
import main.java.com.eagle.mode.StringsFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static boolean DEBUG = true;

    public static final String STRING_HEAD = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\n" +
            "<resources xmlns:android=\"http://schemas.android.com/apk/res/android\"" + "\n" +
            "    xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\">\n";
    public static final String STRING_END = "</resources>";

    public static final String VALUES = "values";

    public static final String STRING_FILE_NAME = "strings.xml";

    public static final String CONFIG_FILE = "res/config.xml";

    public static final String STRINT_RECORD_PREFIX = "    <string name=\"";
    public static final String STRINT_RECORD_MIDDLE = "\">\"";
    public static final String STRINT_RECORD_SURFIX = "\"</string>\n";
    public static final String ERROR_FILE = "error.txt";
    public static final String UTF_8 = "utf-8";
    public static final String ISO_8859_1 = "ISO-8859-1";
    public static final String SEPERATOR = "|";
    public static final String XLS_PATH = "xls";
    public static final String XML_PATH = "xml";
    public static final String XML_RW_PATH = "rwxml";
    public static final String XLS_FILE_NAME = "StringFile.xls";

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

    private static final String REGULAR_SPECIAL_CHARS = ".*<.*>.*";

    public static final String SHEET0_NAME = "多国语言";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_TRANSLATABLE = "translatable";
    public static final String ATTR_TRANSLATE = "translate";
    public static final String ATTR_PRODUCT = "product";

    public static final boolean isRepaceTabAndNewLine = false;

    private static Utils mInstance;

    private ArrayList<String> stringPaths = new ArrayList<String>();

    public void parserArgs(String[] args) {
        Utils.logd(Arrays.toString(args));
        int len = args.length;
        if (len > 0) {
            if (len == 1 && args[0].equals("-h")) {
                printHelp();
            } else {
                Config config = Config.getInstance();
                Config.Command cmd = config.parserCommand(args);
                if (cmd == null) {
                    printHelp();
                } else {
                    cmd.dumpCommand();
                    doCommand(cmd);
                }
            }
        } else {
            printHelp();
        }
    }

    public void doCommand(Command cmd) {
        switch (cmd) {
            case XLS:
                doCmdCreateXls(cmd);
                break;
            case XML:
                doCmdCreateXml(cmd);
                break;
        }
    }

    private void doCmdCreateXls(Command cmd) {
        String fileUri = cmd.getSourcePath();
        if (isEmpty(fileUri)) {
            loge("source dir is empty!");
            return;
        }
        File file = new File(fileUri);
        if (file.exists()) {
            // allStringFiles.clear();
            deleteFile(new File(cmd.getOutputPath(), NEW_XLS_NAME));
            loge("begin create xls. Please wait ...");
            initDir(cmd.getOutputPath());
            if (cmd.isBuildPath()) {
                getBuildFiles(file);
            } else {
                getStringFiles(file);
            }
            loge("end create xls!");
        } else {
            loge("File " + file.getPath() + "not exist!");
            return;
        }
    }

    private void doCmdCreateXml(Command cmd) {
        File xlsFile = getXlsFile(cmd.getXlsPath());
        if (xlsFile == null) {
            loge("can't find xls file on dir " + cmd.getXlsPath());
            return;
        }
        if (cmd.isReadEnStringId()) {
            String fileUri = cmd.getSourcePath();
            if (isEmpty(fileUri)) {
                loge("source dir is empty!");
                return;
            }
            File file = new File(fileUri);
            if (file.exists()) {
                stringPaths.clear();
                logd("finding strings.xml file.Please wait ...");
                if (cmd.isBuildPath()) {
                    getEnBuildFiles(file);
                } else {
                    getEnStringFiles(file);
                }
                int size = stringPaths.size();
                logd("finding strings.xml file finished! size : " + size);
                if (size > 0) {
                    ExcelHelper mExcelHelper = new ExcelHelper(cmd.getOutputPath());
                    mExcelHelper.createXmlFromXlsFile(xlsFile);
                    for (int i = 0; i < size; i++) {
                        // logd(stringPaths.get(i));
                        StringsFile strFile = new StringsFile(stringPaths.get(i));
                        strFile.doParserStringsFile();
                        mExcelHelper.createXmlByStringsFile(strFile);
                    }
                }
            } else {
                loge("File " + file.getPath() + "not exist!");
                return;
            }
        } else {
            ExcelHelper mExcelHelper = new ExcelHelper(cmd.getOutputPath());
            mExcelHelper.createXmlFromXlsFile(xlsFile);
        }
    }

    private void getEnStringFiles(File file) {
        String path = file.getAbsolutePath();
        logd("getEnStringFiles ===> " + path);
        if (file.isDirectory() && !path.contains("values-") && !path.contains("test/res")
                && !path.contains("tests/res")) {
            File[] files = file.listFiles();
            int len = files.length;
            if (len > 0) {
                for (int i = 0; i < len; i++) {
                    getEnStringFiles(files[i]);
                }
            }
        } else {
            ArrayList<String> stringFiles = Config.getInstance().getStringFiles();
            int size = stringFiles.size();
            ArrayList<String> appNames = Config.getInstance().getAppNames();
            for (int i = 0; i < size; i++) {
                String name = stringFiles.get(i);
                if (path.endsWith("values" + "/" + name)) {
                    if (appNames.contains(getAppNameByStringPath(path))) {
                        stringPaths.add(path);
                    } else if (path.endsWith("res/res/values" + "/" + name)) {
                        stringPaths.add(path);
                    }
                }
            }
        }
    }

    private void getBuildFiles(File file) {
        ArrayList<String> baseDirs = Config.getInstance().getBaseDirs();
        for (String dir : baseDirs) {
            getInitAppDir(new File(file, dir));
        }
    }

    private void getEnBuildFiles(File file) {
        logd("getEnBuildFiles :" + file);
        ArrayList<String> baseDirs = Config.getInstance().getBaseDirs();
        for (String dir : baseDirs) {
            getEnStringFiles(new File(file, dir));
        }
    }

    private void getStringFiles(File file) {
        String path = file.getAbsolutePath();
        if (file.isFile()) {
            ArrayList<String> stringFiles = Config.getInstance().getStringFiles();
            int size = stringFiles.size();
            for (int i = 0; i < size; i++) {
                if (path.endsWith(stringFiles.get(i))) {
                    StringsFile stringsFile = new StringsFile(path);
                    stringsFile.doParserStringsFile();
                    stringsFile.writeToExcel();
                }
            }
        } else if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File subFile = files[i];
                if (subFile.isDirectory()) {
                    if (subFile.getAbsolutePath().endsWith("res")) {
                        File parent = subFile.getParentFile();
                        if (parent.isDirectory() && !parent.getAbsolutePath().endsWith("bin")) {
                            App appDir = new App(parent);
                            appDir.parserAppDir();
                            break;
                        } else {
                            break;
                        }
                    } else {
                        getStringFiles(subFile);
                    }
                } else {
                    getStringFiles(subFile);
                }
            }
        }
    }

    public String getAppNameByStringPath(String path) {
        String[] strs = path.split("\\/");
        int len = strs.length;
        if (len > 4) {
            // logd(strs[len - 4]);
            return strs[len - 4];
        }
        return path;
    }

    public String getAppNameByAppPath(String path) {
        String[] strs = path.split("\\/");
        int len = strs.length;
        if (len > 1) {
            // logd(strs[len - 4]);
            return strs[len - 1];
        }
        return path;
    }

    private void getInitAppDir(File file) {
        if (file.isDirectory()) {
            String filePath = file.getAbsolutePath();
            if (filePath.endsWith(FRAMEWORK_BASE_RES)) {
                App mAppDir = new App(FRAMEWORKS, file.getAbsolutePath());
                mAppDir.parserAppDir();
            } else if (filePath.endsWith(PACKAGES)) {
                dealDirectory(file);
            } else {
                logd("not treat current :dir:" + filePath);
            }
        }
    }

    private void dealDirectory(File file) {
        if (file.isDirectory()) {
            App appDir = null;
            String path = file.getAbsolutePath();
            // logd("dealDir file : " + path);
            if (path.endsWith(FRAMEWORK_BASE_PACKAGE)) {
                File[] files = file.listFiles();
                int size = files.length;
                for (int i = 0; i < size; i++) {
                    appDir = new App(files[i]);
                    appDir.parserAppDir();
                }
            } else if (path.endsWith(PACKAGES)) {
                File[] files = file.listFiles();
                int size = files.length;
                for (int i = 0; i < size; i++) {
                    dealDirectory(files[i]);
                }
            } else {
                String[] paths = path.split("\\/");
                int length = paths.length;
                if (length > 2 && paths[length - 2].equals(PACKAGES)) {
                    ArrayList<String> appNames = Config.getInstance().getAppNames();
                    File[] files = file.listFiles();
                    int size = files.length;
                    for (int i = 0; i < size; i++) {
                        if (appNames.contains(getAppNameByAppPath(files[i].getAbsolutePath()))) {
                            appDir = new App(files[i]);
                            appDir.parserAppDir();
                        }
                    }
                }
            }
        } else {
            logd("deal packages not is dir path is : " + file.getAbsolutePath());
        }
    }

    public static Utils getInstance() {
        if (mInstance == null) {
            mInstance = new Utils();
        }
        return mInstance;
    }

    public static boolean isEmpty(CharSequence str) {
        if (str == null || str.length() == 0)
            return true;
        else
            return false;
    }

    public File getXlsFile(String xlsPath) {
        File curFile = new File(xlsPath);
        if (curFile.exists()) {
            if (curFile.isFile()) {
                if (xlsPath.endsWith(SURFIX_XLS)) {
                    return curFile;
                }
            } else if (curFile.isDirectory()) {
                File[] files = curFile.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].getName().endsWith(SURFIX_XLS)) {
                        return files[i];
                    }
                }
            }
        }
        return null;
    }

    /*
     * delete all files which at path and create directory according path
     * param:path is directory's path
     */
    public static void initDir(String path) {
        if (!isEmpty(path)) {
            File file = new File(path);
            initDir(file);
        }
    }

    /*
     * delete all files at dirFile and create directory dirFile param:dirFile is
     * directory file
     */
    public static void initDir(File dirFile) {
        if (dirFile != null) {
            if (dirFile.exists() && dirFile.isDirectory()) {
                deleteFile(dirFile);
            }
            dirFile.mkdir();
        }
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
                // logd("delete file :" + file.getAbsolutePath() +
                // " ; delete successful : " +isSuccess);
            }
            file.delete();
        } else {
            boolean isSuccess = file.delete();
            // logd("delete file :" + file.getAbsolutePath()+
            // " ; delete successful : " + isSuccess);
        }
    }

    public static boolean isContainSpecialCharacter(String str) {
        boolean isSpecial = false;
        Pattern pat = Pattern.compile(REGULAR_SPECIAL_CHARS);
        Matcher mat = pat.matcher(str);
        isSpecial = mat.find();
        if (isSpecial) {
            return isSpecial;
        }

        if (str.indexOf("\\n") != -1) {
            return isSpecial;
        }

        if (str.indexOf('\u2026') != -1) {
            return isSpecial;
        }
        return isSpecial;
    }

    public static String replaceNewLine(String str) {
        String line = removeBlankAndQutation(str);
        if (isRepaceTabAndNewLine) {
            char[] chars = line.toCharArray();
            int len = chars.length;
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if (c == NEWLINE && i != len - 1) {
                    chars[i] = NEWLINE_REPLACE_CHAR;
                } else if (c == TAB && i != len - 1) {
                    chars[i] = NEWLINE_REPLACE_TAB;
                }
            }
            return new String(chars);
        } else {
            return line;
        }
    }

    /*
     * remove the string's blank and qutation
     */
    public static String removeBlankAndQutation(String str) {
        String line = str.trim();
        int length = line.length();
        if ((length > 1) && (line.charAt(0) == '"')
                && (line.charAt(length - 1) == '"')) {
            return line.substring(1, length - 1);
        }
        return line;
    }

    public static void printHelp() {
        loge("");
        loge("=========================================================");
        loge("USAGE:");
        loge("      ./android_string.sh " + Command.XLS.toString().toLowerCase()
                + " path [-a] [-t] [-o] [path] [-d] [-h]");
        loge("");
        loge(" this command will create a xxx.xls at xls direcotry according the path of user input!");
        loge("");
        loge("       path         the app dir (absolute path) which contains strings.xml or strings.xml");
        loge("       -a           the path is android root path : only find strings.xml on packages/apps, frameworks/base/core/res/res, frameworks/packages");
        loge("       -t           all strings of strings.xml(include translatable=false)");
        loge("       -o path      define output dir");
        loge("       -d           print some debug infos");
        loge("       -h           print help");
        loge("");
        loge("");
        loge("      ./android_string.sh " + Command.XML.toString().toLowerCase()
                + " [path] [-r] [-o path] [-d] [-h] [-x path]");
        loge("");
        loge(" this command"
                + " will create some string.xml at output dirctory(default is xml which you can changed by param -o path)"
                + " from the xxx.xls which in current directory or the params path!");
        loge("");
        loge("       path         the app dir (absolute path) which contains strings.xml or strings.xml can absent");
        loge("       -x path      the dir which has a file *.xls(the current dir if absend)");
        loge("       -r           all strings of strings.xml(include translatable=false)");
        loge("       -o path      define output dir");
        loge("       -d           print some debug infos");
        loge("       -h           print help");
        loge("");
        loge("");
        loge("some times you can find an error.txt int strings.xml dir,it records some error when write strings.xml");
        loge("=========================================================");
        loge("");
    }

    public static void logd(String str) {
        if (DEBUG) {
            System.out.println("    " + str);
        }
    }

    public static void loge(String str) {
        System.out.println("    " + str);
    }
}
