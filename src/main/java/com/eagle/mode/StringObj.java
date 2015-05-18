
package main.java.com.eagle.mode;

import main.java.com.eagle.Utils;

import java.util.Arrays;

public class StringObj {
    public String mId;
    public String mValue;

    public StringObj(String id) {
        mId = id;
    }

    public String[] getAllIds() {
        return new String[] {
                mId
        };
    }

    public String[] getAllValues() {
        return new String[] {
                mValue
        };
    }

    public FormatResult getXMLFormatStr(String[] values) {
        boolean isError = false;
        StringBuilder sb = new StringBuilder();
        if (values == null || values.length != 1) {
            isError = true;
            sb.append("id is : ")
                    .append(mId);
            if (values == null) {
                sb.append(" ; input values is : null");
            } else {
                sb.append(" ; input values is : ")
                        .append(Arrays.toString(values));
            }
            sb.append(Utils.NEWLINE);
        } else {
            sb.append(Utils.STRINT_RECORD_PREFIX)
                    .append(mId)
                    .append(Utils.STRINT_RECORD_MIDDLE)
                    .append(values[0])
                    .append(Utils.STRINT_RECORD_SURFIX);
        }
        return new FormatResult(isError, sb.toString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mId).append(Utils.SEPERATOR).append(mValue).append(Utils.NEWLINE);
        return sb.toString();
    }

    public class FormatResult {

        public boolean mIsError = false;
        public String mXmlString = "";

        public FormatResult(boolean isError, String xmlString) {
            mIsError = isError;
            mXmlString = xmlString;
        }
    }
}
