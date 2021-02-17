package md.intelectsoft.quickpos.verifone.Utilities;

import android.util.Log;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Simon on 2018/8/31.
 * Utility for BCD ASCII convert and others
 */
public class Utility {


    private static final String TAG = "Utility";

    public static String byte2HexStr(byte[] var0, int offset, int length ) {
        if (var0 == null) {
            return "";
        } else {
            String var1 ;
            StringBuilder var2 = new StringBuilder("");

            for (int var3 = offset; var3 < (offset+length); ++var3) {
                var1 = Integer.toHexString(var0[var3] & 255);
                var2.append(var1.length() == 1 ? "0" + var1 : var1);
            }

            return var2.toString().toUpperCase().trim();
        }
    }
    public static String byte2HexStr(byte[] var0) {
        if (var0 == null) {
            return "";
        } else {
            String var1 ;
            StringBuilder var2 = new StringBuilder("");

            for (byte b : var0) {
                var1 = Integer.toHexString(b & 255);
                var2.append(var1.length() == 1 ? "0" + var1 : var1);
            }

            return var2.toString().toUpperCase().trim();
        }
    }


    public static byte[] hexStr2Byte(String hexString) {
//        Log.d(TAG, "hexStr2Byte:" + hexString);
        if (hexString == null || hexString.length() == 0 ) {
            return new byte[] {0};
        }
        String hexStrTrimed = hexString.replace(" ", "");
//        Log.d(TAG, "hexStr2Byte:" + hexStrTrimed);
        {
            String hexStr = hexStrTrimed;
            int len = hexStrTrimed.length();
            if( (len % 2 ) == 1 ){
                hexStr = hexStrTrimed + "0";
                ++len;
            }
            char highChar, lowChar;
            int  high, low;
            byte result [] = new byte[len/2];
            String s;
            for( int i=0; i< hexStr.length(); i++ ) {
                // read 2 chars to convert to byte
//                s = hexStr.substring(i,i+2);
//                int v = Integer.parseInt(s, 16);
//
//                result[i/2] = (byte) v;
//                i++;
                // read high byte and low byte to convert
                highChar = hexStr.charAt(i);
                lowChar = hexStr.charAt(i+1);
                high = CHAR2INT(highChar);
                low = CHAR2INT(lowChar);
                result[i/2] = (byte) (high*16+low);
                i++;
            }
            return  result;

        }
    }
    public static byte[] hexStr2Byte(String hexString, int beginIndex, int length ) {
        if (hexString == null || hexString.length() == 0 ) {
            return new byte[] {0};
        }
        {
            if( length > hexString.length() ){
                length = hexString.length();
            }
            String hexStr = hexString;
            int len = length;
            if( (len % 2 ) == 1 ){
                hexStr = hexString + "0";
                ++len;
            }
            byte result [] = new byte[len/2];
            String s;
            for( int i=beginIndex; i< len; i++ ) {
                s = hexStr.substring(i,i+2);
                int v = Integer.parseInt(s, 16);

                result[i/2] = (byte) v;
                i++;
            }
            return  result;

        }
    }

    public static byte HEX2DEC( int hex ){
        return (byte)((hex/10)*16+hex%10);
    }

    public static int DEC2INT( byte dec ){
        int high = ((0x007F & dec) >> 4);
        if( 0!= (0x0080&dec) ) {
            high += 8;
        }
        return (high)*10+(dec&0x0F) ;
    }
    public static int CHAR2INT(char c) {
        if(
                (c >= '0' && c <= '9' )
            || (c == '=' )  // for track2
                ) {
            return c - '0';
        } else if(c >= 'a' && c <= 'f'){
            return c - 'a' +10;
        } else if(c >= 'A' && c <= 'F'){
            return c - 'A' +10;
        } else {
            return 0;
        }
    }
    public static String getReadableAmount(String amount) {
        if (amount != null && !amount.isEmpty()) {
            for(int i = 0; i < amount.length(); ++i) {
                char c = amount.charAt(i);
                if (c < '0' || c > '9') {
                    return "0.00";
                }
            }

            DecimalFormat df = new DecimalFormat("0.00");
            return df.format(Double.parseDouble(amount) / 100.0D);
        } else {
            return "0.00";
        }
    }

