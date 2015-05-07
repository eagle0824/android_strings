
package main.java.com.eagle.config;

import main.java.com.eagle.config.Config.ConfigType;

public class ConfigItem {

    private String mKey;
    private int mIndex;
    private String mValue;
    private ConfigType mType;

    public ConfigItem(ConfigType type) {
        mType = type;
    }

    public void setKey(String key) {
        mKey = key;
    }

    public void setIndex(int index) {
        mIndex = index;
    }

    public void setValue(String value) {
        mValue = value;
    }

    public String getKey() {
        return mKey;
    }

    public int getIndex() {
        return mIndex;
    }

    public String getValue() {
        return mValue;
    }

    public ConfigType getType() {
        return mType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mKey).append(" ").append(mIndex).append(" ").append(mValue);
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ConfigItem) {
            ConfigItem item = (ConfigItem) obj;
            return mType == item.getType() && mIndex == item.getIndex()
                    && mKey.equals(item.getKey())
                    && mValue.equals(item.getValue());
        }
        return false;
    }
}
