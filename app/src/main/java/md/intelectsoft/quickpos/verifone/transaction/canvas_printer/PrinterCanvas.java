package md.intelectsoft.quickpos.verifone.transaction.canvas_printer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.vfi.smartpos.deviceservice.aidl.IPrinter;
import com.vfi.smartpos.deviceservice.aidl.PrinterConfig;
import com.vfi.smartpos.deviceservice.aidl.PrinterListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import md.intelectsoft.quickpos.verifone.Utilities.DeviceHelper;
import md.intelectsoft.quickpos.verifone.Utilities.Utility;

/**
 * Created by Simon on 2019/2/1.
 */

/**
 * Class {@code PrinterCanvas} is for calling PrinterEx to draw PrinterItem on canvas
 * and the canvas can be set to printer or activity
 */
public class PrinterCanvas {

    static final String TAG = "PrinterCanvas";

    public static IPrinter iPrinter;
    public static Context context;

    // for print
    public static PrinterEx printerEx;

    protected List<PrinterItem> printerItems = null;


    public PrinterCanvas(Context context) {
        this.context = context;
    }

    public void initializeData(Bundle extraItems) {
        Log.d(TAG, "initializeData");

        for (PrinterItem p : PrinterItem.values()
                ) {
            p.restore();
        }
    }

    private void initializeDefaltRecp() {
        printerItems = new ArrayList<>();

        for (PrinterItem p : PrinterItem.values()
                ) {
            printerItems.add(p);
        }
    }

    public static void initialize() {
        PrinterCanvas.iPrinter = DeviceHelper.getInstance().getPrinter();
    }

    public Bundle getParameterFromBitmap(PrinterElement printerElement, Bitmap bitmap) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        Bundle fmtImage = new Bundle();
        fmtImage.putInt("width", width);  // bigger then actual, will print the actual
        fmtImage.putInt("height", height); // bigger then actual, will print the actual

        if ((printerElement.style & PrinterDefine.PStyle_align_left) == PrinterDefine.PStyle_align_left) {
            //
            fmtImage.putInt("offset", 0);
        } else if ((printerElement.style & PrinterDefine.PStyle_align_center) == PrinterDefine.PStyle_align_center) {
            //
            fmtImage.putInt("offset", (384 - width) / 2);
        }
        if ((printerElement.style & PrinterDefine.PStyle_align_right) == PrinterDefine.PStyle_align_right) {
            //
            fmtImage.putInt("offset", (384 - width));
        }

