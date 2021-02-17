package md.intelectsoft.quickpos.verifone.transaction.canvas_printer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.vfi.smartpos.deviceservice.aidl.FontFamily;
import com.vfi.smartpos.deviceservice.aidl.PrinterConfig;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;

/**
 * Created by Simon on 2019/4/10.
 *
 * Class {@code PrinterExBase} is for draw content on canvas
 * and the canvas can be set to printer or activity
 */

public class PrinterExBase {

    private static final String TAG = "PrinterExBase";
    public static final int MAX_WIDTH = 384;
    boolean isPaperReceipt = true;

    int offsetY;
    int offsetX;
    Canvas canvas;
    Bitmap bitmap;
    Paint paint;

    public PrinterExBase( boolean isPaperReceipt ) {
        this.isPaperReceipt = isPaperReceipt;

        offsetY = 0;
        offsetX = 0;

        paint = new Paint();
        paint.setStrokeWidth(1);
        paint.setTextSize(50);
//        paint.setColor(Color.BLACK);
        paint.setColor(Color.BLACK);
        bitmap = Bitmap.createBitmap(MAX_WIDTH, MAX_WIDTH * 10, Bitmap.Config.ARGB_8888);
        if( isPaperReceipt ){
            bitmap.eraseColor(Color.WHITE);
        }

        canvas = new Canvas(bitmap);
//        writeRuler(0);
    }

    public int addText(Bundle fontFormat, String text) throws RemoteException {
        String fontStyle = fontFormat.getString("fontStyle", "");

        //
        boolean isBoldFont = fontFormat.getBoolean("bold", false);
        boolean isNewLine = fontFormat.getBoolean("newline", true);
        int offset = fontFormat.getInt("offset", 0);
        int typeFaceStyle = Typeface.NORMAL;


        int fontSize = fontFormat.getInt(PrinterConfig.BUNDLE_PRINT_FONT);

        if (fontSize == FontFamily.MIDDLE) {
            fontSize = 24;
        }
        else if (fontSize == FontFamily.SMALL) {
            fontSize = 16;
        } else if (fontSize == FontFamily.BIG) {
            fontSize = 24;
//            entity.setMultipleHeight(2);
//            entity.setMultipleWidth(1);
            fontSize = 28;
            paint.setTextScaleX(0.5f);
            isBoldFont = true;
        } else if (fontSize == 3) {
            fontSize = 32;
        } else if (fontSize == 4) {
            fontSize = 32;
//            entity.setMultipleHeight(2);
//            entity.setMultipleWidth(1);
            isBoldFont = true;
        } else if (fontSize == 5) {
            fontSize = 48;
        }

        if( isBoldFont ){
            typeFaceStyle = Typeface.BOLD;
        }

        Log.d(TAG, "font size:"+ fontSize+", Style:" + fontStyle );
        paint.setTypeface(Typeface.createFromFile(fontStyle));

        int alignType = fontFormat.getInt(PrinterConfig.BUNDLE_PRINT_ALIGN);
        Paint.Align align = Paint.Align.LEFT;
        int x = 0;
        if (alignType == FontFamily.CENTER) {
            align = Paint.Align.CENTER;
            x = MAX_WIDTH / 2;
        } else if (alignType == FontFamily.RIGHT) {
            align = Paint.Align.RIGHT;
            x = MAX_WIDTH;
        }

        paint.setTextSize(fontSize);
        paint.setTextAlign(align);
        offsetY += fontSize;
        int width = (int) paint.measureText( text,0, text.length() );
        Log.d(TAG, "pixel "+width+" of " + text + ", try print at [" + offsetX + ", " + offsetY + "]");
        switch (alignType){
            case FontFamily.LEFT:
                if( width + offsetX > MAX_WIDTH ) {
                    Log.d(TAG, "Over size, write in new line");
                    offsetY += fontSize;
                    offsetX = 0;
                } else {
                    offsetX += width;
                }
                break;
            case FontFamily.CENTER:
                if( width + offsetX*2 > MAX_WIDTH ) {
                    Log.d(TAG, "Over size, write in new line");
                    offsetY += fontSize;
                    offsetX = 0;
                } else {
                    offsetX = (MAX_WIDTH/2+width/2);
                }
                break;
            case FontFamily.RIGHT:
                if( width + offsetX > MAX_WIDTH ) {
                    Log.d(TAG, "Over size, write in new line");
                    offsetY += fontSize;
                    offsetX = 0;
                } else {
                    offsetX = MAX_WIDTH;
                }
                break;

        }
        canvas.drawText(text, x, offsetY, paint);
        if( isNewLine ) {
            offsetX = 0;
            offsetY += 2;
        } else {
            offsetY -= fontSize;
        }
        return offsetX;
    }

