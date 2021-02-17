package md.intelectsoft.quickpos.verifone.transaction.canvas_printer;

/**
 * Created by Simon on 2019/2/27.
 */


/**
 * this is for ALL items defined
 *
 *
 */
public enum PrinterItem {

    // parameters: type, description (file name for Logo), string value, integer value, print style of description, print style of value
    // print style of description, print style of value, default font size 16, alignment left
    LOGO        (PrinterItemType.LOGO_ASSETS, new PrinterElement("verifone_logo.jpg"), new PrinterElement() ),
    TITLE       (PrinterItemType.STRING, new PrinterElement("Verifone X900", 32 , PrinterDefine.PStyle_align_center), new PrinterElement()),
    SUBTITLE    (PrinterItemType.STRING, new PrinterElement("", 20, PrinterDefine.PStyle_align_right ), new PrinterElement()),
    COPY_NOTE   (PrinterItemType.STRING, new PrinterElement("", 20, PrinterDefine.PStyle_align_left ), new PrinterElement("merchant copy", 16)),
    MERCHANT_NAME(PrinterItemType.STRING, new PrinterElement("Merchant Name",24,PrinterDefine.PStyle_align_left),  new PrinterElement("Verifone", + 32, PrinterDefine.PStyle_align_center , PrinterDefine.Font_Bold)), // +32 + PStyle_force_multi_lines
    MERCHANT_ID (PrinterItemType.STRING, new PrinterElement("Merchant ID"),  new PrinterElement("", 32)),
    TERMINAL_ID (PrinterItemType.STRING, new PrinterElement("Terminal ID",20),  new PrinterElement( )),
    OPERATOR_ID (PrinterItemType.STRING, new PrinterElement("Operator ID",20),  new PrinterElement( )),
    HOST        (PrinterItemType.STRING, new PrinterElement("HOST", 20),  new PrinterElement()),
    TRANS_TYPE  (PrinterItemType.STRING, new PrinterElement("", 20 ,PrinterDefine.PStyle_align_left),
            new PrinterElement("", 28, PrinterDefine.PStyle_align_center , PrinterDefine.Font_Bold )),
    CARD_ISSUE  (PrinterItemType.STRING, new PrinterElement("Card Issue", 20),  new PrinterElement() ),
    CARD_NO     (PrinterItemType.STRING, new PrinterElement("CARD NO.",  20),  new PrinterElement("",28, PrinterDefine.PStyle_align_center , 0, PrinterDefine.Font_Bold) , true ), //+ PStyle_align_center + PStyle_force_multi_lines),
    CARD_TYPE   (PrinterItemType.STRING, new PrinterElement("Card Type", 20),  new PrinterElement() ),
    CARD_HOLDER (PrinterItemType.STRING, new PrinterElement("Card Holder", 20),  new PrinterElement() ),
    CARD_VALID  (PrinterItemType.STRING, new PrinterElement("Card Valid", 20),  new PrinterElement() ),
    BATCH_NO    (PrinterItemType.STRING, new PrinterElement("BATCH #", 20),  new PrinterElement() ),
    DATE_TIME   (PrinterItemType.STRING, new PrinterElement("DATE/TIME", 20),  new PrinterElement() ),
    REFER_NO    (PrinterItemType.STRING, new PrinterElement("REFER", 20),  new PrinterElement() ),
    TRACK_NO    (PrinterItemType.STRING, new PrinterElement("TRACE #", 20),  new PrinterElement() ),
    AUTH_NO     (PrinterItemType.STRING, new PrinterElement("AUTH #", 20),  new PrinterElement() ),
    AMOUNT      (PrinterItemType.STRING, new PrinterElement("AMOUNT", 32, PrinterDefine.Font_Bold),  new PrinterElement("", 32, PrinterDefine.Font_Bold) ),
    BALANCE     (PrinterItemType.STRING, new PrinterElement("BALANCE", 20),  new PrinterElement() ),
    TIP         (PrinterItemType.STRING, new PrinterElement("TIP"),  new PrinterElement() ),
    TOTAL       (PrinterItemType.STRING, new PrinterElement("TOTAL"),  new PrinterElement() ),
    REFERENCE   (PrinterItemType.STRING, new PrinterElement("REFERENCE",20),  new PrinterElement() ),
    E_SIGN        (PrinterItemType.IMG_BCD, new PrinterElement("SIGN",20),  new PrinterElement() ),
    RE_PRINT_NOTE(PrinterItemType.STRING, new PrinterElement("RE-PRINT",20 , PrinterDefine.PStyle_align_center),  new PrinterElement() ),
    TC   (PrinterItemType.STRING, new PrinterElement("TC",20),  new PrinterElement() ),

