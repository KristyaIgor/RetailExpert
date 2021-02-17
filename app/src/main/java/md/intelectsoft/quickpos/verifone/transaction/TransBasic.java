package md.intelectsoft.quickpos.verifone.transaction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;

import com.vfi.smartpos.deviceservice.aidl.CheckCardListener;
import com.vfi.smartpos.deviceservice.aidl.EMVHandler;
import com.vfi.smartpos.deviceservice.aidl.IBeeper;
import com.vfi.smartpos.deviceservice.aidl.IDeviceService;
import com.vfi.smartpos.deviceservice.aidl.IEMV;
import com.vfi.smartpos.deviceservice.aidl.IExternalSerialPort;
import com.vfi.smartpos.deviceservice.aidl.IPinpad;
import com.vfi.smartpos.deviceservice.aidl.IPrinter;
import com.vfi.smartpos.deviceservice.aidl.ISerialPort;
import com.vfi.smartpos.deviceservice.aidl.IUsbSerialPort;
import com.vfi.smartpos.deviceservice.aidl.OnlineResultHandler;
import com.vfi.smartpos.deviceservice.aidl.PinInputListener;
import com.vfi.smartpos.deviceservice.aidl.PinpadKeyType;
import com.vfi.smartpos.deviceservice.aidl.key_manager.IDukpt;
import com.vfi.smartpos.deviceservice.constdefine.ConstCheckCardListener;
import com.vfi.smartpos.deviceservice.constdefine.ConstIPBOC;
import com.vfi.smartpos.deviceservice.constdefine.ConstIPinpad;
import com.vfi.smartpos.deviceservice.constdefine.ConstOnlineResultHandler;
import com.vfi.smartpos.deviceservice.constdefine.ConstPBOCHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import md.intelectsoft.quickpos.verifone.Utilities.Comm;
import md.intelectsoft.quickpos.verifone.Utilities.DeviceHelper;
import md.intelectsoft.quickpos.verifone.Utilities.Utility;
import md.intelectsoft.quickpos.verifone.basic.HostInformation;
import md.intelectsoft.quickpos.verifone.basic.ISO8583;
import md.intelectsoft.quickpos.verifone.caseA.ISO8583u;
import md.intelectsoft.quickpos.verifone.transaction.balance.BalancePrinter;
import md.intelectsoft.quickpos.verifone.transaction.sale.PrintRecpSale;
import md.intelectsoft.quickpos.verifone.usecase.EmvSetAidRid;
import md.intelectsoft.quickpos.verifone.usecase.MultiHostsConfig;

/**
 * Created by Simon on 2019/2/1.
 */

public class TransBasic {

    static String TAG = "EMVDemoIS-TransBasic";
    private static TransBasic instance;
    private IDeviceService idevice;
    private IEMV iemv;
    private IPinpad ipinpad;
    private IBeeper iBeeper;
    private IPrinter iPrinter;
    private IDukpt iDukpt;

    public static EMVHandler emvHandler;

    public static IExternalSerialPort iExternalSerialPort = null;    // for RS232 in the Base
    public static ISerialPort iSerialPort = null;    // for USB cable, the UART port the PC side. need install the driver
    public static IUsbSerialPort iUsbSerialPort = null;  // for OTG+USB2Serial

    public PinInputListener pinInputListener;
    private OnGetCardNoListener onGetCardNoListener;
    private OnInputPinConfirm onInputPinConfirm;

    // some client static
    String terminalID = "01020304";
    String merchantName = "X990 EMV Demo";
    String merchantID = "ABCDE0123456789";

    // keys
    int masterKeyID = 97;
    int workKeyId = 1;

    String pinKey_WorkKey = "B0BCE9315C0AA31E5E6667A037DE0AC4B0BCE9315C0AA31E";
    //    String pinKey_WorkKey = "B0BCE9315C0AA31E5E6667A037DE0AC4";
    String macKey = "";
    String masterKey = "758F0CD0C866348099109BAF9EADFA6E";

    //    String savedPan = "8880197100005603384";
    String savedPan = "0197100005603384";
    byte[] savedPinblock = null;

    /**
     * field-value map to save iso data
     */
    SparseArray<String> tagOfF55 = null;

    /**
     * \Brief the transaction type
     * <p>
     * Prefix
     * T_ means transaction
     * M_ means management
     */
    enum TransType {
        T_BALANCE, T_PURCHASE,
        M_LOGON
    }

    TransType mTransType;

    ISO8583 mIso8583;

    Handler handler;    // for UI
    public Context context;

    // for printer
    TransPrinter transPrinter;


    private TransBasic() {
    }

    public static TransBasic getInstance() {
        if (null == instance) {
            instance = new TransBasic();
        }
        return instance;
    }

    public void initTransBasic(Handler handler, Context context) {

        this.handler = handler;
        this.context = context;
        this.iemv = DeviceHelper.getInstance().getIemv();
        this.iBeeper = DeviceHelper.getInstance().getBeeper();
        this.ipinpad = DeviceHelper.getInstance().getPinPad();
        this.iDukpt = DeviceHelper.getInstance().getiDukpt();
        this.iSerialPort = DeviceHelper.getInstance().getSerialPort();

        initializeEMV();
        initializePinInputListener();
        TransPrinter.initialize();
    }

    Long amount = 0L;

    /**
     * \Brief make purchase fields
     */
    public void doPurchase() {

        // get transaction Amount from global field
        amount = Long.parseLong(TransactionParams.getInstance().getTransactionAmount());
        doTransaction(TransType.T_PURCHASE, -1);
    }