    public void addTextInLine(Bundle fontFormat, String left, String center, String right, int mode) throws RemoteException {
        // todo f

    }
    public void addImage(Bundle format, byte[] imageData) throws RemoteException {
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length );
        addImage( format, bitmap );
    }
    public Bitmap convertImage(Bitmap bitmap){
        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            Bitmap createBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int color = bitmap.getPixel(j, i);
                    int g = Color.green(color);
                    int r = Color.red(color);
                    int b = Color.blue(color);
                    int a = Color.alpha(color);
                    if(g>=196&&r>=196&&b>=196){
                        a = 0;
                    }
                    color = Color.argb(a, r, g, b);
                    createBitmap.setPixel(j, i, color);
                }
            }
            return createBitmap;
        }
        return null;
    }
    public void addImage(Bundle format, Bitmap bitmap) {
        int offset = 0;
        if( null == bitmap ){
            Log.e(TAG, "addImage:null" );
            return;
        }
        if( null != format ){
            offset = format.getInt("offset", 0);
            switch (format.getInt( PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.LEFT ) ){
                case PrinterConfig.addText.Alignment.CENTER:
                    offset = (MAX_WIDTH-bitmap.getWidth())/2;
                    break;
                case PrinterConfig.addText.Alignment.RIGHT:
                    offset = (MAX_WIDTH-bitmap.getWidth());
                    break;
            }
        }

        Log.d(TAG, "addImage, offset:" + offset );
        if( !isPaperReceipt ){
            bitmap = convertImage(bitmap);
        }

        canvas.drawBitmap( bitmap, offset, offsetY, null );
        scrollDown( bitmap.getHeight() + 2);

    }
    // draw horizontal line
    public void addLine(Bundle format, int width ) throws RemoteException {
        paint.setPathEffect(null);
        paint.setStrokeWidth(width);
        canvas.drawLine(1,offsetY,MAX_WIDTH-1, offsetY, paint );
        offsetY += width;
        offsetY += 2;
    }
    public void addQrCode(Bundle format, String qrCode){
        int size = format.getInt(PrinterConfig.addQrCode.Height.BundleName);
        Bitmap bitmap = create1D2DcodeImage(qrCode,size, BarcodeFormat.QR_CODE );
        addImage( format, bitmap );

    }
    public void addBarCode(Bundle format, String barcode){
        // BarcodeFormat.CODE_128
        int size = format.getInt(PrinterConfig.addBarCode.Height.BundleName);
        Bitmap bitmap = create1D2DcodeImage(barcode,size, BarcodeFormat.CODE_128 );
        addImage( format, bitmap );
    }

    public void feedPixel(Bundle format, int pixel ) throws RemoteException {
        offsetY += pixel;
    }
    public void writeRuler( int mode ){
        int x = 0;
        int y = 0;
        paint.setStrokeWidth(1);
        int c = paint.getColor();
        paint.setColor(Color.GRAY);
        paint.setPathEffect(new DashPathEffect(new float[]{16f,48f}, 0));
        for( x = -8; x <= MAX_WIDTH;  ){
            canvas.drawLine(x,0,x, MAX_WIDTH, paint );
            canvas.drawLine(0,x,MAX_WIDTH, x, paint );
            x+= 32;
        }
        paint.setColor(c);
    }

    protected int lastScrollDownPixel = 0;
    public int scrollDown( int pixel ){
        offsetY += pixel;
        lastScrollDownPixel = pixel;
        return offsetY;
    }

    public int scrollBack(){
        int a = lastScrollDownPixel;
        offsetY -= lastScrollDownPixel;
        lastScrollDownPixel = 0;
        return a;
    }

    public Bitmap getBitmap(){
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0,0,MAX_WIDTH, offsetY);
        return newBitmap;
    }
    public int getHeight() {
        return offsetY;
    }

    public byte[] getData() {
        return getBytesByBitmap(bitmap);
    }

    public byte[] getBytesByBitmap(Bitmap bitmap) {

        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0,0,MAX_WIDTH, offsetY);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();

    }

    private Bitmap create1D2DcodeImage(String content, int size, BarcodeFormat barcodeFormat) {
        Log.d(TAG, "create1D2DcodeImage, size:" + size + ", content:" + content);
        if( content.length() == 0 ){
            return null;
        }
        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8" );
        hints.put(EncodeHintType.MARGIN, 1);
        if( size == 0 ){
            size = 360;
        }
        int QRCODE_SIZE = size;
        int QRCODE_SIZEf = size;
        int width = size;
        int height = size;
        if( barcodeFormat != BarcodeFormat.QR_CODE ){
            // bar code
            width = MAX_WIDTH - 16;
        }
        BitMatrix bitMatrix = null;
        try {
//                bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, QRCODE_SIZE, QRCODE_SIZE, hints);
            bitMatrix = new MultiFormatWriter().encode(content, barcodeFormat, width, height, hints);

            width = bitMatrix.getWidth();
            height = bitMatrix.getHeight();

            Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            b.eraseColor(Color.WHITE);
            for( int y=0; y< height ; y++){
                for( int x=0; x<width; x++){
                    if( bitMatrix.get(x,y)){
                        b.setPixel(x,y, Color.BLACK);
                    }
                }
            }
            return b;

        } catch (WriterException e) {
            e.printStackTrace();
        } catch ( Exception e ){
            e.printStackTrace();
        }
        return null;
    }



}

