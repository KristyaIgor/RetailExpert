package md.intelectsoft.quickpos.verifone.usecase;

import android.util.Log;

import md.intelectsoft.quickpos.verifone.basic.HostInformation;
import md.intelectsoft.quickpos.verifone.basic.MultiHosts;
import md.intelectsoft.quickpos.verifone.caseA.ISO8583u;
import md.intelectsoft.quickpos.verifone.caseA.ISO8583u128;
import md.intelectsoft.quickpos.verifone.caseB.ISO8583CaseB;

/**
 * Created by Simon on 2019/1/25.
 */

public class MultiHostsConfig {
    private static String TAG = "MultiHostsConfig";

    public static MultiHosts multiHosts = null;

    public enum Category {
        Default,
        VISA,
        Bitmap128,
    }

    public static void initialize() {

        multiHosts = new MultiHosts();

        int index = -1;

        HostInformation hostInformation;

        // Default
        hostInformation = new HostInformation<>(new ISO8583u(), Category.Default, "Default");

        hostInformation.setMerchant("01020304", "ABCDE0123456789", "X990 EMV Demo");
        hostInformation.setHost("127.0.0.1", 5556);
        hostInformation.AIDList = null;
        hostInformation.cardBinList = null; // both the AIDList & cardBinList, so all cards can be match for this host. this should be the last ONE in the list.
        hostInformation.setKeysIndex(10, 10, 10, 10, 1);

        hostInformation.prn_card_mask = "*";
        hostInformation.prn_card_mask_range = new int[]{6, -5};

        index = multiHosts.append(hostInformation);
        Log.d(TAG, "new host " + hostInformation.description +
                "setting @:" + index);

        // VISA
        hostInformation = new HostInformation<>(new ISO8583CaseB(), Category.VISA, "VISA");
        hostInformation.setMerchant("03040102", "0123456789ABCDE", "X990 EMV Demo");
        hostInformation.setHost("127.0.0.1", 5558);
        hostInformation.AIDAppend("A000000003");
        hostInformation.CardBINAppend("439225");
        hostInformation.CardBINAppend("11000241");
        hostInformation.setKeysIndex(20, 20, 20, 20, 2);

        hostInformation.prn_card_mask = "#";
        hostInformation.prn_card_mask_range = new int[]{2, -5};


        index = multiHosts.append(hostInformation);
        Log.d(TAG, "new host " + hostInformation.description +
                "setting @:" + index);

        // 128 bitmap
        hostInformation = new HostInformation<>(new ISO8583u128(), Category.Bitmap128, "128 Bitmap");
        hostInformation.setMerchant("02030401", "CDE0123456789AB", "X990 EMV Demo");
        hostInformation.setHost("127.0.0.1", 5560);
        hostInformation.AIDAppend("A000000668");
        hostInformation.CardBINAppend("233605");
        hostInformation.setKeysIndex(20, 20, 20, 20, 2);

        hostInformation.prn_card_mask = "X";
        hostInformation.prn_card_mask_range = new int[]{4, -5};

        index = multiHosts.append(hostInformation);
        Log.d(TAG, "new host " + hostInformation.description + "setting @:" + index);

    }

    public static HostInformation get(int index) {
        return multiHosts.get(index);
    }

    public static void update(int index, HostInformation hostInformation) {
        multiHosts.update(index, hostInformation);
    }

    public static HostInformation getHost(int index, String cardBin, String aid) {
        return multiHosts.getHost(index, cardBin, aid);
    }
}
