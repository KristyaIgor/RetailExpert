package md.intelectsoft.quickpos.verifone.basic;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import md.intelectsoft.quickpos.verifone.usecase.MultiHostsConfig;

/**
 * Created by Simon on 2019/1/23.
 */

public class HostInformation<T extends ISO8583> {
    private static final String TAG = "EMVDemo-HostInformation";

    public static final int TYPE_TIMEOUT_CONNECT = 0;
    public static final int TYPE_TIMEOUT_RECEIVE = 1;


    public String description;
    public int index;

    public MultiHostsConfig.Category category;

    // merchant
    public String terminalID;
    public String merchantID;
    public String merchantName;

    // host
    public String hostAddr;
    public int hostPort;

    public int[] timeout;  // 0, connect. 1, receive

    // cards
    public List<String> cardBinList;
    public List<String> AIDList;

    // keys
    public int masterKeyIndex ;
    public int pinKeyIndex ;
    public int dataKeyIndex;    // TD KEY
    public int macKeyIndex;
    public int dukptIndex;

    // date time format/pattern
    public SimpleDateFormat dateFormat;
    public SimpleDateFormat timeFormat;

    // 8583
    public Class<T> iso8583Class;
    public T iso8583;

    // print setting
    public String prn_card_mask = null;
    public int[] prn_card_mask_range;   // [a,b], a -> start offset, b -> end offset ,  negative value mean from end to start. such as [6 , -5] means mask from [6] to [len-5]

    public HostInformation(T t, MultiHostsConfig.Category category, String description){
        iso8583Class = (Class<T>) t.getClass();
        iso8583 = t;
        this.category = category;
        this.description = description;
        this.AIDList = null;
        this.cardBinList = null;

        this.dateFormat = new SimpleDateFormat(t.DATA_FORMAT);
        this.timeFormat = new SimpleDateFormat(t.TIME_FORMAT);

        timeout = new int[2];
        timeout[TYPE_TIMEOUT_CONNECT] = 60;
        timeout[TYPE_TIMEOUT_RECEIVE] = 60;

    }

    public void setMerchant(String terminalID, String merchantID, String name ){
        this.terminalID = terminalID;
        this.merchantID = merchantID;
        this.merchantName = name;
    }
    public void setHost(String addr, int port ){
        this.hostAddr = addr;
        this.hostPort = port;
    }
    public void setKeysIndex( int masterKeyIndex, int pinKeyIndex, int dataKeyIndex, int macKeyIndex, int dukptIndex ){
        this.masterKeyIndex = masterKeyIndex;
        this.pinKeyIndex = pinKeyIndex;
        this.dataKeyIndex = dataKeyIndex;
        this.macKeyIndex = macKeyIndex;
        this.dukptIndex = dukptIndex;
    }

    public void setDateTimePattern(String datePattern, String timePattern ){
        this.dateFormat = new SimpleDateFormat(datePattern);
        this.timeFormat = new SimpleDateFormat(timePattern);
    }
    public void AIDAppend( String aid ){
        if( this.AIDList == null  ){
            this.AIDList = new ArrayList<String>();
        }
        this.AIDList.add( aid );
    }
    public void CardBINAppend( String cardBIN ){
        if( this.cardBinList == null  ){
            this.cardBinList = new ArrayList<String>();
        }
        this.cardBinList.add( cardBIN );
    }

    boolean checkValid(String cardBin, String aid){
        if( null == cardBinList && null == AIDList ){
            Log.d(TAG, "there is no cardBind & AID be set, it's valid for all cards." + this.description);
            return true;
        }
        if( null != aid && null != AIDList ){
            for( String a : AIDList ){
                int len = a.length();
                if( aid.length() >= len ){
                    if( aid.substring(0, len).compareTo(a) == 0 ) {
                        Log.d(TAG, "AID hit to " + this.description);
                        return true;
                    }
                }
            }
        }
        if( null != cardBin && null != cardBinList ){
            for( String card : cardBinList ){
                int len = card.length();
                if( len <= 0 ){
                    continue;
                }
                if( cardBin.length() >= len ){
                    if( cardBin.substring(0, len).compareTo(card) == 0 ) {
                        Log.d(TAG, "cardbin=" + cardBin +
                                " hit to " + this.description + ", value" + card );
                        return true;
                    }
                }
            }

        }
        return false;
    }

    public T new8583(){
        try {
//            iso8583 = iso8583Class.newInstance();
            T t = (T) iso8583Class.newInstance();
            return t;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        // return iso8583;
        return null;
    }

    public String getHostIp() {
        return hostAddr;
    }

//    public String getPort() {
//        return Integer.toString(hostPort);
//    }

    public int getPort() {
        return hostPort;
    }

    public String getUrlPath(){
        return "";
    }

    public int getTimeout( int type ){
        switch (type){
            case TYPE_TIMEOUT_CONNECT:
                return timeout[TYPE_TIMEOUT_CONNECT];
            case TYPE_TIMEOUT_RECEIVE:
                return timeout[TYPE_TIMEOUT_RECEIVE];
        }
        return -1;
    }
}
