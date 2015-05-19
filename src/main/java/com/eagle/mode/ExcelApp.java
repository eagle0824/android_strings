
package main.java.com.eagle.mode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class ExcelApp {

    private String mAppName;
    private HashMap<String, ArrayList<ExcelRecord>> mStringsRecords;
    private String mCurrentStringsFile;

    public ExcelApp(String appName) {
        mAppName = appName;
        mStringsRecords = new HashMap<String, ArrayList<ExcelRecord>>();
    }

    public void addStringsFileName(String name) {
        if (!mStringsRecords.containsKey(name)) {
            mCurrentStringsFile = name;
            mStringsRecords.put(name, new ArrayList<ExcelRecord>());
        }
    }

    public void setAppName(String name) {
        mAppName = name;
    }

    public String getAppName() {
        return mAppName;
    }

    public void add(ExcelRecord record) {
        record.setStringsFileName(mCurrentStringsFile);
        mStringsRecords.get(mCurrentStringsFile).add(record);
    }

    public int getRecordCount() {
        int count = 0;
        for (ArrayList<ExcelRecord> records : mStringsRecords.values()) {
            count += records.size();
        }
        return count;
    }

    public ExcelRecord getRecordById(String fileName, String id) {
        for (ExcelRecord record : mStringsRecords.get(fileName)) {
            String idValue = record.getIdValue();
            if (idValue != null && !idValue.equals("") && idValue.equals(id)) {
                return record;
            }
        }
        return null;
    }

    public ExcelRecord getRecordById(String id) {
        for (ArrayList<ExcelRecord> records : mStringsRecords.values()) {
            for (ExcelRecord record : records) {
                String idValue = record.getIdValue();
                if (idValue != null && !idValue.equals("") && idValue.equals(id)) {
                    return record;
                }
            }
        }
        return null;
    }

    public ExcelRecord[] getRecordById(String fileName, String[] ids) {
        int idsCount = ids.length;
        ExcelRecord[] results = new ExcelRecord[idsCount];
        int idsIndex = 0;
        for (ExcelRecord record : mStringsRecords.get(fileName)) {
            String idValue = record.getIdValue();
            if (idValue != null && !idValue.equals("") && idValue.equals(ids[idsIndex])) {
                results[idsIndex] = record;
                idsIndex++;
            }
            if (idsIndex == idsCount) {
                return results;
            }
        }
        if (results[0] != null) {
            return results;
        }
        return null;
    }

    public ExcelRecord[] getRecordById(String[] ids) {
        int idsCount = ids.length;
        ExcelRecord[] results = new ExcelRecord[idsCount];
        for (ArrayList<ExcelRecord> records : mStringsRecords.values()) {
            int idsIndex = 0;
            for (ExcelRecord record : records) {
                String idValue = record.getIdValue();
                if (idValue != null && !idValue.equals("") && idValue.equals(ids[idsIndex])) {
                    results[idsIndex] = record;
                    idsIndex++;
                }
                if (idsIndex == idsCount) {
                    return results;
                }
            }
        }
        if (results[0] != null) {
            return results;
        }
        return null;
    }

    public ArrayList<ExcelRecord> getRecordsByFileName(String fileName) {
        return mStringsRecords.get(fileName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AppName " + mAppName + " ")
                .append("Rcord count : ")
                .append(getRecordCount())
                .append("\n");
        Set<String> fileNames = mStringsRecords.keySet();
        for (String name : fileNames) {
            ArrayList<ExcelRecord> records = mStringsRecords.get(name);
            sb.append(name).append("\n");
            for (ExcelRecord record : records) {
                sb.append(record.toString()).append("\n");
            }
        }
        sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }

    public HashMap<String, ArrayList<ExcelRecord>> getStringsRecords() {
        return mStringsRecords;
    }

}
