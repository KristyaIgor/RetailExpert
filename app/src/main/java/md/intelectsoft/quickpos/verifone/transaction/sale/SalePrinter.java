package md.intelectsoft.quickpos.verifone.transaction.sale;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import md.intelectsoft.quickpos.R;
import md.intelectsoft.quickpos.verifone.Utilities.Utility;
import md.intelectsoft.quickpos.verifone.basic.HostInformation;
import md.intelectsoft.quickpos.verifone.basic.ISO8583;
import md.intelectsoft.quickpos.verifone.transaction.TransPrinter;
import md.intelectsoft.quickpos.verifone.transaction.TransactionParams;
import md.intelectsoft.quickpos.verifone.transaction.canvas_printer.PrinterDefine;
import md.intelectsoft.quickpos.verifone.transaction.canvas_printer.PrinterItem;


/**
 * Created by Simon on 2019/2/22.
 */

public class SalePrinter extends TransPrinter {
    static final String TAG = "SalePrinter";
    public SalePrinter(Context context) {
        super(context);
        Log.d(TAG, "create");
    }
    public void initializeData(ISO8583 require, ISO8583 response, HostInformation hostInformation, Bundle extraItems ){
        super.initializeData(require, response, hostInformation, extraItems);
        Log.d(TAG, "initializeData");
        try {
            printerItems = new ArrayList<>();
            PrinterItem.LOGO.title.sValue = "v_logo.jpg";
            PrinterItem.LOGO.title.style = PrinterDefine.PStyle_align_left;

            PrinterItem.TRANS_TYPE.value.sValue = "SALE";

            if( null != hostInformation ){
                // set in super
            }

            if (null != require) {
                PrinterItem.AMOUNT.value.sValue = Utility.getReadableAmount(require.getValue(ISO8583.ATTRIBUTE.Amount));
            }

            printerItems.add(PrinterItem.LOGO);
            printerItems.add(PrinterItem.TITLE);
            printerItems.add(PrinterItem.SUBTITLE);
            printerItems.add(PrinterItem.HOST);

            printerItems.add(PrinterItem.MERCHANT_NAME);
            printerItems.add(PrinterItem.MERCHANT_ID);
            printerItems.add(PrinterItem.TERMINAL_ID);

            printerItems.add(PrinterItem.TRANS_TYPE);

            printerItems.add(PrinterItem.LINE);

            printerItems.add(PrinterItem.CARD_NO);
            printerItems.add(PrinterItem.AMOUNT);

            PrinterItem.FLEXIBLE_1.copy(PrinterItem.AMOUNT);
            // Set transaction amount on receipt and make it readable
            PrinterItem.FLEXIBLE_1.value.sValue = Utility.getReadableAmount(TransactionParams.getInstance().getTransactionAmount());
            PrinterItem.FLEXIBLE_1.title.sValue += " (USD)";
            printerItems.add(PrinterItem.FLEXIBLE_1);

            printerItems.add(PrinterItem.DATE_TIME);

            PrinterItem.QRCODE_1.value.sValue = context.getResources().getString(R.string.prn_qrcode2);

            PrinterItem.BARCODE_1.value.sValue = context.getResources().getString(R.string.prn_barcode);

            printerItems.add(PrinterItem.FEED);
            printerItems.add(PrinterItem.BARCODE_1);
            printerItems.add(PrinterItem.FEED);
            printerItems.add(PrinterItem.FEED);
            printerItems.add(PrinterItem.QRCODE_1);
            printerItems.add(PrinterItem.LINE);

            printerItems.add(PrinterItem.COMMENT_1);
            printerItems.add(PrinterItem.COMMENT_2);
            printerItems.add(PrinterItem.COMMENT_3);


        }catch ( Exception e){
            Log.e(TAG,"Exception :" + e.getMessage());
            for (StackTraceElement m:e.getStackTrace()
                 ) {
                Log.e(TAG,"Exception :" + m );

            }
        }
    }

}
