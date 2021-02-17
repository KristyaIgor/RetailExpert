package md.intelectsoft.quickpos.verifone.transaction.canvas_printer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.RemoteException;

/**
 * Created by Simon on 2019/2/20.
 */

public class PrinterEx {

    private static final String TAG = "PrinterEx";
    public static final int MAX_WIDTH = 384;

    PrinterExBase printerReceipt;
    PrinterExBase printerCanvas;

    public PrinterEx() {
        printerCanvas = new PrinterExBase(false);
        printerReceipt= new PrinterExBase(true);
    }

    public int addText(Bundle fontFormat, String text, int printerMode) throws RemoteException {
        int ret = 0;
        if( (printerMode & 1) > 0 ){
            ret = printerReceipt.addText(fontFormat,text);
        }
        if( (printerMode & 2) > 0 ){
            ret = printerCanvas.addText(fontFormat,text);
        }
        return ret;
    }

    public void addTextInLine(Bundle fontFormat, String left, String center, String right, int mode) throws RemoteException {
        // todo f

    }
    public void addImage(Bundle format, byte[] imageData) throws RemoteException {
        printerCanvas.addImage(format, imageData);
        printerReceipt.addImage(format, imageData);
    }
    public void addImage(Bundle format, Bitmap bitmap) throws RemoteException {
        printerCanvas.addImage(format, bitmap);
        printerReceipt.addImage(format, bitmap);

    }
    public void addLine(Bundle format, int width ) throws RemoteException {
        printerCanvas.addLine(format,width);
        printerReceipt.addLine(format,width);
    }
    public void addQrCode(Bundle format, String qrCode){
        printerCanvas.addQrCode(format, qrCode);
        printerReceipt.addQrCode(format, qrCode);
    }
    public void addBarCode(Bundle format, String barcode){
        printerCanvas.addBarCode(format, barcode);
        printerReceipt.addBarCode(format, barcode);
    }
    public void feedPixel(Bundle format, int pixel ) throws RemoteException {
        printerCanvas.feedPixel(format,pixel);
        printerReceipt.feedPixel(format,pixel);

    }
    public void writeRuler( int mode ){
        printerCanvas.writeRuler(mode);
        printerReceipt.writeRuler(mode);
    }
    public void scrollBack( ){
        printerCanvas.scrollBack();
        printerReceipt.scrollBack();
    }

    public Bitmap getBitmap(boolean isPaperReceipt){
        if( isPaperReceipt ){
            return printerReceipt.getBitmap();
        } else {
            return printerCanvas.getBitmap();
        }
    }
    public int getHeight( boolean isPaperReceipt) {
        if( isPaperReceipt ){
            return printerReceipt.getHeight();
        } else {
            return printerCanvas.getHeight();
        }

    }

    public byte[] getData( boolean isPaperReceipt) {
        if( isPaperReceipt ){
            return printerReceipt.getData();
        } else {
            return printerCanvas.getData();
        }
    }

    public byte[] getBytesByBitmap(Bitmap bitmap, boolean isPaperReceipt) {
        if( isPaperReceipt ){
            return printerReceipt.getBytesByBitmap(bitmap);
        } else {
            return printerCanvas.getBytesByBitmap(bitmap);
        }
    }


}
