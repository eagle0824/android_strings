public class Command {

    public String savePath;
    public boolean isCW;
    public boolean isIncludeNotTranslate;
    public boolean isBuildPath;
    private static Command instance;
    public String mPath;

    private Command() {
    }

    public static Command getInstace() {
        if (instance == null) {
            instance = new Command();
        }
        return instance;
    }

    public void resetCommand(){
        savePath="";
        isCW = false;
        isIncludeNotTranslate = false;
        isBuildPath = false;
        mPath = "";
    }
}
