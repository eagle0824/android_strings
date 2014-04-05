import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class StrHandler extends DefaultHandler{
    private ArrayList<StringObj> mStrObjs = new ArrayList<StringObj>();
    private StringBuilder sb;
    private StringObj strObj;
    private StringArray strArray;
    private StringPlurals strPlurals;
    private ArrayList<String> items;
    private LinkedHashMap<String, String> mPlurasItems;
    private boolean isItemHasAttr = false;
    private String pluralsKey = null;
    private boolean mIncludeNotTranslate = false;
    private AppDir mAppDir;
    private StringsFile mStrFile;

    public StrHandler(StringsFile strFile, Command cmd,AppDir appDir) {
        mStrFile = strFile;
        mStrObjs.clear();
        mIncludeNotTranslate = cmd.isIncludeNotTranslate;
        mAppDir = appDir;

    }

    @Override
    public void startDocument() throws SAXException {
        // logd("start document");
        sb = new StringBuilder();
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName,
            Attributes attrs)
            throws SAXException {
        // Utils.logd("startElement qName is : " + qName);
        if (qName.equals("string")) {
            sb.delete(0, sb.length());
            int nameIndex = attrs.getIndex(Utils.ATTR_NAME);
            int translatableIndex = attrs.getIndex(Utils.ATTR_TRANSLATABLE);
            if (translatableIndex != -1) {
                boolean isTranslate = Boolean.valueOf(attrs.getValue(Utils.ATTR_TRANSLATABLE));
                if (!isTranslate && !mIncludeNotTranslate) {
                    strObj = null;
                    return;
                }
            }
            int translateIndex = attrs.getIndex(Utils.ATTR_TRANSLATE);
            if (translateIndex != -1) {
                boolean isTranslate = Boolean.valueOf(attrs.getValue(Utils.ATTR_TRANSLATE));
                if (!isTranslate && !mIncludeNotTranslate) {
                    strObj = null;
                    return;
                }
            }
            int producIndex = attrs.getIndex(Utils.ATTR_PRODUCT);
            if(producIndex!=-1){
                String productId = attrs.getValue(nameIndex)+"\" product=\""+ attrs.getValue(producIndex);
                strObj = new StringObj(productId);
            }else{
                strObj = new StringObj(attrs.getValue(nameIndex));
            }
        } else if (qName.equals("string-array")/* || qName.equals("array") */) {
            sb.delete(0, sb.length());
            int nameIndex = attrs.getIndex(Utils.ATTR_NAME);
            int translatableIndex = attrs.getIndex(Utils.ATTR_TRANSLATABLE);
            if (translatableIndex != -1) {
                boolean isTranslate = Boolean.valueOf(attrs.getValue(Utils.ATTR_TRANSLATABLE));
                if (!isTranslate && !mIncludeNotTranslate) {
                    strArray = null;
                    return;
                }
            }
            int translateIndex = attrs.getIndex(Utils.ATTR_TRANSLATE);
            if (translateIndex != -1) {
                boolean isTranslate = Boolean.valueOf(attrs.getValue(Utils.ATTR_TRANSLATE));
                if (!isTranslate && !mIncludeNotTranslate) {
                    strArray = null;
                    return;
                }
            }
            strArray = new StringArray(attrs.getValue(nameIndex));
            items = new ArrayList<String>();
        } else if (qName.equals("plurals")) {
            sb.delete(0, sb.length());
            int nameIndex = attrs.getIndex(Utils.ATTR_NAME);
            strPlurals = new StringPlurals(attrs.getValue(nameIndex));
            mPlurasItems = new LinkedHashMap<String, String>();
        } else if (qName.equals("item")) {
            sb.delete(0, sb.length());
            if (attrs.getLength() > 0) {
                int quantityIndex = attrs.getIndex(Utils.QUANTITY);
                if (quantityIndex != -1) {
                    isItemHasAttr = true;
                    pluralsKey = attrs.getValue(quantityIndex);
                }else{
                    isItemHasAttr = false;
                }
            } else {
                isItemHasAttr = false;
            }
        } else if (qName.equals("xliff:g") || qName.equals("font") || qName.equals("a")
                || qName.equals("img")) {
            sb.append("<").append(qName);
            int len = attrs.getLength();
            if (len > 0) {
                for (int i = 0; i < len; i++) {
                    sb.append(" " + attrs.getLocalName(i) + "=\"" + attrs.getValue(i) + "\"");
                }
            }
            sb.append(">");
        } else if (qName.equals("b") || qName.equals("li") || qName.equals("i")
                || qName.equals("u") || qName.equals("ignore") || qName.equals("p")) {
            sb.append("<").append(qName).append(">");
        } else {
            if (!qName.equals("resources") && !qName.equals("skip")) {
                Utils.logd("startElement not treat this element : " + qName);
            }
        }

    }

    @Override
    public void characters(char[] chars, int start, int length) throws SAXException {
        sb.append(chars, start, length);
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        // logd("endElement qName is : " + qName);
        if (qName.equals("string")) {
            if (strObj != null) {
                strObj.mValue = Utils.replaceNewLine(sb.toString());
                mStrObjs.add(strObj);
                strObj = null;
            }
            sb.delete(0, sb.length());
        } else if (qName.equals("string-array")/* || qName.equals("array") */) {
            if (strArray != null && items != null) {
                strArray.setValues(items);
                mStrObjs.add(strArray);
            }
            items = null;
            strArray = null;
            sb.delete(0, sb.length());
        } else if (qName.equals("plurals")) {
            if (strPlurals != null && mPlurasItems != null) {
                strPlurals.setValues(new LinkedHashMap<String, String>(mPlurasItems));
                mStrObjs.add(strPlurals);
            }
            items = null;
            strPlurals = null;
            sb.delete(0, sb.length());
        } else if (qName.equals("item")) {
            if (isItemHasAttr && pluralsKey != null) {
                if (mPlurasItems != null) {
                    // Utils.logd("pural key :" + pluralsKey + " value : " +
                    // sb.toString());
                    mPlurasItems.put(pluralsKey, Utils.replaceNewLine(sb.toString()));
                }
                pluralsKey = null;
                isItemHasAttr = false;
            } else {
                if (items != null) {
                    items.add(Utils.replaceNewLine(sb.toString()));
                }
            }
            sb.delete(0, sb.length());
        } else if (qName.equals("xliff:g") || qName.equals("font") || qName.equals("a")
                || qName.equals("img")) {
            sb.append("</").append(qName).append(">");
        } else if (qName.equals("b") || qName.equals("li") || qName.equals("i")
                || qName.equals("u") || qName.equals("ignore") || qName.equals("p")) {
            sb.append("</").append(qName).append(">");
        } else {
            if (!qName.equals("resources") && !qName.equals("skip")) {
                Utils.logd("endElement not treat this element : " + qName);
            }
        }
    }

    @Override
    public void endDocument() throws SAXException {
        // logd("end document");
        /*
         * for (StringObj strObj : mStrObjs) { logd(strObj.toString()); }
         * logd("key"); for (StringObj strObj : mStrObjs) {
         * logd(strObj.getAllId()); } logd("value"); for (StringObj strObj :
         * mStrObjs) { logd(strObj.getAllValue()); }
         */
        // if (mSavePath != "") {
        // mSavePath = mSavePath + "/";
        // }
        // File ids = new File(mSavePath + "ids.txt");
        // File values = new File(mSavePath + "values.txt");
        // File id_values = new File(mSavePath + "id_value.txt");
        // if (!mAppend) {
        // ids.delete();
        // values.delete();
        // id_values.delete();
        // }
        // // logd("write id_values.txt");
        // try {
        // FileWriter fileWriter = new FileWriter(id_values, mAppend);
        // fileWriter.write(mFilePath + Utils.NEWLINE);
        // for (StringObj strObj : mStrObjs) {
        // fileWriter.write(strObj.toString());
        // }
        // fileWriter.flush();
        // fileWriter.close();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        // // logd("write ids.txt");
        // try {
        // FileWriter fileWriter = new FileWriter(ids, mAppend);
        // fileWriter.write(mFilePath + Utils.NEWLINE);
        // for (StringObj strObj : mStrObjs) {
        // fileWriter.write(strObj.getAllId() + Utils.NEWLINE);
        // }
        // fileWriter.flush();
        // fileWriter.close();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        // // logd("write values.txt");
        // try {
        // FileWriter fileWriter = new FileWriter(values, mAppend);
        // fileWriter.write(mFilePath + Utils.NEWLINE);
        // for (StringObj strObj : mStrObjs) {
        // fileWriter.write(strObj.getAllValue() + Utils.NEWLINE);
        // }
        // fileWriter.flush();
        // fileWriter.close();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        mStrFile.setArrayStrs(mStrObjs);
        //Utils.logd("String file : " + mStrFile.getDirName());
        if(mAppDir!=null){
            mAppDir.addStringFile(mStrFile);
        }
    }
}
