
package main.java.com.eagle.mode;

import main.java.com.eagle.Utils;
import main.java.com.eagle.config.Config;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

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
public class App {
    /*
     * mStringFiles is collection sub values name and sub values dir String is
     * values-* dir name StringFile is
     */
    private HashMap<String, StringsFolder> mStringsFolders;

    /*
     * mPath is The appDir path
     */
    private String mPath;

    private String mName;

    public enum CellType {
        NORMAL, TITLE, APP_NAME, SPECIAL
    }

    public App(String appDir) {
        mPath = appDir;
        mName = Utils.getAppName(mPath);
    }

    public App(String name, String path) {
        mPath = path;
        mName = Utils.getAppName(mPath);
    }

    public void parser(String folder) {
        Collection<StringsFolder> stringsFolders = getStringsFolders();
        for (StringsFolder stringsFolder : stringsFolders) {
            if (stringsFolder.getName().equals(folder)) {
                stringsFolder.parser();
            }
        }
    }

    public void parser() {
        Utils.loge(String.format("app : %s path : %s", mName, mPath));
        Collection<StringsFolder> stringsFolders = getStringsFolders();
        for (StringsFolder stringsFolder : stringsFolders) {
            stringsFolder.parser();
        }
        // dumpStringsFiles();
    }

    private Collection<StringsFolder> getStringsFolders() {
        return getStringsFoldersHashMap().values();
    }

    public HashMap<String, StringsFolder> getStringsFoldersHashMap() {
        if (mStringsFolders == null) {
            mStringsFolders = findAppStringsFolders(mPath);
        }
        return mStringsFolders;
    }

    public HashMap<String, StringsFolder> findAppStringsFolders(String appDir) {
        HashMap<String, StringsFolder> stringsFolders = new HashMap<String, StringsFolder>();
        File appFile = new File(appDir);
        File[] resFiles = appFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (dir.isDirectory() && name.equals(Utils.RES)) {
                    return true;
                }
                return false;
            }
        });
        if (resFiles != null && resFiles.length == 1) {
            File[] valuesFiles = resFiles[0].listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (dir.isDirectory() && name.contains(Utils.VALUES)) {
                        return true;
                    }
                    return false;
                }
            });
            if (valuesFiles != null && valuesFiles.length > 0) {
                Set<String> mKeys = Config.getInstance().getLanguages().keySet();
                for (File valuesDir : valuesFiles) {
                    if (mKeys.contains(valuesDir.getName())) {
                        stringsFolders.put(valuesDir.getName(),
                                new StringsFolder(valuesDir.getAbsolutePath(), this));
                    }
                }
            }
        }
        return stringsFolders;
    }

    private void dumpStringsFiles() {
        Set<String> keySet = mStringsFolders.keySet();
        for (String key : keySet) {
            Utils.loge(String.format("key : %s StringsFile path %s ", key,
                    mStringsFolders.get(key).getPath()));
        }
    }

    public String getPath() {
        return mPath;
    }

    public String getName() {
        return mName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof App) {
            App o = (App) obj;
            return getPath().equals(o.getPath());
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append("name : ").append(getName());
        sBuilder.append("path : ").append(getPath());
        return sBuilder.toString();
    }

}
