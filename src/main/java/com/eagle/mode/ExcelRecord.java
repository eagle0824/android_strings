
package main.java.com.eagle.mode;

import main.java.com.eagle.Utils;

import java.util.ArrayList;

public class ExcelRecord {

    private ArrayList<String> mItems;
    private int mLine;
    private String mStringsFileName;

    public ExcelRecord(int line) {
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