    COMMENT_1   (PrinterItemType.STRING,1, new PrinterElement("", 16, PrinterDefine.PStyle_align_center),  new PrinterElement() ),
    COMMENT_2   (PrinterItemType.STRING,1, new PrinterElement("", 16, PrinterDefine.PStyle_align_center),  new PrinterElement() ),
    COMMENT_3   (PrinterItemType.STRING,1, new PrinterElement("", 16, PrinterDefine.PStyle_align_center),  new PrinterElement() ),

    FLEXIBLE_1  (PrinterItemType.STRING, new PrinterElement(),  new PrinterElement() ),
    FLEXIBLE_2  (PrinterItemType.STRING, new PrinterElement(),  new PrinterElement() ),
    FLEXIBLE_3  (PrinterItemType.STRING, new PrinterElement(),  new PrinterElement() ),
    FLEXIBLE_4  (PrinterItemType.STRING, new PrinterElement(),  new PrinterElement() ),
    FLEXIBLE_5  (PrinterItemType.STRING, new PrinterElement(),  new PrinterElement() ),

//    GUIDE1        (PrinterItemType.LOGO_ASSETS, new PrinterElement("guide/2-main.png"), new PrinterElement() ),
//    GUIDE2        (PrinterItemType.LOGO_ASSETS, new PrinterElement("guide/2-main.png"), new PrinterElement() ),
//    GUIDE3        (PrinterItemType.LOGO_ASSETS, new PrinterElement("guide/2-main.png"), new PrinterElement() ),
//    GUIDE4        (PrinterItemType.LOGO_ASSETS, new PrinterElement("guide/2-main.png"), new PrinterElement() ),

    BARCODE_1     (PrinterItemType.BARCODE, new PrinterElement("Barcode for refund", 48, PrinterDefine.PStyle_align_center), new PrinterElement("123456789", 32, PrinterDefine.PStyle_align_center ) ),
    BARCODE_2     (PrinterItemType.BARCODE, new PrinterElement("Barcode for refund", 48, PrinterDefine.PStyle_align_center), new PrinterElement("123456789", 32, PrinterDefine.PStyle_align_center ) ),

    QRCODE_1     (PrinterItemType.QRCODE, new PrinterElement("123456789", 128, PrinterDefine.PStyle_align_center), new PrinterElement("123456789", 128, PrinterDefine.PStyle_align_center) ),
    QRCODE_2     (PrinterItemType.QRCODE, new PrinterElement("123456789", 128, PrinterDefine.PStyle_align_center), new PrinterElement("123456789", 128, PrinterDefine.PStyle_align_center) ),

    CUT         (PrinterItemType.STRING, 1, new PrinterElement("----------x----------x----------"),  new PrinterElement() ),
    LINE        (PrinterItemType.LINE, new PrinterElement("",2),  new PrinterElement() ),
    FEED        (PrinterItemType.FEED, new PrinterElement("",2),  new PrinterElement() ), // pixel for feed

    FEED_LINE   (PrinterItemType.FEED, 1,  new PrinterElement("",20),  new PrinterElement() ); // pixel for feed


    public PrinterItemType type;
    public PrinterElement title;
    public PrinterElement value;
    /**
     * 1 for paper, 2 for show, 3 for paper and show as the default
     */
    public int printerMode;

    public boolean isForceMultiLines;

    private PrinterItemType df_type;
    private PrinterElement df_title;
    private PrinterElement df_value;
    private boolean df_isForceMultiLines;

    private void set(PrinterItemType type, PrinterElement title, PrinterElement value, boolean isForceMultiLines, int printerMode){
        if( title.style == -1 ){
            title.style = PrinterDefine.PStyle_align_left;
        }
        if( value.style == -1 ){
            value.style = PrinterDefine.PStyle_align_right;
        }

        this.df_type = type;
        this.df_title = title;
        this.df_value = value;
        this.df_isForceMultiLines = isForceMultiLines ;

        this.type = df_type;
        this.title = df_title;
        this.value = df_value;
        this.isForceMultiLines = this.df_isForceMultiLines;

        this.printerMode = printerMode;
    }

    PrinterItem(PrinterItemType type, PrinterElement title, PrinterElement value){
        set(type, title, value, false , 3);
    }
    PrinterItem(PrinterItemType type, PrinterElement title, PrinterElement value, boolean isForceMultiLines ){
        set(type, title,value, isForceMultiLines , 3);
    }
    PrinterItem(PrinterItemType type, int printerMode, PrinterElement title, PrinterElement value){
        set(type, title, value, false , printerMode);
    }

    public void copy( PrinterItem printerItem ){
        this.title = new PrinterElement( printerItem.title);
        this.value = new PrinterElement( printerItem.value);
        this.type = printerItem.type;
        this.isForceMultiLines = printerItem.isForceMultiLines;
    }

    public void restore(){
        type = df_type;
        title = df_title;
        value = df_value;
        isForceMultiLines = df_isForceMultiLines;
    }


}
