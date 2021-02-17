package md.intelectsoft.quickpos.verifone.transaction.balance;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import md.intelectsoft.quickpos.verifone.Utilities.Utility;
import md.intelectsoft.quickpos.verifone.basic.HostInformation;
import md.intelectsoft.quickpos.verifone.basic.ISO8583;
import md.intelectsoft.quickpos.verifone.transaction.TransPrinter;
import md.intelectsoft.quickpos.verifone.transaction.canvas_printer.PrinterDefine;
import md.intelectsoft.quickpos.verifone.transaction.canvas_printer.PrinterItem;

/**
 * Created by Simon on 2019/2/22.
 */

public class BalancePrinter extends TransPrinter {
    static final String TAG = "BalancePrinter";
    public BalancePrinter(Context context) {
        super(context);
        Log.d(TAG, "create");

    }

    public void initializeData(ISO8583 require, ISO8583 response, HostInformation hostInformation, Bundle extraItems ) {
        super.initializeData(require, response, hostInformation, extraItems);
        try {
            printerItems = new ArrayList<>();

            PrinterItem.LOGO.title.sValue = "verifone_logo.jpg";
            PrinterItem.LOGO.title.style = PrinterDefine.PStyle_align_left;

            PrinterItem.TRANS_TYPE.value.sValue = "BALANCE";

            if( null != hostInformation ){
                PrinterItem.HOST.value.sValue = hostInformation.description;
                PrinterItem.TERMINAL_ID.value.sValue = hostInformation.terminalID;
                PrinterItem.MERCHANT_NAME.value.sValue = hostInformation.merchantName;
                PrinterItem.MERCHANT_ID.value.sValue = hostInformation.merchantID;
            }

            if (null != require) {
                PrinterItem.BALANCE.value.sValue = Utility.getReadableAmount(require.getValue(ISO8583.ATTRIBUTE.Balance));
                // PrinterItem.CARD_NO.sValue = fixCardNoWithMask( require.getValue(ISO8583.ATTRIBUTE.Track2) );
            }

            printerItems.add(PrinterItem.LOGO);

            printerItems.add(PrinterItem.LINE);
            printerItems.add(PrinterItem.MERCHANT_NAME);
            printerItems.add(PrinterItem.MERCHANT_ID);
            printerItems.add(PrinterItem.TERMINAL_ID);
            printerItems.add(PrinterItem.HOST);

            printerItems.add(PrinterItem.LINE);
            printerItems.add(PrinterItem.TRANS_TYPE);
            printerItems.add(PrinterItem.CARD_NO);
            printerItems.add(PrinterItem.BALANCE);

            printerItems.add(PrinterItem.LINE);
        }catch ( Exception e){
        Log.e(TAG,"Exception :" + e.getMessage());
            for (StackTraceElement m:e.getStackTrace()
                    ) {
                Log.e(TAG,"Exception :" + m );

            }

        }

}


}