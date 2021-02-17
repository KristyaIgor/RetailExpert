package md.intelectsoft.quickpos.verifone.transaction.canvas_printer;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;


/**
 * Created by Simon on 2019/5/14.
 */

public class PrintReceipt extends PrinterCanvas {
    public PrintReceipt(Context context) {
        super(context);
    }

    public String getAppParam(String key) {
        return "getAppParam";
//        return VFIApplication.getInstance().getAppParam(key);
    }

    public Resources getResources(){
        return context.getResources();
    }


    public void initializeData(Bundle extraItems ){
        super.initializeData(extraItems);
//        Log.d(TAG, "initializeData");
//        try {
//            Boolean isReprint = extraItems.getBoolean("reprint", false);
//
//
//            PrinterItem.LOGO.title.sValue = "verifone_logo2.jpg";   // addImageFromAssets(getApplication(), "verifone_logo2.jpg");
//            PrinterItem.LOGO.title.style = PrinterDefine.PStyle_align_center;
//
//            // MERCHANT NAME
//            PrinterItem.MERCHANT_NAME.title.sValue = getResources().getString(R.string.prn_merchantNmae);
//
//            // MERCHANT NO.
//            PrinterItem.MERCHANT_ID.title.sValue = getResources().getString(R.string.prn_merchantNo);
//
//            // TERMINAL NO
//            PrinterItem.TERMINAL_ID.title.sValue = getResources().getString(R.string.prn_terminalNo);
//
//            // OPERATOR NO
//            PrinterItem.OPERATOR_ID.title.sValue = getResources().getString(R.string.prn_operatorNo);
//
//            // ISSUE
//            PrinterItem.CARD_ISSUE.title.sValue = getResources().getString(R.string.prn_issue);
//
//            // CARD NO.
//            PrinterItem.CARD_NO.title.sValue = getResources().getString(R.string.prn_cardNo) ;
//
//            // EXP. DATE
//            PrinterItem.CARD_VALID.title = new PrinterElement(getResources().getString(R.string.prn_expDate));
//
//            // TRANS TYPE
//            PrinterItem.TRANS_TYPE.title.sValue = getResources().getString(R.string.prn_transType);
//
//            // BATCH NO.
//            PrinterItem.BATCH_NO.title.sValue = getResources().getString(R.string.prn_batchNo);
//
//            // TRACE NO.
//            PrinterItem.TRACK_NO.title.sValue = getResources().getString(R.string.prn_trackNo);
//
//            // AUTH NO.
//            PrinterItem.AUTH_NO.title.sValue = getResources().getString(R.string.prn_authNo);
//
//            // REF NO.
//            PrinterItem.REFER_NO.title.sValue = getResources().getString(R.string.prn_refNo);
//
//            // DATE/TIME
//            PrinterItem.DATE_TIME.title.sValue = getResources().getString(R.string.prn_dateTime);
//
//            // AMOUNT
//            PrinterItem.AMOUNT.title.sValue = getResources().getString(R.string.prn_amount);
//
//            PrinterItem.REFERENCE.title.sValue = getResources().getString(R.string.prn_reference);
//
//            PrinterItem.TC.title.sValue = getResources().getString(R.string.prn_tc);
//
//            // REPRINT
//            if (isReprint) {
//                PrinterItem.RE_PRINT_NOTE.title.sValue = getResources().getString(R.string.prn_reprint);
//            }
//
//            // CARDHOLDER SIGNATURE
//
//            PrinterItem.E_SIGN.title.sValue = getResources().getString(R.string.prn_signature);
//
//            PrinterItem.QRCODE_1.title.sValue = getResources().getString(R.string.prn_qrcode1);
//            PrinterItem.QRCODE_1.value.sValue = getResources().getString(R.string.prn_qrcode2);
//
//            PrinterItem.BARCODE_1.value.sValue = getResources().getString(R.string.prn_barcode);
//
//
//            PrinterItem.COMMENT_1.title.sValue = getResources().getString(R.string.prn_comment1);
//            PrinterItem.COMMENT_2.title.sValue = getResources().getString(R.string.prn_comment2);
//            PrinterItem.COMMENT_3.title.sValue = getResources().getString(R.string.prn_comment3);
//
//        }catch ( Exception e){
//            Log.e(TAG,"Exception :" + e.getMessage());
//            for (StackTraceElement m:e.getStackTrace()
//                    ) {
//                Log.e(TAG,"Exception :" + m );
//
//            }
//        }
    }

}
