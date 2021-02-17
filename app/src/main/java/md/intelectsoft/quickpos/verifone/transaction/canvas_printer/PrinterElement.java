package md.intelectsoft.quickpos.verifone.transaction.canvas_printer;

/**
 * Created by Simon on 2019/2/27.
 */

public class PrinterElement{
    private static final int DEFAULT_FONT_SIZE = 24;
    public String sValue;
    public int style;
    public int size;    // font size for print string, barcode height for barcode, QR code board size for QR code
    public int iValue;
    public String fontFile;
    public PrinterElement(){
        set("", DEFAULT_FONT_SIZE, -1, 0);
    }
    public PrinterElement(PrinterElement pe){
        set(pe.sValue, pe.size, pe.style, pe.iValue );
        this.fontFile = pe.fontFile;
    }
    public PrinterElement(String sValue ){
        set(sValue, DEFAULT_FONT_SIZE, -1, 0);
    }
    public PrinterElement(String sValue, String fontFile ){
        set(sValue, DEFAULT_FONT_SIZE, -1, 0);
        this.fontFile = fontFile;
    }
    public PrinterElement(String sValue, int size){
        set(sValue, size, -1, 0);
    }
    public PrinterElement(String sValue, int size, String fontFile ){
        set(sValue, size, -1, 0);
        this.fontFile = fontFile;
    }
    public PrinterElement(String sValue, int size, int style  ){
        set(sValue, size, style, 0);
    }
    public PrinterElement(String sValue, int size, int style, int iValue ){
        set(sValue, size, style, iValue);
    }
    public PrinterElement(String sValue, int size, int style, String fontFile ){
        set(sValue, size, style, 0);
        this.fontFile = fontFile;
    }
    public PrinterElement(String sValue, int size, int style, int iValue, String fontFile ){
        set(sValue, size, style, iValue);
        this.fontFile = fontFile;
    }
    public void set(String sValue, int size, int style, int iValue ){
        this.sValue = sValue;
        this.size = size;
        this.style = style;
        this.iValue = iValue;
        this.fontFile = "";
    }

    public void setAlignment( int alignment ){
        this.style &= PrinterDefine.PStyle_align_remove;
        this.style |= alignment;
    }

}