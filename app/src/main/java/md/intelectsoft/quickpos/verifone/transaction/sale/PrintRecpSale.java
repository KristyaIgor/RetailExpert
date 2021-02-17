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
 * Created by Simon on 2019/5/15.
 */

public class PrintRecpSale extends TransPrinter {
    private static final String TAG = "PrintRecpSale";
    public PrintRecpSale(Context context) {
        super(context);

    }


    public String getCardTypeMode(int im_pan) {
        String cardTypeMode;
        switch (im_pan) {
            case 1:
                cardTypeMode = "M";
                break;
            case 2:
                cardTypeMode = "S";
                break;
            case 5:
                cardTypeMode = "C";
                break;
            case 7:
                cardTypeMode = "Q";
                break;
            default:
                cardTypeMode = "M";
                break;
        }
        return cardTypeMode;
    }

    // Here to "draw" your receipt with various PrinterItem
    public void initializeData(ISO8583 require, ISO8583 response, HostInformation hostInformation, Bundle extraItems ) {
        super.initializeData(require, response, hostInformation, extraItems);
        Log.d(TAG, "initializeData");
        try {

            //Get some extra infos about this receipt, like the index of copy and if it is reprint
            String tmp;
            int copyIndex = extraItems.getInt("copyIndex");
            Boolean isReprint = extraItems.getBoolean("reprint", false);

            // A List to put PrintItems, printCanvas will resolve this list to draw receipt
            printerItems = new ArrayList<>();

            // The LOGO on the top of receipt, set stype to align center.
            PrinterItem.LOGO.title.sValue = "verifone_logo.jpg";
            PrinterItem.LOGO.title.style = PrinterDefine.PStyle_align_center;
            printerItems.add(PrinterItem.LOGO);

            switch (copyIndex) {
                case 1:
                    tmp = getResources().getString(R.string.prn_merchantCopy); //"商户存根                           请妥善保管";
                    break;
                case 2:
                    tmp = getResources().getString(R.string.prn_cardholderCopy); //"持卡人存根                         请妥善保管";
                    break;
                case 3:
                default:
                    tmp = getResources().getString(R.string.prn_bankCopy); //"银行存根                           请妥善保管";
                    break;
            }
            PrinterItem.SUBTITLE.value.sValue = tmp;
            printerItems.add(PrinterItem.SUBTITLE);

            printerItems.add(PrinterItem.LINE);

//            // MERCHANT NAME
//            PrinterItem.MERCHANT_NAME.value.sValue = getAppParam(AppParam.System.cust_name);
//            printerItems.add(PrinterItem.MERCHANT_NAME);
//
//            // MERCHANT NO.
//            PrinterItem.MERCHANT_ID.value.sValue = getAppParam(AppParam.System.cust);
//            printerItems.add(PrinterItem.MERCHANT_ID);
//
//            // TERMINAL NO
//            PrinterItem.TERMINAL_ID.value.sValue = getAppParam(AppParam.System.terminal);
//            printerItems.add(PrinterItem.TERMINAL_ID);

            // OPERATOR NO
//            PrinterItem.OPERATOR_ID.value.sValue = getAppParam(AppParam.System.oper_no);
//            printerItems.add(PrinterItem.OPERATOR_ID);
//            printerItems.add(PrinterItem.LINE);

            // ISSUE
//            PrinterItem.CARD_ISSUE.value.sValue = extraItems.getString(TXNREC.ISSBANKNAME).trim();
//            printerItems.add(PrinterItem.CARD_ISSUE);

            // CARD NO.
//            String pansn = extraItems.getString(TXNREC.PANSN);
            String pan = TransactionParams.getInstance().getPan();
            PrinterItem.CARD_NO.title.sValue = getResources().getString(R.string.cardno);
            PrinterItem.CARD_NO.value.sValue = Utility.fixCardNoWithMask(pan);
//            if (pan != null && pan.trim().length() > 4) {
//                if (pansn != null && !pansn.isEmpty()) {
//                    PrinterItem.CARD_NO.title.sValue = getResources().getString(R.string.cardno1) + pansn.substring(1);
//                } else {
//                }
//                PrinterItem.CARD_NO.value.sValue = fixCardNumWithMask(pan);
//            } else {
//                Log.e(TAG, "No Card No. got!");
//            }
            printerItems.add(PrinterItem.CARD_NO);

            // EXP. DATE
            String expiredDate = extraItems.getString(TransactionParams.getInstance().getExpiredDate());
            if (expiredDate != null && !expiredDate.isEmpty()) {
                PrinterItem.CARD_VALID.value.sValue = expiredDate.substring(0, 4) + "/" + expiredDate.substring(0, 2);
            } else {
                Log.e(TAG, "no card expire date got");
            }
            printerItems.add(PrinterItem.CARD_VALID);

            // TRANS TYPE
            PrinterItem.TRANS_TYPE.value.sValue = extraItems.getString(TransactionParams.getInstance().getTransactionType());
            printerItems.add(PrinterItem.TRANS_TYPE);


            // BATCH NO. TODO
//            String batchNo = getAppParam(AppParam.System.batch_num);
//            PrinterItem.BATCH_NO.value.sValue = getAppParam(AppParam.System.batch_num);
//            printerItems.add(PrinterItem.BATCH_NO);

            // TRACE NO. TODO
//            String traceNo = extraItems.getString(TXNREC.TRACE);
//            if (traceNo != null && !traceNo.isEmpty()) {
//                PrinterItem.TRACK_NO.value.sValue = traceNo;
//                printerItems.add(PrinterItem.TRACK_NO);
//            }

            // AUTH NO. TODO
//            tmp = extraItems.getString(TXNREC.AUTHID);
//            if (tmp != null && !tmp.isEmpty()) {
//                PrinterItem.AUTH_NO.value.sValue = tmp;
//                printerItems.add(PrinterItem.AUTH_NO);
//            }

            // REF NO. TODO
//            String referenceNo = extraItems.getString(TXNREC.REFERNUM);
//            if (referenceNo != null && !referenceNo.isEmpty()) {
//                PrinterItem.REFER_NO.value.sValue = referenceNo;
//                printerItems.add(PrinterItem.REFER_NO);
//            }

            // DATE/TIME
//            String dateString = extraItems.getString(TXNREC.DATE) + extraItems.getString(TXNREC.TIME);
            String dateString = "1996-03-07";
            if (dateString != null && !dateString.isEmpty()) {
                dateString = Utility.getSystemDatetime();
                PrinterItem.DATE_TIME.value.sValue = Utility.getFormattedDateTime(dateString, "yyyyMMddHHmmss", "yyyy/MM/dd HH:mm:ss");
            } else {
                PrinterItem.DATE_TIME.value.sValue = "";
            }
            printerItems.add(PrinterItem.DATE_TIME);

            // AMOUNT
            String retamount = extraItems.getString(TransactionParams.getInstance().getTransactionAmount());
            if (retamount != null && !retamount.isEmpty()) {

                PrinterItem.AMOUNT.value.sValue = getResources().getString(R.string.prn_currency) + Utility.getReadableAmount(retamount);
                printerItems.add(PrinterItem.AMOUNT);
            }
            printerItems.add(PrinterItem.LINE);

            printerItems.add(PrinterItem.REFERENCE);

            // TC TODO
//            String ac = extraItems.getString(TXNREC.AC);
//            int im_pan = 0;
//            try {
//                im_pan = Integer.parseInt(extraItems.getString(TXNREC.MODE).substring(0, 2));
//            } catch (Exception e) {
//            }

//            if (ac != null && "C".equals(getCardTypeMode(im_pan))) {
//                PrinterItem.TC.value.sValue = ac;
//            }

            // REPRINT
            if (isReprint) {
                printerItems.add(PrinterItem.RE_PRINT_NOTE);
            }
            printerItems.add(PrinterItem.LINE);

            // CARDHOLDER SIGNATURE

            PrinterItem.E_SIGN.value.sValue = extraItems.getString(TransactionParams.getInstance().getEsignData());
            printerItems.add(PrinterItem.E_SIGN);

//            if (!printEsign()) {
//                printerItems.add(PrinterItem.FEED_LINE);
//                printerItems.add(PrinterItem.FEED_LINE);
//            }

            PrinterItem.QRCODE_1.value.sValue = getResources().getString(R.string.prn_qrcode2);

            PrinterItem.BARCODE_1.value.sValue = getResources().getString(R.string.prn_barcode);

            printerItems.add(PrinterItem.FEED);
            printerItems.add(PrinterItem.BARCODE_1);
            printerItems.add(PrinterItem.FEED);
            printerItems.add(PrinterItem.FEED);
            printerItems.add(PrinterItem.QRCODE_1);
            printerItems.add(PrinterItem.LINE);

            printerItems.add(PrinterItem.COMMENT_1);
            printerItems.add(PrinterItem.COMMENT_2);
            printerItems.add(PrinterItem.COMMENT_3);


        } catch (Exception e) {
            Log.e(TAG, "Exception :" + e.getMessage());
            for (StackTraceElement m : e.getStackTrace()
                    ) {
                Log.e(TAG, "Exception :" + m);

            }
        }
    }
}
