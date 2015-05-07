package main.java.com.eagle.mode;
public class FormatResult {

    public boolean mIsError = false;
    public String mXmlString = "";

    public FormatResult(boolean isError, String xmlString) {
        mIsError = isError;
        mXmlString = xmlString;
    }
}
