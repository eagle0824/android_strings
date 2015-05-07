package main.java.com.eagle.mode;
import main.java.com.eagle.Utils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Set;

public class StringPlurals extends StringObj{

    public LinkedHashMap<String, String> mValues;

    public StringPlurals(String id) {
        super(id);
        mValues = new LinkedHashMap<String, String>();
    }

    public void setValues(LinkedHashMap<String, String> map) {
        mValues = map;
    }

    public String[] getAllIds() {
        StringBuilder sb = new StringBuilder();
        Set<String> keys = mValues.keySet();
        String[] ids = new String[keys.size()];
        int index = 0;
        for (String key : keys) {
            ids[index] = sb.append(Utils.SURFIX_PLURALS).append(mId).append(Utils.SEPERATOR)
                    .append(key)
                    .toString();
            sb.delete(0, sb.length());
            index += 1;
        }
        //Utils.logd("ids " + Arrays.toString(ids));
        return ids;
    }

    public String[] getAllValues() {
        Set<String> keys = mValues.keySet();
        String[] values = new String[keys.size()];
        int index = 0;
        for (String key : keys) {
            values[index] = mValues.get(key);
            index++;
        }
        return values;
    }

    /*
     * (non-Javadoc)
     * @see StringObj#getXMLFormatStr(java.lang.String[]) <plurals
     * name="listTotalAllContacts"> <item quantity="one">Displaying 1
     * contact</item> <item quantity="other">Displaying <xliff:g
     * id="count">%d</xliff:g> contacts</item> </plurals>
     */
    public FormatResult getXMLFormatStr(String[] values) {
        boolean isError = false;
        int length = mValues.size();
        StringBuilder sb = new StringBuilder();
        if (values == null || values.length != length) {
            isError = true;
            sb.append("id is : ")
                    .append(mId)
                    .append("array count is : " + length);
            if (values == null) {
                sb.append(" ; input values is : null");
            } else {
                sb.append(" ; input values is : ")
                        .append(Arrays.toString(values));
            }
            sb.append(Utils.NEWLINE);
        } else {
            sb.append(Utils.PLURALS_HEAD_PREFIX)
                    .append(mId)
                    .append(Utils.PLURALS_HEAD_SURFIX);
            for (int i = 0; i < length; i++) {
                String key = getKeyByIndex(i);
                if (key.equals("")) {
                    isError = true;
                    sb.delete(0, sb.length());
                    sb.append("StringPlurals id ")
                            .append(mId)
                            .append(" ; item quantity is empty ; value is ")
                            .append(values[i]);
                    return new FormatResult(isError, sb.toString());
                }
                sb.append(Utils.PLURALS_ITEM_PREFIX)
                        .append(key)
                        .append(Utils.PLURALS_MIDDLE_PREFIX)
                        .append(values[i])
                        .append(Utils.PLURALS_ITEM_SUFFIX);
            }
            sb.append(Utils.PLURALS_END);
        }
        return new FormatResult(isError, sb.toString());
    }

    public String getKeyByIndex(int index) {
        int i = 0;
        for (String key : mValues.keySet()) {
            if (i == index) {
                return key;
            }
            i++;
        }
        return "";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // sb.append(mId);
        Set<String> keys = mValues.keySet();
        for (String key : keys) {
            sb.append(Utils.SURFIX_PLURALS).append(mId).append("-").append(key)
                    .append(Utils.SEPERATOR)
                    .append(mValues.get(key)).append(Utils.NEWLINE);
        }
        return sb.toString();
    }
}