        return fmtImage;
    }

    public Bitmap convertBitmap(PrinterItemType printerItemType, PrinterElement printerElement) {
        try {
            Log.d(TAG, "Try convert bitmap:" + printerElement.sValue);
            Bitmap bitmap;
            if (printerItemType == PrinterItemType.LOGO_ASSETS) {
                InputStream is = context.getAssets().open(printerElement.sValue);
                bitmap = BitmapFactory.decodeStream(is);
//                                bitmap.getPixels();
            } else {
                bitmap = Bitmap.createBitmap(384, 4, Bitmap.Config.ARGB_8888);
                bitmap.eraseColor(Color.WHITE);
            }

            int height = bitmap.getHeight();
            int width = bitmap.getWidth();


//        int colorThreshold=0xFFa0a0a0;    //  0xFF------ for revert
//        int colorThreshold=0xFF606060;    //  0xFF------ for revert, 60 much white
//                        int colorThreshold=0xFF404040;    //  0xFF------ for revert, 40 more white
            int colorThreshold = 0;
            if ((printerElement.style & PrinterDefine.PStyle_image_revert) == PrinterDefine.PStyle_image_revert) {
                colorThreshold |= 0xFF000000;
            }
            if ((printerElement.style & PrinterDefine.PStyle_image_contrast_light) == PrinterDefine.PStyle_image_contrast_light) {
                colorThreshold |= 0x00a0a0a0;
            } else if ((printerElement.style & PrinterDefine.PStyle_image_contrast_normal) == PrinterDefine.PStyle_image_contrast_normal) {
                colorThreshold |= 0x00808080;
            } else if ((printerElement.style & PrinterDefine.PStyle_image_contrast_heavy) == PrinterDefine.PStyle_image_contrast_heavy) {
                colorThreshold |= 0x00404040;
            }

//                            printerEx.addImage(fmtImage, bitmap);
//                            printerEx.feedPixel( null, 8);

            // convert bitmap -- start
            int r, g, b;
            int r_t, g_t, b_t;
            r_t = ((colorThreshold & 0x00FF0000) >> 16);
            g_t = ((colorThreshold & 0x0000FF00) >> 8);
            b_t = ((colorThreshold & 0x000000FF));
            Log.d(TAG, "Color Threshold:" + r_t + ", " + g_t + ", " + b_t);

            int pixels[] = new int[width * height];
            int pixels2[] = new int[width * height];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
            int n = 0;
            for (int i = 0; i < height; i++) {
//                                String line ="Pixel:";
                for (int j = 0; j < width; j++) {
                    int pixel = pixels[n];
//                                    if( j < 64 ){
//                                        line += Integer.toHexString(pixel);
////                                        line += ",";
//                                    }
                    r = ((pixel & 0x00FF0000) >> 16);
                    g = ((pixel & 0x0000FF00) >> 8);
                    b = ((pixel & 0x000000FF));
                    if ((colorThreshold & 0x00FFFFFF) > 0) {
                        // convert color
                        if (r > r_t) {
                            r = 0xFF;
                        }
                        if (g > g_t) {
                            g = 0xFF;
                        }
                        if (b > b_t) {
                            b = 0xFF;
                        }

                        int c = (r < g) ? r : g;
                        c = (c < b) ? c : b;

                        r = c;
                        g = c;
                        b = c;

//                                    pixels[n] = (0xFF000000 + (r << 16) + (g << 8) + b );
                        pixels[n] = (0xFF000000 + (c << 16) + (c << 8) + c);
                        pixels2[n] = pixels[n];
                    }

                    if ((colorThreshold & 0xFF000000) == 0xFF000000) {
                        // revert
                        if ((r + g + b) < 600) {
                            r = 0xFF;
                            g = 0xFF;
                            b = 0xFF;
                        } else {
                            r = 0;
                            g = 0;
                            b = 0;
//                                            r = 0xFF - r;
//                                            g = 0xFF - g;
//                                            b = 0xFF - b;
                        }
                        pixels[n] = (0xFF000000 + (r << 16) + (g << 8) + b);
                    }

//                                    if( j < 64 ){
//                                        line += "-";
//                                        line += Integer.toHexString(pixels[n]);
//                                        line += ",";
//                                    }

                    n++;
                }
//                                Log.d("PIXEL", line);
            }
//                            bitmap = Bitmap.createBitmap(pixels2, 0, width, width, height, Bitmap.Config.ARGB_8888);
//                            // convert bitmap -- end
//                            printerEx.addImage(fmtImage, bitmap);
//                            printerEx.feedPixel( null, 8);

            bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
            // convert bitmap -- end
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public void print() {
        print(printerListenerDef);
    }

    // return true for paper printing, false for not
    public boolean print(PrinterListener printerListener) {
        Log.d(TAG, "print()" );
        if( null == printerItems ){
//            initializeDefaltRecp();
        }
        boolean paperPrinting = false;
        printerEx = new PrinterEx();

        Bundle format = new Bundle();

        // bundle formate for AddTextInLine
        Bundle fmtAddTextInLine = new Bundle();

        Bundle fmtImage = new Bundle();
        try {
            byte[] bufferLogo = null;
            for ( PrinterItem printerItem: printerItems
                 ) {
                format.putInt(PrinterConfig.addText.FontSize.BundleName, 1);
                format.putInt(PrinterConfig.addText.Alignment.BundleName, 2);
                format.putString("fontStyle", PrinterDefine.Font_default );
                format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.LEFT);

                switch ( printerItem.type ){
                    case LOGO_STORAGE:
                    case LOGO_ASSETS:

                        Bitmap bitmap = convertBitmap( printerItem.type, printerItem.title);
                        if( null != bitmap ) {
                            fmtImage = getParameterFromBitmap(printerItem.title, bitmap);
                            printerEx.addImage(fmtImage, bitmap);
                        }

                        if( printerItem.value.sValue != null ){
                            if(printerItem.value.sValue.length() > 0 ){
                                int w = 0;
                                int h = 0;
                                if( null != bitmap ){
                                    w = bitmap.getWidth();
                                    h = bitmap.getHeight();
                                }
                                bitmap = convertBitmap( printerItem.type, printerItem.value);
                                if( null != bitmap ) {
                                    fmtImage = getParameterFromBitmap(printerItem.value, bitmap);

                                    if( (printerItem.title.style & PrinterDefine.PStyle_align_left) != 0
                                            && (printerItem.value.style & PrinterDefine.PStyle_align_right) != 0 ) {
                                        // try print 2 logo in one line
                                        if( (w + bitmap.getWidth()) < PrinterEx.MAX_WIDTH ) {
                                            // print 2 logo in one line
                                            printerEx.feedPixel(null, 0-h-2 );
                                        } else {

                                        }
                                    }
                                    printerEx.addImage(fmtImage, bitmap);
                                }
                            }

                        }
                        break;
                    case STRING:
                        int offsetX = 0;
                            int fontsize = 1;
                            if( printerItem.title.sValue.length() > 0 ) {
                                // font size
                                fontsize = printerItem.title.size;
                                if( fontsize > 0 ) {
                                    format.putInt(PrinterConfig.addText.FontSize.BundleName, fontsize);
                                }
                                if( printerItem.title.fontFile.length() > 0 ){
                                    format.putString("fontStyle", printerItem.title.fontFile );
                                }
                                // aligment
                                format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.LEFT);
                            if( (printerItem.title.style& PrinterDefine.PStyle_align_left) != 0 ){
                                format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.LEFT);
                            } else if( (printerItem.title.style& PrinterDefine.PStyle_align_center) != 0 ){
                                format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.CENTER);
                            } else if( (printerItem.title.style& PrinterDefine.PStyle_align_right) != 0 ){
                                format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.RIGHT);
                            }
                            if( printerItem.isForceMultiLines ) {
                                format.putBoolean("newline", true );
                            } else if(null != printerItem.value.sValue) {
                                if( printerItem.value.sValue.length() > 0 ){
                                    format.putBoolean("newline", false );
                                }
                            }
                            offsetX = printerEx.addText(format, printerItem.title.sValue, printerItem.printerMode );

                        }
                        if( null != printerItem.value.sValue ) {
                            if( printerItem.value.sValue.length() > 0 ) {
                                // font size
                                fontsize = printerItem.value.size;
                                if( fontsize > 0 ) {
                                    format.putInt(PrinterConfig.addText.FontSize.BundleName, fontsize);
                                }
                                if( printerItem.value.fontFile.length() > 0 ){
                                    format.putString("fontStyle", printerItem.value.fontFile );
                                }
                                // aligment
                                format.putBoolean("newline", true );
                                format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.RIGHT);
                                if( (printerItem.value.style& PrinterDefine.PStyle_align_left) != 0 ){
                                    format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.LEFT);
                                } else if( (printerItem.value.style& PrinterDefine.PStyle_align_center) != 0 ){
                                    format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.CENTER);
                                } else if( (printerItem.value.style& PrinterDefine.PStyle_align_right) != 0 ){
                                    format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.RIGHT);
                                }

                                printerEx.addText(format, printerItem.value.sValue, printerItem.printerMode );
                            }
                        }
                        break;
                    case LINE:
                        printerEx.addLine(null, printerItem.title.size);
                        break;
                    case FEED:
                        printerEx.feedPixel(null, printerItem.title.size);
                        break;
                    case QRCODE:
                        Bundle qrCodeFormat = new Bundle();
                        boolean needScrollBack = false;
                        if( printerItem.title.sValue.length() > 0 && printerItem.value.sValue.length() > 0  ) {
                            printerItem.title.style = PrinterDefine.PStyle_align_left;
                            if( printerItem.title.size > 180 ){
                                printerItem.title.size = 180;
                            }
                            printerItem.value.style = PrinterDefine.PStyle_align_right;
                            if( printerItem.value.size > 180 ){
                                printerItem.value.size = 180;
                            }

                            needScrollBack = true;
                        }
                        PrinterElement qrcode = printerItem.title;
                        if( qrcode.sValue.length() > 0 ){
                            if( (qrcode.style& PrinterDefine.PStyle_align_center) != 0 ){
                                qrCodeFormat.putInt(PrinterConfig.addText.Alignment.BundleName,  PrinterConfig.addText.Alignment.CENTER);
                            } else if( (qrcode.style& PrinterDefine.PStyle_align_right) != 0 ){
                                qrCodeFormat.putInt(PrinterConfig.addText.Alignment.BundleName,  PrinterConfig.addText.Alignment.RIGHT);
                            }
                            // size
                            qrCodeFormat.putInt(PrinterConfig.addQrCode.Height.BundleName, qrcode.size );
                            printerEx.addQrCode( qrCodeFormat, qrcode.sValue );
                        }

                        qrcode = printerItem.value;
                        if( qrcode.sValue.length() > 0 ){
                            if( (qrcode.style& PrinterDefine.PStyle_align_center) != 0 ){
                                qrCodeFormat.putInt(PrinterConfig.addText.Alignment.BundleName,  PrinterConfig.addText.Alignment.CENTER);
                            } else if( (qrcode.style& PrinterDefine.PStyle_align_right) != 0 ){
                                qrCodeFormat.putInt(PrinterConfig.addText.Alignment.BundleName,  PrinterConfig.addText.Alignment.RIGHT);
                            }
                            // size
                            qrCodeFormat.putInt(PrinterConfig.addQrCode.Height.BundleName, qrcode.size );
                            if( needScrollBack ){
                                printerEx.scrollBack();
                            }
                            printerEx.addQrCode( qrCodeFormat, qrcode.sValue );
                        }

                        break;
                    case BARCODE:
                        Bundle barcodeFormat = new Bundle();
                        if( (printerItem.value.style& PrinterDefine.PStyle_align_center) != 0 ){
                            barcodeFormat.putInt(PrinterConfig.addText.Alignment.BundleName,  PrinterConfig.addText.Alignment.CENTER);
                        } else if( (printerItem.value.style& PrinterDefine.PStyle_align_right) != 0 ){
                            barcodeFormat.putInt(PrinterConfig.addText.Alignment.BundleName,  PrinterConfig.addText.Alignment.RIGHT);
                        }
                        barcodeFormat.putInt(PrinterConfig.addBarCode.Height.BundleName, printerItem.value.size );
                        printerEx.addBarCode( barcodeFormat, printerItem.value.sValue );
                        break;
                    case IMG_BCD:
                        // title
                        addString( printerItem.title );

                        // image
                        if( null != printerItem.value.sValue ){
                            Log.d(TAG, "add IMG_BCD, size" + printerItem.value.sValue.length() );
                            if( printerItem.value.sValue.length() > 0 ){
                                printerEx.addImage( null , (Utility.hexStr2Byte(printerItem.value.sValue) ) );
                            }
                        }
                        break;
                }

            }

            //
            if( true ) {
                // print to paper
                if( iPrinter != null ){
                    format.putInt(PrinterConfig.addText.FontSize.BundleName, 0);
                    format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.CENTER);
                    iPrinter.addText(format, "    ");

                    // add printerEx to iPrinter
                    fmtImage.putInt("offset", 0);
                    fmtImage.putInt("width", 384);  // bigger then actual, will print the actual
                    fmtImage.putInt("height", printerEx.getHeight(true)); // bigger then actual, will print the actual
                    iPrinter.addImage(fmtImage, printerEx.getData(true));
                    // printerEx -- end

                    format.putInt(PrinterConfig.addText.FontSize.BundleName, 0);
                    format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.CENTER);
                    iPrinter.addText(format, "---------X-----------X----------");

                    iPrinter.feedLine(3);

                    // start print here
                    iPrinter.startPrint(printerListener);

                    paperPrinting = true;
                }

            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }catch ( Exception e){
            Log.e(TAG,"Exception :" + e.getMessage());
            for (StackTraceElement m:e.getStackTrace()
                    ) {
                Log.e(TAG,"Exception :" + m );

        }

    }
