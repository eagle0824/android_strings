import java.util.ArrayList;
import java.util.Arrays;


public class StringArray extends StringObj{

    public ArrayList<String> mValues;

    public StringArray(String id) {
        super(id);
        mValues = new ArrayList<String>();
    }

    public String[] getAllIds() {
        int len = mValues.size();
        StringBuilder sb = new StringBuilder();
        String[] ids = new String[len];
        for (int i = 0; i < len; i++) {
            ids[i] = sb.append(Utils.SURFIX_ARRAY).append(mId).toString();
            sb.delete(0, sb.length());
        }
        //Utils.logd("ids " + Arrays.toString(ids));
        return ids;
    }

    public String[] getAllValues() {
        int len = mValues.size();
        String[] values = new String[len];
        return mValues.toArray(values);
    }

    public void setValues(ArrayList<String> mList) {
        mValues = mList;
    }

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
            sb.append(Utils.STRING_ARRAY_HEAD_PREFIX)
                    .append(mId)
                    .append(Utils.STRING_ARRAY_HEAD_SURFIX);
            for (int i = 0; i < length; i++) {
                sb.append(Utils.ITEM_PREFIX)
                        .append(values[i])
                        .append(Utils.ITEM_SUFFIX);
            }
            sb.append(Utils.STRING_ARRAY_END);
        }
        return new FormatResult(isError, sb.toString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // sb.append(mId);
        int len = mValues.size();
        for (int i = 0; i < len; i++) {
            sb.append(Utils.SURFIX_ARRAY).append(mId).append(Utils.SEPERATOR)
                    .append(mValues.get(i))
                    .append(Utils.NEWLINE);
        }
        return sb.toString();
    }

}