    public static String fixCardNoWithMask(String card){
        Log.d(TAG, "try fixCardNoWithMask:" +card );
        if( null == card ){

            return null;
        }
        String fix = card;

        int i;

        int start = 6;
        if( start < 0 ){
            start = card.length()+start;
        }
        int end = -4;
        if( end < 0 ){
            end = card.length() + end ;
        }
        Log.d(TAG, "replace mask "+"*"+" from " + start + " to " + end);
        if( start > end ){
            return card;
        }
        fix = card.substring(0, start);

        for ( i= start; i < end; ) {
            fix += ("*" );
            i += 1;
        }
        fix += (card.substring(end));


        Log.d(TAG, "try insert space to card No. " + fix);
        // add space after each 4 chars
        String fix2 = "";
        for( i=0; i < fix.length(); ){
            int end2 = i + 4;
            if( end2 > fix.length() ){
                end2 = fix.length();
            }
            fix2 += (fix.substring(i, end2));
            if( end2 < fix.length() ){
                fix2 += (" ");
            }
            i = end2;
        }
        return  fix2;
    }

    public static String getSystemDatetime() {
        try {
            Calendar c = Calendar.getInstance();
            int y = c.get(1);
            int m = c.get(2) + 1;
            int d = c.get(5);
            int h = c.get(11);
            int mn = c.get(12);
            int s = c.get(13);
            return String.format("%04d%02d%02d%02d%02d%02d", y, m, d, h, mn, s);
        } catch (Exception var7) {
            var7.printStackTrace();
            return null;
        }
    }

    public static String getFormattedDateTime(String dataTime, String oldFormat, String newFormat) {
        try {
            return (new SimpleDateFormat(newFormat)).format((new SimpleDateFormat(oldFormat)).parse(dataTime));
        } catch (ParseException var4) {
            var4.printStackTrace();
            return null;
        }
    }

    public static byte[] asc2Bcd(String asc, int length) {
        if (asc != null && asc.length() > 0 && length > 0 && length <= asc.length()) {
            try {
                int len = length;
                int mod = length % 2;
                if (mod != 0) {
                    asc = "0" + asc;
                    len = length + 1;
                }

                byte[] abt = new byte[len];
                if (len >= 2) {
                    len /= 2;
                }

                byte[] bbt = new byte[len];
                abt = asc.getBytes();

                for(int p = 0; p < len; ++p) {
                    int j;
                    if (abt[2 * p] >= 48 && abt[2 * p] <= 57) {
                        j = abt[2 * p] - 48;
                    } else if (abt[2 * p] >= 97 && abt[2 * p] <= 122) {
                        j = abt[2 * p] - 97 + 10;
                    } else {
                        j = abt[2 * p] - 65 + 10;
                    }

                    int k;
                    if (abt[2 * p + 1] >= 48 && abt[2 * p + 1] <= 57) {
                        k = abt[2 * p + 1] - 48;
                    } else if (abt[2 * p + 1] >= 97 && abt[2 * p + 1] <= 122) {
                        k = abt[2 * p + 1] - 97 + 10;
                    } else {
                        k = abt[2 * p + 1] - 65 + 10;
                    }

                    int a = (j << 4) + k;
                    byte b = (byte)a;
                    bbt[p] = b;
                }

                return bbt;
            } catch (Exception var11) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static String bcd2Asc(byte[] bcd, int length) {
        if (bcd != null && bcd.length > 0 && length > 0 && length <= bcd.length) {
            try {
                String stmp = "";
                StringBuilder sb = new StringBuilder("");

                for(int i = 0; i < length; ++i) {
                    stmp = Integer.toHexString(bcd[i] & 255);
                    sb.append(stmp.length() == 1 ? "0" + stmp : stmp);
                }

                return sb.toString().toUpperCase().trim();
            } catch (Exception var5) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static String bcd2Asc(byte[] bcd) {
        if (bcd != null && bcd.length > 0) {
            try {
                String stmp = "";
                StringBuilder sb = new StringBuilder("");

                for(int i = 0; i < bcd.length; ++i) {
                    stmp = Integer.toHexString(bcd[i] & 255);
                    sb.append(stmp.length() == 1 ? "0" + stmp : stmp);
                }

                return sb.toString().toUpperCase().trim();
            } catch (Exception var4) {
                return null;
            }
        } else {
            return null;
        }
    }


}
