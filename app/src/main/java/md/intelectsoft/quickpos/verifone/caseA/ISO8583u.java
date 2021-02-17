package md.intelectsoft.quickpos.verifone.caseA;


import java.util.Arrays;

import md.intelectsoft.quickpos.verifone.basic.ISO8583;

/**
 * Created by Simon on 2018/8/27.
 */

public class ISO8583u extends ISO8583 {

    public static int F_MessageType_00 = 0;
    public static int F_AccountNumber_02 = 2;
    public static int F_AmountOfTransactions_04 = 4;
    public static int F_DateOfExpired_14 = 14;
    public static int F_CardSN_23 = 23;
    public static int F_Track_2_Data_35 = 35;
    public static int F_Track_3_Data_36 = 36;
    public static int F_AuthorizationIdentificationResponseCode_38 = 38;
    public static int F_ResponseCode_39 = 39;
    public static int F_TerminalID_41 = 41;
    public static int F_MerchentID_42 = 42;
    public static int F_CurrencyCode_49 = 49;
    public static int F_PINData_52 = 52;
    public static int F_BalancAmount_54 = 54;
    public static int F_55 = 55;

    static int [][] FIELD_ATTRIBUTE_ARRAY =
                    {// type, length, not defined, not defined
                    {	TYPE_BCD	,	4, 0, 0	},	// field	0, (Message Type Identifier)
                    {	TYPE_BIN	,	8, 0, 0	},	// field	1, bitmap
                    {	TYPE_L_BCD	,	19, 0, 0	},	// field	2, (Primary Account Number), N..19(LLVAR)
                    {	TYPE_BCD	,	6, 0, 0	},	// field	3, (Transaction Processing Code), N6
                    {	TYPE_BCD + TYPE_FILL_ZERO_LEFT	,	12, 0, 0	},	// field	4, (Amount Of Transactions), N12
                    {	0	,	0, 0, 0	},	// field	5
                    {	0	,	0, 0, 0	},	// field	6
                    {	0	,	0, 0, 0	},	// field	7
                    {	0	,	0, 0, 0	},	// field	8
                    {	0	,	0, 0, 0	},	// field	9
                    {	0	,	0, 0, 0	},	// field	10
                    {	TYPE_BCD	,	6, 0, 0	},	// field	11, (System Trace Audit Number), N6
                    {	TYPE_BCD	,	6, 0, 0	},	// field	12, (Local Time Of Transaction), hhmmss, N6
                    {	TYPE_BCD	,	4, 0, 0	},	// field	13, (Local Date Of Transaction), MMDD, N4
                    {	TYPE_BCD	,	4, 0, 0	},	// field	14, (Date Of Expired), YYMM, N4
                    {	TYPE_BCD	,	4, 0, 0	},	// field	15, (Date Of Settlement), N4
                    {	0	,	0, 0, 0	},	// field	16
                    {	0	,	0, 0, 0	},	// field	17
                    {	0	,	0, 0, 0	},	// field	18
                    {	0	,	0, 0, 0	},	// field	19
                    {	0	,	0, 0, 0	},	// field	20
                    {	0	,	0, 0, 0	},	// field	21
                    {	TYPE_BCD	,	3, 0, 0	},	// field	22, (Point Of Service Entry Mode), N3
                    {	TYPE_BCD	,	3, 0, 0	},	// field	23, (Card Sequence Number), N3
                    {	TYPE_ASC	,	2, 0, 0	},	// field	24
                    {	TYPE_BCD	,	2, 0, 0	},	// field	25, (Point Of Service Condition Mode), N2
                    {	TYPE_BCD	,	2, 0, 0	},	// field	26, (Point Of Service PIN Capture Code), N2
                    {	TYPE_BCD	,	2, 0, 0	},	// field	27
                    {	TYPE_BCD	,	2, 0, 0	},	// field	28
                    {	TYPE_ASC	,	8, 0, 0	},	// field	29
                    {	TYPE_BCD	,	8, 0, 0	},	// field	30
                    {	TYPE_BCD	,	8, 0, 0	},	// field	31
                    {	TYPE_L_BCD	,	11, 0, 0	},	// field	32, (Acquiring Institution Identification Code), N..11(LLVAR)
                    {	TYPE_L_BCD	,	11, 0, 0	},	// field	33
                    {	TYPE_L_BCD	,	28, 0, 0	},	// field	34
                    {	TYPE_L_BCD	,	37, 0, 0	},	// field	35, (Track 2 Data), Z..37(LLVAR)
                    {	TYPE_LL_BCD	,	104, 0, 0	},	// field	36, (Track 3 Data), Z...104(LLLVAR)
                    {	TYPE_ASC_FS	,	12, 0, 0	},	// field	37, (Retrieval Reference Number), AN12
                    {	TYPE_ASC_FS	,	6, 0, 0	},	// field	38, (Authorization Identification Response Code), AN6
                    {	TYPE_ASC_FS	,	2, 0, 0	},	// field	39, (Response Code),
                    {	TYPE_ASC	,	3, 0, 0	},	// field	40
                    {	TYPE_ASC_FS	,	8, 0, 0	},	// field	41, (Card Acceptor Terminal Identification), ANS8
                    {	TYPE_ASC	,	15, 0, 0	},	// field	42, (Card Acceptor Identification Code), ANS15
                    {	TYPE_ASC	,	40, 0, 0	},	// field	43
                    {	TYPE_L_ASC	,	25, 0, 0	},	// field	44, (Additional Response Data), AN..25
                    {	TYPE_L_ASC	,	76, 0, 0	},	// field	45
                    {	TYPE_LL_ASC	,	999, 0, 0	},	// field	46
                    {	TYPE_LL_ASC	,	999, 0, 0	},	// field	47,
                    {	TYPE_LL_BCD	,	322, 0, 0	},	// field	48, (Additional Data - Private), N...322(LLLVAR)
                    {	TYPE_ASC	,	3, 0, 0	},	// field	49, (Currency Code Of Transaction), AN3
                    {	TYPE_ASC	,	3, 0, 0	},	// field	50
                    {	TYPE_ASC	,	3, 0, 0	},	// field	51
                    {	TYPE_BIN	,	8, 0, 0	},	// field	52, (PIN Data), B64
                    {	TYPE_BCD	,	16, 0, 0	},	// field	53, (Security Related Control Information ), n16
                    {	TYPE_LL_ASC	,	20, 0, 0	},	// field	54, (Balanc Amount), AN...020(LLLVAR)
                    {	TYPE_LL_BIN	,	255, 0, 0	},	// field	55, (Intergrated Circuit Card System Related Data)
                    {	TYPE_LL_ASC	,	999, 0, 0	},	// field	56
                    {	TYPE_LL_ASC	,	999, 0, 0	},	// field	57
                    {	TYPE_LL_ASC	,	100, 0, 0	},	// field	58, （PBOC_ELECTRONIC_DATA）, ans...100(LLLVAR)
                    {	TYPE_LL_ASC	,	999, 0, 0	},	// field	59, (Reserved Private)
                    {	TYPE_LL_BCD	,	17, 0, 0	},	// field	60, (Reserved Private), N...17(LLLVAR)
                    {	TYPE_LL_BCD	,	29, 0, 0	},	// field	61, (Original Message), N...029(LLLVAR)
                    {	TYPE_LL_ASC	,	512, 0, 0	},	// field	62, (Reserved Private), ANS...512(LLLVAR)
                    {	TYPE_LL_ASC	,	163, 0, 0	},	// field	63, (Reserved Private), ANS...163(LLLVAR)
                    {	TYPE_BIN	,	8, 0, 0	},	// field	64, (Message Authentication Code), B64
            };

