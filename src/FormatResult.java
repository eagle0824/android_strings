public class FormatResult {

    boolean mIsError = false;
    String mXmlString = "";

    public FormatResult(boolean isError, String xmlString) {
        mIsError = isError;
        mXmlString = xmlString;
    }
}
