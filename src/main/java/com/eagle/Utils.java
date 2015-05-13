
package main.java.com.eagle;

import main.java.com.eagle.config.Config;
import main.java.com.eagle.config.Config.Command;
import main.java.com.eagle.mode.App;
import main.java.com.eagle.mode.StringsFile;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static boolean DEBUG = false;

    public static final String STRING_HEAD = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\n" +
            "<resources xmlns:android=\"http://schemas.android.com/apk/res/android\"" + "\n" +
            "    xmlns:xliff=\"urn:oasis:names:tc:xliff:document:1.2\">\n";
    public static final String STRING_END = "</resources>";

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
    public static final String VALUES = "values";
    public static final String FRAMEWORK_BASE_PACKAGE = "frameworks/base/packages";
    public static final String FRAMEWORK_BASE_RES = "frameworks/base/core/res";
    public static final String PACKAGES = "packages";

    private static final String REGULAR_SPECIAL_CHARS = ".*<.*>.*";

    public static final String SHEET0_NAME = "多国语言";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_TRANSLATABLE = "translatable";
    public static final String ATTR_TRANSLATE = "translate";
    public static final String ATTR_PRODUCT = "product";

    public static final String MANIFEST_FILE_NAME = "AndroidManifest.xml";

    public static final boolean isRepaceTabAndNewLine = false;

    private static Utils mInstance;

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
            File xlsFile = new File(cmd.getOutputPath(), NEW_XLS_NAME);
            deleteFile(xlsFile);
            loge("begin create xls. Please wait ...");
            initDir(cmd.getOutputPath());
            if (file.isFile()) {
                parserStringFiles(file);
            } else if (file.isDirectory()) {
                ArrayList<String> allApps = findAppDirsByRoot(file, cmd.isBuildPath());
                if(allApps.size() > 0){
                    App app = null;
                    for(String appDir : allApps){
                        loge("app dir : " + appDir);
                        app = new App(appDir);
                        app.parserAppDir();
                    }
                }
            }
            loge("create " + xlsFile.getPath() + " successful!");
        } else {
            loge("create xls failed, because " + file.getPath() + " not exist!");
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
                ArrayList<String> stringsPaths = new ArrayList<String>();
                loge("find en strings.xml file.Please wait ...");
                stringsPaths = findEnStringsFilesByRoot(file, cmd.isBuildPath());
                int size = stringsPaths.size();
                loge("find en strings.xml file finished! app size : " + size);
                if (size > 0) {
                    ExcelHelper mExcelHelper = new
                            ExcelHelper(cmd.getOutputPath());
                    mExcelHelper.createXmlFromXlsFile(xlsFile);
                    for (int i = 0; i < size; i++) {
                        logd(stringsPaths.get(i));
                        StringsFile strFile = new
                                StringsFile(stringsPaths.get(i));
                        strFile.doParserStringsFile();
                        mExcelHelper.createXmlByStringsFile(strFile);
                    }
                }
            } else {
                loge("File " + file.getPath() + "not exist!");
                return;
            }
        } else {
            loge("begin create strings.xml...");
            ExcelHelper mExcelHelper = new ExcelHelper(cmd.getOutputPath());
            mExcelHelper.createXmlFromXlsFile(xlsFile);
            loge("finished create strings.xml on dir " + cmd.getOutputPath());
        }
    }

    private ArrayList<String> findEnStringsFilesByRoot(File file, boolean buildPath) {
        ArrayList<String> stringsFiles = new ArrayList<String>();
        ArrayList<String> apps = findAppDirsByRoot(file, buildPath);
        int appSize = apps.size();
        if (appSize > 0){
            ArrayList<String> appNames = Config.getInstance().getAppNames();
            String appPath = "";
            for(int i=0; i<appSize; i++){
                appPath = apps.get(i);
                String appName = Utils.getAppName(appPath);
                if (appNames.size() > 0) {
                    if (appNames.contains(appName)) {
                        stringsFiles.addAll(getAppStringsFiles(appPath, true));
                    } else if (appName.equals(RES)) {// frameworks-res
                        stringsFiles.addAll(getAppStringsFiles(appPath, true));
                    }
                } else {
                    stringsFiles.addAll(getAppStringsFiles(appPath, true));
                }
            } 
        }
        return stringsFiles;
    }

    private ArrayList<String> findAppDirsByRoot(File file, boolean buildPath){
        ArrayList<String> apps = new ArrayList<String>();
        if (buildPath){
            ArrayList<String> baseDirs = Config.getInstance().getBaseDirs();
            for (String dir : baseDirs) {
                findAppDirs(new File(file, dir), apps);
            }
        } else {
            findAppDirs(file, apps);
        }
        return apps;
    }

    private void findAppDirs(File file, ArrayList<String> apps) {
        String path = file.getAbsolutePath();
        if (file.isDirectory()) {
            if (Utils.isAppDir(path)) {
                apps.add(file.getAbsolutePath());
            } else {
                File[] subDirs = file.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        String name = pathname.getName();
                        if (pathname.isDirectory() && !name.startsWith(".") && !name.equals("jni")
                                && !name.equals("native") && !name.equals("tests") && !name.equals("bin")) {
                            return true;
                        }
                        return false;
                    }
                });
                if (subDirs != null && subDirs.length > 0) {
                    for (File dir : subDirs) {
                        findAppDirs(dir, apps);
                    }
                }
            }
        } else {
            loge("findAppDirs " + path + " is not dir!");
        }
    }

    private ArrayList<String> getAppStringsFiles(String appDir, boolean onlyEnString) {
        ArrayList<String> stringsFiles = new ArrayList<String>();
        File appFile = new File(appDir);
        File[] resFiles = appFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (dir.isDirectory() && name.equals(RES)) {
                    return true;
                }
                return false;
            }
        });
        if (resFiles != null && resFiles.length == 1) {
            ArrayList<String> defaultStringsName = Config.getInstance().getStringFiles();
            int size = defaultStringsName.size();
            if (onlyEnString) {
                for (int i = 0; i < size; i++) {
                    String name = defaultStringsName.get(i);
                    File stringsFile = new File(resFiles[0], VALUES + "/" + name);
                    if (stringsFile.exists() && stringsFile.isFile()) {
                        stringsFiles.add(stringsFile.getAbsolutePath());
                    }
                }
            } else {
                File[] valuesFiles = appFile.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        if (dir.isDirectory() && name.contains(VALUES)) {
                            return true;
                        }
                        return false;
                    }
                });
                if (valuesFiles != null && valuesFiles.length > 0) {
                    for (File valuesDir : valuesFiles) {
                        for (int i = 0; i < size; i++) {
                            String name = defaultStringsName.get(i);
                            File stringsFile = new File(valuesDir, name);
                            if (stringsFile.exists() && stringsFile.isFile()) {
                                stringsFiles.add(stringsFile.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        }
        logd("getAppStringsFiles > " + appDir + " strings count : " + stringsFiles.size());
        return stringsFiles;
    }



    private void parserStringFiles(File file) {
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
        } else {
            loge(path + " is not file path!");
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

    public static boolean isAppDir(String dirPath) {
        File file = new File(dirPath);
        if (file.isDirectory()) {
            File[] subFiles = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isFile() && pathname.getName().equals(MANIFEST_FILE_NAME)) {
                        return true;
                    }
                    return false;
                }
            });
            if (subFiles != null && subFiles.length == 1) {
                return true;
            }
        }
        return false;
    }

    public static String getAppName(File file) {
        String filePath = file.getAbsolutePath();
        return getAppName(filePath);
    }

    public static String getAppName(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            if (file.isDirectory()) {
                String name = "";
                if (isAppDir(filePath)) {
                    name = file.getName();
                }
                while (isEmpty(name)) {
                    file = file.getParentFile();
                    if (file == null || isEmpty(file.getAbsolutePath())
                            || file.getAbsolutePath().equals("/")) {
                        break;
                    }
                    name = getAppName(file);
                }
                return name;
            } else if (file.isFile()) {
                File parent = file.getParentFile();
                if (parent != null) {
                    return getAppName(parent);
                }
            }
        }
        return "";
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
        loge("       -a           the path is android root path : only find strings.xml on packages/apps, frameworks/base/core/res/res, frameworks/packages");
        loge("       -x path      the dir which has a file *.xls(the current dir if absend)");
        loge("       -t           all strings of strings.xml(include translatable=false)");
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