    /**
     * \Brief make balance fields
     */
    public void doBalance() {
        doTransaction(TransType.T_BALANCE, -1);
    }

    HostInformation hostInformation;

    /**
     * \Brief make logon fields
     */
    public void doLogon(int index) {
        doTransaction(TransType.M_LOGON, index);
    }

    void doTransaction(TransType transType, int index) {
        mTransType = transType;

        if (transType == TransType.M_LOGON) {
            mIso8583 = getIso8583Packet(index, "", "", transType);
            // management, no card need
            // start onlineRequest
            new Thread(onlineRequest).start();
        } else {
            transPrinter = null;
            switch (transType) {
                case T_PURCHASE:
                    transPrinter = new TransPrinter(this.context);
                    break;
                case T_BALANCE:
                    transPrinter = new BalancePrinter(this.context);
                    break;
            }
            // do search card and online request
            doSearchCard(transType);
        }
    }


    void doSearchCard(final TransType transType) {
        showUI("start check card\nUse you card please");
        mTransType = transType;
        Bundle cardOption = new Bundle();
        cardOption.putBoolean("supportCTLSCard", AppParams.getInstance().isSupportTap());
        cardOption.putBoolean("supportSmartCard", AppParams.getInstance().isSupportInsert());
        cardOption.putBoolean(ConstIPBOC.checkCard.cardOption.KEY_MagneticCard_boolean, AppParams.getInstance().isSupportSwipe());

//        iso8583 = iso8583u;

        try {
            iemv.checkCard(cardOption, 30, new CheckCardListener.Stub() {

                        @Override
                        public void onCardSwiped(Bundle track) throws RemoteException {
                            Log.d(TAG, "onCardSwiped ...");
//                            iemv.stopCheckCard();
//                            iemv.abortPBOC();

//                            iBeeper.startBeep(20);

                            String pan = track.getString(ConstCheckCardListener.onCardSwiped.track.KEY_PAN_String);
                            String track1 = track.getString(ConstCheckCardListener.onCardSwiped.track.KEY_TRACK1_String);
                            String track2 = track.getString(ConstCheckCardListener.onCardSwiped.track.KEY_TRACK2_String);
                            String track3 = track.getString(ConstCheckCardListener.onCardSwiped.track.KEY_TRACK3_String);
                            String serviceCode = track.getString(ConstCheckCardListener.onCardSwiped.track.KEY_SERVICE_CODE_String);

                            Log.d(TAG, "onCardSwiped ...1");
                            byte[] bytes = Utility.hexStr2Byte(track2);
                            Log.d(TAG, "Track2:" + track2 + " (" + Utility.byte2HexStr(bytes) + ")");

                            Boolean bIsKeyExist = ipinpad.isKeyExist(12, 1);
                            if (!bIsKeyExist) {
                                Log.e(TAG, "no key exist type: 12, @: 1");
                            }
                            byte[] enctypted = ipinpad.dukptEncryptData(1, 1, 1, bytes, new byte[]{0, 0, 0, 0, 0, 0, 0, 0,});
                            if (null == enctypted) {
                                Log.e(TAG, "NO DUKPT Encrypted got");
                            } else {
                                Log.d(TAG, "DUKPT:" + Utility.byte2HexStr(enctypted));
                            }
                            bIsKeyExist = ipinpad.isKeyExist(12, 1);
                            if (!bIsKeyExist) {
                                Log.e(TAG, "no key exist type: 12, @: 1");
                            }

                            mIso8583 = getIso8583Packet(-1, track2, "A000000668000000000276", mTransType);


                            if (null != track3) {
                                mIso8583.setValue(ISO8583.ATTRIBUTE.Track3, track3);
                            }
                            String validDate = track.getString(ConstCheckCardListener.onCardSwiped.track.KEY_EXPIRED_DATE_String);
                            if (null != validDate) {
                                mIso8583.setValue(ISO8583.ATTRIBUTE.DateOfExpired, validDate);
                            }
                            Log.d(TAG, "onCardSwiped ...3");
                            onlineRequest.run();
                            showUI("response:" + isoResponse.getField(ISO8583u.F_ResponseCode_39));

//                            printReceipt();
                        }

                        @Override
                        public void onCardPowerUp() throws RemoteException {
                            iemv.stopCheckCard();
                            iemv.abortEMV();
                            iBeeper.startBeep(200);

                            // set aid to kernel
                            List<String> tags = new ArrayList<String>();
                            tags.add("5F2A020840");
                            iemv.setEMVData(tags);
                            doEMV(ConstIPBOC.startEMV.intent.VALUE_cardType_smart_card, transType);
                        }

                        @Override
                        public void onCardActivate() throws RemoteException {
                            iemv.stopCheckCard();
                            iemv.abortEMV();
                            iBeeper.startBeep(200);

                            doEMV(ConstIPBOC.startEMV.intent.VALUE_cardType_contactless, transType);


//                            ITag AID_CARD = new TagImpl("4f", TagValueTypeEnum.BINARY, "Application Identifier (AID) - card", "Identifies the application as described in ISO/IEC 7816-5");
//                            byte[] tag4F = iemv.getCardData("4f");
//                            String[] aids = iemv.getAID(2);
//
//                            EmvParser parser = new EmvParser(mProvider, true);
//                            try {
//                                EmvCard mCard = parser.readEmvCard();
//
//
//                                if (mCard != null) {
//                                    if (StringUtils.isNotBlank(mCard.getCardNumber())) {
//                                       String mCardNumber = mCard.getCardNumber();
//                                        String mExpireDate = mCard.getExpireDate();
//                                        String mCardType = mCard.getType().toString();
//                                        if (mCardType.equals(EmvCardScheme.UNKNOWN.toString())){
//                                            Log.d("creditCardNfcReader", "UNKNOWN_CARD_MESS");
//                                        }
//                                    }
//                                }
//                            } catch (CommunicationException e) {
//                                e.printStackTrace();
//                            }


                        }

                        @Override
                        public void onTimeout() throws RemoteException {
                            showUI("timeout");
                        }

                        @Override
                        public void onError(int error, String message) throws RemoteException {
                            showUI("error:" + error + ", msg:" + message);
                        }
                    }
            );
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    /**
     * \brief sample of EMV
     * <p>
     * \code{.java}
     * \endcode
     *
     * @see
     */
    void doEMV(int type, TransType transType) {
        //
        Log.i(TAG, "start EMV demo");

        Bundle emvIntent = new Bundle();
        emvIntent.putInt(ConstIPBOC.startEMV.intent.KEY_cardType_int, type);
        if (transType == TransType.T_PURCHASE) {
            emvIntent.putLong(ConstIPBOC.startEMV.intent.KEY_authAmount_long, amount);
        }
        emvIntent.putString(ConstIPBOC.startEMV.intent.KEY_merchantName_String, merchantName);

        emvIntent.putString(ConstIPBOC.startEMV.intent.KEY_merchantId_String, merchantID);  // 010001020270123
        emvIntent.putString(ConstIPBOC.startEMV.intent.KEY_terminalId_String, terminalID);   // 00000001
        emvIntent.putBoolean(ConstIPBOC.startEMV.intent.KEY_isSupportQ_boolean, ConstIPBOC.startEMV.intent.VALUE_supported);
//        emvIntent.putBoolean(ConstIPBOC.startEMV.intent.KEY_isSupportQ_boolean, ConstIPBOC.startEMV.intent.VALUE_unsupported);
        emvIntent.putBoolean(ConstIPBOC.startEMV.intent.KEY_isSupportSM_boolean, ConstIPBOC.startEMV.intent.VALUE_supported);
        emvIntent.putBoolean(ConstIPBOC.startEMV.intent.KEY_isQPBOCForceOnline_boolean, ConstIPBOC.startEMV.intent.VALUE_unforced);
        if (type == ConstIPBOC.startEMV.intent.VALUE_cardType_contactless) {   // todo, check here
            emvIntent.putByte(ConstIPBOC.startEMV.intent.KEY_transProcessCode_byte, (byte) 0x00);
        }
        emvIntent.putBoolean("isSupportPBOCFirst", false);
//        emvIntent.putString(ConstIPBOC.startEMV.intent.KEY_transCurrCode_String, "0156");
//        emvIntent.putString(ConstIPBOC.startEMV.intent.KEY_otherAmount_String, "0");

        try {
            iemv.startEMV(ConstIPBOC.startEMV.processType.full_process, emvIntent, emvHandler);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    public void initializeEMV() {

        /**
         * \brief initialize the call back listener of EMV
         *
         *  \code{.java}
         * \endcode
         * @version
         * @see
         *
         */
        emvHandler = new EMVHandler.Stub() {
            @Override
            public void onRequestAmount() throws RemoteException {
                // this is an deprecated callback
                // need set Amount while calling startEMV
            }

            @Override
            public void onSelectApplication(List<Bundle> appList) throws RemoteException {
                for (Bundle aidBundle : appList) {
                    String aidName = aidBundle.getString("aidName");
                    String aid = aidBundle.getString("aid");
                    String aidLabel = aidBundle.getString("aidLabel");
                    Log.i(TAG, "AID Name=" + aidName + " | AID Label=" + aidLabel + " | AID=" + aid);
                }
                showUI("onSelectApplication..." + appList.get(0));
                iemv.importAppSelection(0);
            }

            /**
             * \brief confirm the card info
             *
             * show the card info and import the confirm result
             * \code{.java}
             * \endcode
             *
             */
            @Override
            public void onConfirmCardInfo(Bundle info) throws RemoteException {
                Log.d(TAG, "onConfirmCardInfo...");
                savedPan = info.getString(ConstPBOCHandler.onConfirmCardInfo.info.KEY_PAN_String);
                onGetCardNoListener.onGetCardNo(savedPan);
                TransactionParams.getInstance().setPan(savedPan);

                String result = "onConfirmCardInfo callback, " +
                        "\nPAN:" + savedPan +
                        "\nTRACK2:" + info.getString(ConstPBOCHandler.onConfirmCardInfo.info.KEY_TRACK2_String) +
                        "\nCARD_SN:" + info.getString(ConstPBOCHandler.onConfirmCardInfo.info.KEY_CARD_SN_String) +
                        "\nSERVICE_CODE:" + info.getString(ConstPBOCHandler.onConfirmCardInfo.info.KEY_SERVICE_CODE_String) +
                        "\nEXPIRED_DATE:" + info.getString(ConstPBOCHandler.onConfirmCardInfo.info.KEY_EXPIRED_DATE_String);

                // read card data
                byte[] tlv = iemv.getCardData("9F51");
                result += ("\n9F51:" + Utility.byte2HexStr(tlv));

                int[] tagList = {
                        0x4F,   // Application Identifier (AID) – card
                        0x9F06, // Application Identifier (AID) – terminal
                        0x98,
                        0x50,
                        0x9F12,
                        0x9F26,
                        0x9F27,
                        0x9F10,
                        0x9F37,
                        0x9F36,
                        0x95,
                        0x9A,
                        0x9C,
                        0x9F02,
                        0x5F2A,
                        0x82,
                        0x9F1A,
                        0x9F03,
                        0x9F33,
                        0x9F74,
                        0x9F24,
                        0x5F36
                };
                String aid = "";
                for (int tag : tagList) {
                    tlv = iemv.getCardData(Integer.toHexString(tag).toUpperCase());
                    if (null != tlv && tlv.length > 0) {
                        Log.d(TAG, "CardData:" + Integer.toHexString(tag).toUpperCase() + ", value:" + Utility.byte2HexStr(tlv));
                        switch (tag) {
                            case 0x4F:
                                aid = Utility.byte2HexStr(tlv);
                                break;
                        }
                    } else {
                        Log.e(TAG, "getCardData:" + Integer.toHexString(tag) + ", fails");
                    }
                }

                String track2 = info.getString(ConstPBOCHandler.onConfirmCardInfo.info.KEY_TRACK2_String);
                if (null != track2) {
                    int a = track2.indexOf('D');
                    if (a > 0) {
                        track2 = track2.substring(0, a);
                    }
//                    data8583.put(ISO8583u.F_Track_2_Data_35, track2);
                }
                mIso8583 = getIso8583Packet( -1, track2, aid, mTransType );

                mIso8583.setValue(ISO8583.ATTRIBUTE.Track2, track2);

                showUI("onConfirmCardInfo:" + result);

//                importCardConfirmResult();

            }

            /**
             * \brief show the pin pad
             *
             *  \code{.java}
             * \endcode
             *
             */
            @Override
            public void onRequestInputPIN(boolean isOnlinePin, int retryTimes) throws RemoteException {
                showUI("onRequestInputPIN isOnlinePin:" + isOnlinePin + ",Retry Times:" + retryTimes);
                // show the pin pad, import the pin block
                TransactionParams.getInstance().setOnlinePin(isOnlinePin);
                TransactionParams.getInstance().setRetryTimes(retryTimes);


            }

            @Override
            public void onConfirmCertInfo(String certType, String certInfo) throws RemoteException {
                showUI("onConfirmCertInfo, type:" + certType + ",info:" + certInfo);

                iemv.importCertConfirmResult(ConstIPBOC.importCertConfirmResult.option.CONFIRM);
            }

            @Override
            public void onRequestOnlineProcess(Bundle aaResult) throws RemoteException {
                Log.d(TAG, "onRequestOnlineProcess...");
                int result = aaResult.getInt(ConstPBOCHandler.onRequestOnlineProcess.aaResult.KEY_RESULT_int);
//                boolean signature = aaResult.getBoolean(ConstPBOCHandler.onRequestOnlineProcess.aaResult.KEY_SIGNATURE_boolean);
                boolean signature = false;
                showUI("onRequestOnlineProcess result=" + result + " signal=" + signature);
                switch (result) {
                    case ConstPBOCHandler.onRequestOnlineProcess.aaResult.VALUE_RESULT_AARESULT_ARQC:
                    case ConstPBOCHandler.onRequestOnlineProcess.aaResult.VALUE_RESULT_QPBOC_ARQC:
                        showUI(aaResult.getString(ConstPBOCHandler.onRequestOnlineProcess.aaResult.KEY_ARQC_DATA_String));
                        break;
                    case ConstPBOCHandler.onRequestOnlineProcess.aaResult.VALUE_RESULT_PAYPASS_EMV_ARQC:
                        break;
                }

                showUI("CVM = " + aaResult.getInt("CTLS_CVMR"));

                tagOfF55 = new SparseArray<>();
                // read card data
                byte[] tlv;
                int[] tagList = {
                        0x4F,
                        0x9F06,
                        0x50,
                        0x9F12,
                        0x9F26,
                        0x9F27,
                        0x9F10,
                        0x9F37,
                        0x9F36,
                        0x95,
                        0x9A,
                        0x9C,
                        0x9F02,
                        0x5F2A,
                        0x82,
                        0x9F1A,
                        0x9F03,
                        0x9F33,
                        0x9F74,
                        0x9F24,
                        0x98
                };

                for (int tag : tagList) {
                    tlv = iemv.getCardData(Integer.toHexString(tag).toUpperCase());
                    if (null != tlv && tlv.length > 0) {
                        Log.d(TAG, "CardData:" + Integer.toHexString(tag).toUpperCase() + ", value:" + Utility.byte2HexStr(tlv));
                        tagOfF55.put(tag, Utility.byte2HexStr(tlv));  // build up the field 55
                    } else {
                        Log.e(TAG, "getCardData:" + Integer.toHexString(tag) + ", fails");
                    }
                }

                // set the pin block
                // data8583.put(ISO8583u.F_PINData_52, Utility.byte2HexStr(savedPinblock));
                mIso8583.setField(ISO8583u.F_PINData_52, Utility.byte2HexStr(savedPinblock));


                Log.d(TAG, "start online request");
                onlineRequest.run();
                Log.d(TAG, "online request finished");

                // import the online result
                Bundle onlineResult = new Bundle();
                onlineResult.putBoolean(ConstIPBOC.inputOnlineResult.onlineResult.KEY_isOnline_boolean, true);
                if (isoResponse.unpackValidField[ISO8583u.F_ResponseCode_39]) {
                    onlineResult.putString(ConstIPBOC.inputOnlineResult.onlineResult.KEY_field55_String, isoResponse.getUnpack(ISO8583u.F_ResponseCode_39));
                } else {
                    onlineResult.putString(ConstIPBOC.inputOnlineResult.onlineResult.KEY_respCode_String, "00");
                }

                if (isoResponse.unpackValidField[ISO8583u.F_AuthorizationIdentificationResponseCode_38]) {
                    //
                    onlineResult.putString(ConstIPBOC.inputOnlineResult.onlineResult.KEY_authCode_String, isoResponse.getUnpack(ISO8583u.F_AuthorizationIdentificationResponseCode_38));
                } else {
                    onlineResult.putString(ConstIPBOC.inputOnlineResult.onlineResult.KEY_authCode_String, "123456");
                }

                // onlineResult.putString( ConstIPBOC.inputOnlineResult.onlineResult.KEY_field55_String, "910A1A1B1C1D1E1F2A2B30307211860F04DA9F790A0000000100001A1B1C1D");
                if (isoResponse.unpackValidField[55]) {
                    onlineResult.putString(ConstIPBOC.inputOnlineResult.onlineResult.KEY_field55_String, isoResponse.getUnpack(55));

                } else {
                    onlineResult.putString(ConstIPBOC.inputOnlineResult.onlineResult.KEY_field55_String, "5F3401019F3303E0F9C8950500000000009F1A0201569A039707039F3704F965E43082027C009F3602041C9F260805142531F709C8669C01009F02060000000000125F2A0201569F101307010103A02000010A01000000000063213EC29F2701809F1E0831323334353637389F0306000000000000");
                }
//                onlineResult.putBoolean("getPBOCData", true);
//                onlineResult.putInt("importAppSelectResult", 1);
//                onlineResult.putInt("IsPinInput", 1);
//                onlineResult.putString("importPIN", "123456");
//                onlineResult.putInt("importAmount", 101);
//                onlineResult.putBoolean("cancelCardConfirmResult", false);


                iemv.inputOnlineResult(onlineResult, new OnlineResultHandler.Stub() {
                    @Override
                    public void onProccessResult(int result, Bundle data) throws RemoteException {
                        Log.i(TAG, "onProccessResult callback:");
                        String str = "RESULT:" + result +
                                "\nTC_DATA:" + data.getString(ConstOnlineResultHandler.onProccessResult.data.KEY_TC_DATA_String, "not defined") +
                                "\nSCRIPT_DATA:" + data.getString(ConstOnlineResultHandler.onProccessResult.data.KEY_SCRIPT_DATA_String, "not defined") +
                                "\nREVERSAL_DATA:" + data.getString(ConstOnlineResultHandler.onProccessResult.data.KEY_REVERSAL_DATA_String, "not defined");
                        showUI(str);

                        String resultTLV = data.getString("TC_TLV");
                        Log.d(TAG, "CardData :" + "onResult TLV: " + resultTLV);
                        Log.d(TAG, "CardData :" + "onResult 98: " + Arrays.toString(iemv.getCardData("98")));

                        String[] tlv_tag_list = {"98"};

                        Log.d(TAG, "CardData :" + "onResult tlvlist: " + iemv.getAppTLVList(tlv_tag_list));
                        Log.d(TAG, "CardData :" + "onResult emvdata: " + iemv.getEMVData("98"));

                        switch (result) {
                            case ConstOnlineResultHandler.onProccessResult.result.TC:
                                showUI("TC");
                                break;
                            case ConstOnlineResultHandler.onProccessResult.result.Online_AAC:
                                showUI("Online_AAC");
                                break;
                            default:
                                showUI("error, code:" + result);
                                break;
                        }
                        Log.d(TAG, "Try to print");
//                        if( null != transPrinter ){
//                            printReceipt();
//                        }
                    }
                });
            }

            @Override
            public void onTransactionResult(int result, Bundle data) throws RemoteException {
                Log.d(TAG, "onTransactionResult");
                String msg = data.getString("ERROR");
                showUI("onTransactionResult result = " + result + ",msg = " + msg);

                switch (result) {

                    case ConstPBOCHandler.onTransactionResult.result.EMV_CARD_BIN_CHECK_FAIL:
                        // read card fail
                        showUI("read card fail");
                        return;
                    case ConstPBOCHandler.onTransactionResult.result.EMV_MULTI_CARD_ERROR:
                        // multi-cards found
                        showUI(data.getString(ConstPBOCHandler.onTransactionResult.data.KEY_ERROR_String));
                        return;
                }

            }
        };
    }


    /**
     * \brief set main key and work key
     * <p>
     * \code{.java}
     * \endcode
     *
     * @see
     */
    public void doSetKeys() {
        // Load Main key
        // 758F0CD0C866348099109BAF9EADFA6E
        boolean bRet;
        try {
            bRet = ipinpad.loadMainKey(masterKeyID, Utility.hexStr2Byte(masterKey), null);
            showUI("loadMainKey:" + bRet);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        String DukptSN = "01020304050607080900";
        String dukptKey = "343434343434343434343434343434343434343434343434";

        // Sudarson's dukpt issue
//        String DukptSN = "FFFF9876543210E10004";
//        String dukptKey = "C1D0F8FB4958670DBA40AB1F3752EF0D";

        // Load work key
        // 89B07B35A1B3F47E89B07B35A1B3F488
        try {
            bRet = ipinpad.loadWorkKey(PinpadKeyType.PINKEY, masterKeyID, workKeyId, Utility.hexStr2Byte(pinKey_WorkKey), null);
            showUI("loadWorkKey:" + bRet);

            int keyId = 1;
            String ksn = "FFFF9876543210E10001";
            String key = "C1D0F8FB4958670DBA40AB1F3752EF0D";

//            bRet = ipinpad.loadDukptKey(1, Utility.hexStr2Byte(DukptSN), Utility.hexStr2Byte(dukptKey), null);
            Bundle extend = new Bundle();
            extend.putBoolean("isPlainKey", true);
            extend.putBoolean("KSNAutoIncrease", true);
            bRet = iDukpt.loadDukptKey(1, Utility.hexStr2Byte(ksn), Utility.hexStr2Byte(key), null, extend);
            Log.d("PINPAD", "loadDukptKey:" + bRet);

            showUI("loadDukptKey:" + bRet);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    /**
     * \brief show the pinpad
     * <p>
     * \code{.java}
     * \endcode
     */
    public void doPinPad(boolean isOnlinePin, int retryTimes) {
        Bundle param = new Bundle();
        Bundle globeParam = new Bundle();
        String panBlock = savedPan;
        byte[] pinLimit = {0, 4, 5, 6}; // 0 means bypass pin input
        param.putString("promptString", "DEMO-EMV PINPAD");
        param.putString("promptsFont", "/system/fonts/DroidSans-Bold.ttf");
        param.putByteArray("displayKeyValue", new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        param.putByteArray(ConstIPinpad.startPinInput.param.KEY_pinLimit_ByteArray, pinLimit);
        param.putInt(ConstIPinpad.startPinInput.param.KEY_timeout_int, 20);
        param.putBoolean(ConstIPinpad.startPinInput.param.KEY_isOnline_boolean, isOnlinePin);
        param.putString(ConstIPinpad.startPinInput.param.KEY_pan_String, panBlock);
        param.putInt(ConstIPinpad.startPinInput.param.KEY_desType_int, ConstIPinpad.startPinInput.param.Value_desType_DUKPT_3DES);
        if (!isOnlinePin) {
            param.putString(ConstIPinpad.startPinInput.param.KEY_promptString_String, "OFFLINE PIN, retry times:" + retryTimes);
        }
//        globeParam.putString( ConstIPinpad.startPinInput.globleParam.KEY_Display_One_String, "[1]");
        try {
            ipinpad.startPinInput(1, param, globeParam, pinInputListener);
            iDukpt.getDukptKsn(1);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    /**
     * \brief initialize the pin pad listener
     * <p>
     * \code{.java}
     * \endcode
     */
    public void initializePinInputListener() {
        pinInputListener = new PinInputListener.Stub() {
            @Override
            public void onInput(int len, int key) throws RemoteException {
                Log.d(TAG, "PinPad onInput, len:" + len + ", key:" + key);
            }

            @Override
            public void onConfirm(byte[] data, boolean isNonePin) throws RemoteException {
                Log.d(TAG, "PinPad onConfirm");
                iemv.importPin(1, data);
                savedPinblock = data;
                onInputPinConfirm.onConfirm();
            }

            @Override
            public void onCancel() throws RemoteException {
                Log.d(TAG, "PinPad onCancel");
            }

            @Override
            public void onError(int errorCode) throws RemoteException {
                Log.d(TAG, "PinPad onError, code:" + errorCode);
            }
        };
    }

    public void doSetAID(int type) {
        showUI("Set AID start");
        EmvSetAidRid emvSetAidRid = new EmvSetAidRid(iemv);
        emvSetAidRid.setAID(type);
        if (type == 2) {

        } else {
            try {
                iBeeper.startBeep(200);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        showUI("Set AID DONE");
    }

    public void doSetRID(int type) {
//        showUI("Set RID start");
//        EmvSetAidRid emvSetAidRid = new EmvSetAidRid(iemv);
//        emvSetAidRid.setRID(type);
//        if (type == 2) {
//
//        } else {
//            try {
//                iBeeper.startBeep(200);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//        }
        showUI("Set RID DONE");
    }


    Handler onlineResponse = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("value");
            Log.i(TAG, "handle message:" + val);
        }
    };

    public ISO8583 isoResponse = null;

    Runnable onlineRequest = new Runnable() {
        @Override
        public void run() {

            if (tagOfF55 != null) {
                for (int i = 0; i < tagOfF55.size(); i++) {
                    int tag = tagOfF55.keyAt(i);
                    String value = tagOfF55.valueAt(i);
                    if (value.length() > 0) {
                        byte[] tmp = mIso8583.appendF55(tag, value);
                        if (tmp == null) {
                            Log.e(TAG, "error of tag:" + Integer.toHexString(tag) + ", value:" + value);
                        } else {
                            Log.d(TAG, "append F55 tag:" + Integer.toHexString(tag) + ", value:" + Utility.byte2HexStr(tmp));
                        }
                    }
                }
                tagOfF55 = null;

            }
//            byte[] packet = iso8583u.makePacket(data8583, ISO8583.PACKET_TYPE.PACKET_TYPE_HEXLEN_BUF);
            byte[] packet = mIso8583.getPacket(ISO8583.PACKET_TYPE.PACKET_TYPE_HEXLEN_BUF);

            Comm comm = new Comm(hostInformation.hostAddr, hostInformation.hostPort);
            if (false == comm.connect()) {
                Log.e(TAG, "connect server error");
                return;
            }

            comm.send(packet);
            byte[] response = comm.receive(1024, 30);
            if (null == response) {
                Log.e(TAG, "receive error");
            }
            comm.disconnect();

            if (response == null) {
                Log.e(TAG, "Test fails");
            } else {
                Log.i(TAG, "Test return length:" + response.length);
                Log.i(TAG, Utility.byte2HexStr(response));
                isoResponse = hostInformation.new8583();
                if (isoResponse.unpack(response, 2)) {
                    String message = "";
                    String s;
                    String type = "";

                    s = isoResponse.getUnpack(0);
                    if (null != s) {
                        type = s;
                        message += "Message Type:";
                        message += s;
                        message += "\n";
                    }

                    s = isoResponse.getUnpack(39);
                    if (null != s) {
                        message += "Response(39):";
                        message += s;
                        message += "\n";
                    }
                    if (type.equals("0810")) {
                        s = isoResponse.getUnpack(62 + 200);
                        if (null != s) {
                            Log.d(TAG, "Field62:" + s);
                            if (s.length() == 48) {
                                pinKey_WorkKey = s.substring(0, 32);
                                macKey = s.substring(32, 48);
                            } else if (s.length() == 80) {
                                pinKey_WorkKey = s.substring(0, 64);
                                macKey = s.substring(64, 80);
                            } else if (s.length() == 120) {
                                pinKey_WorkKey = s.substring(0, 64);
                                macKey = s.substring(64, 80);
                            }
                            message += "pinKey:";
                            message += pinKey_WorkKey;
                            message += "\n";
                            message += "macKey:";
                            message += macKey;
                            message += "\n";
                        }
                    } else if (type.equals("0210")) {
                        s = isoResponse.getUnpack(54);  // 1002764C000123456000
                        if (null != s && s.length() > 10) {
                            message += "Balance(54):";
                            message += s.substring(0, 2) + "," + s.substring(2, 4) + "," + s.substring(4, 7) + "," + s.substring(7, 8);
                            message += "\n" + Integer.valueOf(s.substring(8, s.length() - 1));
                            message += "\n";
                        }
                    }
                    showUI(message);
                }
            }
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", "receive finished");
            msg.setData(data);
            onlineResponse.sendMessage(msg);

        }
    };

    /**
     * get the 8583 packet and initialize some data without card information
     *
     * @param index     , the index of the host defined in MultiHostsConfig
     *                  -1 means use the cardBin or AID to find the host from MultiHostsConfig
     * @param cardBin   , the card BIN of the card to select which host & 8583 package to do the transaction
     * @param AID       , the AID of smart card to select which host & 8583 package to do the transaction.
     * @param transType , transaction type to make various packet
     * @return the ISO8583 object to set the field-value to make transaction.
     */
    ISO8583 getIso8583Packet(int index, String cardBin, String AID, TransType transType) {
        ISO8583 iso8583;

        Date dt = new Date();

        // get hostInformation first
        if (index >= 0) {
            hostInformation = MultiHostsConfig.getHost(index, null, null);
        } else {
            if(hostInformation == null)
                MultiHostsConfig.initialize();
            hostInformation = MultiHostsConfig.getHost(index, cardBin, AID);
        }

        iso8583 = hostInformation.new8583();    // make the iso8583 from various child class

        // set some attribute for 8583
        iso8583.setValue(ISO8583.ATTRIBUTE.MerchantID, hostInformation.merchantID);
        iso8583.setValue(ISO8583.ATTRIBUTE.TerminalID, hostInformation.terminalID);

        iso8583.setValue(ISO8583.ATTRIBUTE.Time, hostInformation.timeFormat.format(dt));
        iso8583.setValue(ISO8583.ATTRIBUTE.Date, hostInformation.dateFormat.format(dt));

        // by identify
        switch (hostInformation.category) {
            case Default:
                Log.d(TAG, "is default:" + hostInformation.description);
                break;
            case VISA:
                Log.d(TAG, "is VISA:" + hostInformation.description);
                break;
            case Bitmap128:
                Log.d(TAG, "is 123 Bitmap:" + hostInformation.description);
                iso8583.setField(67, "123456");
                break;
        }

        // by trans type
        switch (transType) {
            case T_PURCHASE:
                iso8583.setValue(ISO8583.ATTRIBUTE.Amount, String.valueOf(amount));
                break;
        }

        // by trans type
        switch (transType) {
            case M_LOGON:
                // Logon or key load
                Log.d(TAG, "is Sign in");

                iso8583.setValue(ISO8583.ATTRIBUTE.MessageType, "0800");

                switch (hostInformation.category) {
                    case Default:
                        iso8583.setField(11, "012345");
                        iso8583.setField(32, "12345678");
                        iso8583.setField(37, "ABCDEF123456");
                        iso8583.setField(39, "00");
                        iso8583.setField(60, "000008673720");
                        iso8583.setField(62, "10O");
                        iso8583.setField(63, "001");
                        break;
                    case VISA:
                        break;
                    default:
                        break;
                }
                break;
            case T_BALANCE:
                Log.d(TAG, "is balance");

                switch (hostInformation.category) {
                    case Default:
                        iso8583.setValue(ISO8583.ATTRIBUTE.MessageType, "0200");
                        iso8583.setField(3, "310000");
                        iso8583.setField(11, "010203");
                        iso8583.setField(14, "9912");
                        iso8583.setField(22, "020");  // mag
                        iso8583.setField(25, "00");
                        iso8583.setField(49, "156");  // RMB
                        iso8583.setField(60, "01654321");

                        break;
                    case VISA:
                        iso8583.setValue(ISO8583.ATTRIBUTE.MessageType, "0200");
                        iso8583.setField(2, savedPan);
                        iso8583.setField(3, "310000");
                        iso8583.setField(11, "010203");
                        iso8583.setField(14, "9912");
                        iso8583.setField(22, "020");  // mag
                        //        data8583_u_balance.put(22, "021");  // mag + pin
                        iso8583.setField(25, "00");
                        //        data8583_u_balance.put(26, ""); // pin
                        iso8583.setField(35, ""); // track 2

                        iso8583.setField(49, "156");  // RMB
                        //        data8583_u_balance.put(55, ""); // IC
                        iso8583.setField(60, "01654321");
                        break;
                    default:
                        break;
                }
                break;
            case T_PURCHASE:
                Log.d(TAG, "is purchase");

                iso8583.setField(3, "00 00 00");
                iso8583.setField(11, "01 02 03");
                iso8583.setField(22, "02 1");   // 02 mag, 05 smart, 07 ctls; 1 pin
                iso8583.setField(25, "00");
                iso8583.setField(49, "156");

//                data8583_u_purchase.put(ISO8583u.F_AccountNumber_02, "");
                iso8583.setValue(ISO8583.ATTRIBUTE.Amount, "15");

                switch (hostInformation.category) {
                    case Default:
                        iso8583.setValue(ISO8583.ATTRIBUTE.MessageType, "0200");
                        // iso8583.setField( 1, "");

                        iso8583.setField(32, "12345678");
                        iso8583.setField(37, "ABCDEF123456");
                        iso8583.setField(39, "00");
                        iso8583.setField(60, "000008673720");
                        iso8583.setField(62, "10O");
                        iso8583.setField(63, "001");
                        break;
                    case VISA:
                        iso8583.setValue(ISO8583.ATTRIBUTE.MessageType, "0200");
                        break;
                    default:
                        break;
                }
                break;
            default:
                switch (hostInformation.category) {
                    case Default:
                        break;
                    case VISA:
                        break;
                    default:
                        break;
                }
                break;
        }
        return iso8583;
    }

    public void abortEMV() {
        try {
            iemv.stopCheckCard();
            iemv.abortEMV();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    void showUI(String str) {
        Message msg = new Message();
        msg.getData().putString("msg", str);
        handler.sendMessage(msg);
    }


    public void printTest(int index) {

//        TransPrinter transPrinter = new TransPrinter(this.context);
//        TransPrinter.initialize(iPrinter);
//
//        transPrinter.print(1);
//        switch (index) {
//            case 1:
//                transPrinter = new PrintRecpSale(this.context);
//                break;
//            case 2:
//                transPrinter = new BalancePrinter(this.context);
//                break;
//            default:
//                transPrinter = new TransPrinter(this.context);
//                break;
//        }
        transPrinter = new PrintRecpSale(this.context);
        printReceipt();
    }

    //Judge transaction type : card/QR
    public void printReceipt() {
        Log.d(TAG, "initialize default receipt data");

        Bundle bundle = new Bundle();

        transPrinter.initializeData(mIso8583, isoResponse, hostInformation, bundle);
        transPrinter.print();
        Log.d(TAG, "print");
        transPrinter.print();
    }


    public void importCardConfirmResult() {
        Message msg = new Message();
        msg.what = TransactionParams.IMPORT_CARD_CONFIRM_RESULT;
        handleImport.sendMessage(msg);
    }

    public void doPinPad() {
        Message msg = new Message();
        msg.what = TransactionParams.IMPORT_PIN;
        handleImport.sendMessage(msg);
    }

    @SuppressLint("HandlerLeak")
    Handler handleImport = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                switch (msg.what) {
                    case TransactionParams.IMPORT_APP_SELECT:
                        iemv.importAppSelection(0);
                        break;
                    case TransactionParams.IMPORT_CARD_CONFIRM_RESULT:
                        iemv.importCardConfirmResult(ConstIPBOC.importCardConfirmResult.pass.allowed);
                        break;
                    case TransactionParams.IMPORT_PIN:
                        boolean isOnlinePin = TransactionParams.getInstance().isOnlinePin();
                        int retryTimes = TransactionParams.getInstance().getRetryTimes();
                        doPinPad(isOnlinePin, retryTimes);
                        break;
                    case TransactionParams.INPUT_ONLINE_RESULT:
                        break;
                    case TransactionParams.IMPORT_CERT_CONFIRM_RESULT:
                        break;
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    };


    public interface OnGetCardNoListener {
        void onGetCardNo(String pan);
    }

    public interface OnCardCheckedListener {
        void onSuccessful(String pan);

        void onError();

        void onTimeOut();
    }

    public void setOnGetCardNoListener(OnGetCardNoListener onGetCardNoListener) {
        this.onGetCardNoListener = onGetCardNoListener;
    }

    public interface OnInputPinConfirm {
        void onConfirm();
    }

    public void setOnInputPinConfirm(OnInputPinConfirm onInputPinConfirm) {
        this.onInputPinConfirm = onInputPinConfirm;
    }
}