//        Intent intent = new Intent( context, PrintCanvasActivity.class);
//        context.startActivity(intent);

        return paperPrinting;

    }

    public void addString( PrinterElement printerElement){
        int offsetX = 0;
        int fontsize = 1;
        Bundle format = new Bundle();
        format.putInt(PrinterConfig.addText.FontSize.BundleName, 1);
        format.putInt(PrinterConfig.addText.Alignment.BundleName, 2);
        format.putString("fontStyle", PrinterDefine.Font_default );
        format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.LEFT);

        if( printerElement.sValue.length() > 0 ) {
            // font size
            fontsize = printerElement.size;
            if( fontsize > 0 ) {
                format.putInt(PrinterConfig.addText.FontSize.BundleName, fontsize);
            }
            if( printerElement.fontFile.length() > 0 ){
                format.putString("fontStyle", printerElement.fontFile );
            }
            // aligment
            format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.LEFT);
            if( (printerElement.style& PrinterDefine.PStyle_align_left) != 0 ){
                format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.LEFT);
            } else if( (printerElement.style& PrinterDefine.PStyle_align_center) != 0 ){
                format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.CENTER);
            } else if( (printerElement.style& PrinterDefine.PStyle_align_right) != 0 ){
                format.putInt(PrinterConfig.addText.Alignment.BundleName, PrinterConfig.addText.Alignment.RIGHT);
            }
            try {
                offsetX = printerEx.addText(format, printerElement.sValue, 3 );
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    }


    PrinterListener printerListenerDef = new PrinterListener.Stub() {
        @Override
        public void onFinish() throws RemoteException {
            Log.d(TAG, "Printer : Finish" );
        }

        @Override
        public void onError(int error) throws RemoteException {
            Log.e(TAG, "Printer error : " +error );
        }
    };
}
