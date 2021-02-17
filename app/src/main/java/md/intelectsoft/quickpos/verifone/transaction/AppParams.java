package md.intelectsoft.quickpos.verifone.transaction;
/*
 *  author: Derrick
 *  Time: 2019/5/27 16:38
 */

public class AppParams {
    private static AppParams instance;
    private AppParams(){

    }
    public static AppParams getInstance(){
        if ( instance == null){
            instance = new AppParams();
        }
        return instance;
    }

    public void initAppParam(){
        getInstance();
        instance.setOffline(true);
        instance.setShowScanBorder(true);
        instance.setSupportInsert(true);
        instance.setSupportTap(true);
        instance.setSupportSwipe(true);
    }

    private boolean Offline;
    private boolean ShowScanBorder;
    private boolean supportTap;
    private boolean supportInsert;
    private boolean supportSwipe;

    public boolean isOffline() {
        return Offline;
    }

    public void setOffline(boolean offline) {
        Offline = offline;
    }

    public boolean isShowScanBorder() {
        return ShowScanBorder;
    }

    public void setShowScanBorder(boolean showScanBorder) {
        ShowScanBorder = showScanBorder;
    }

    public boolean isSupportTap() {
        return supportTap;
    }

    public void setSupportTap(boolean supportTap) {
        this.supportTap = supportTap;
    }

    public boolean isSupportInsert() {
        return supportInsert;
    }

    public void setSupportInsert(boolean supportInsert) {
        this.supportInsert = supportInsert;
    }

    public boolean isSupportSwipe() {
        return supportSwipe;
    }

    public void setSupportSwipe(boolean supportSwipe) {
        this.supportSwipe = supportSwipe;
    }
}