    public ISO8583u(){
        super.attribute_array = this.FIELD_ATTRIBUTE_ARRAY;
        super.header = "6000800000"+"603100010202";
        super.tail = "";
    }

    @Override
    protected byte[] calculateMac( byte[] packet, int offset, int length ) {
        int start = offset + 0;

        int len = length-start;

        int i, j;
        int cnt = (len % 8 != 0) ? (len / 8 + 1) : len / 8;
        byte[] mac = new byte[8];
        Arrays.fill(mac, (byte) 0);

        cnt += start;
        for (i = start; i < cnt; i++) {
            for (j = 0; j < 8; j++) {
                mac[j] ^= packet[i * 8 + j];
            }
        }

        return mac;
    }
    @Override
    protected int getHeaderLen() {
        return 11;
    }


    @Override
    public int getFieldIndex( ATTRIBUTE attribute ) {
        int field = -1;
        switch (attribute){
            case MessageType:
                field = F_MessageType_00;
                break;
            case Amount:
                field = F_AmountOfTransactions_04;
                        break;
            case CardSN:
                field = F_CardSN_23;
                break;
            case Track2:
                field = F_Track_2_Data_35;
                break;
            case Track3:
                field = F_Track_3_Data_36;
                break;
            case PinBlock:
                field = F_PINData_52;
            case ServiceCode:

                break;
            case CurrencyCode:
                field = F_CurrencyCode_49;
                break;
            case DateOfExpired:
                field = F_DateOfExpired_14;
                break;
            case MerchantID:
                field = F_MerchentID_42;
                break;
            case TerminalID:
                field = F_TerminalID_41;
                break;
            case Time:
                field = 12;
                break;
            case Date:
                field = 13;
                break;
            case Balance:
                field = F_BalancAmount_54;
                break;
        }
        return field;
    }

    @Override
    protected int getISOBITMAX() {
        return (FIELD_ATTRIBUTE_ARRAY[1][1]<<3);
    }



}


