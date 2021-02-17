package md.intelectsoft.quickpos.verifone.caseB;

import md.intelectsoft.quickpos.verifone.basic.ISO8583;

/**
 * Created by Simon on 2018/8/27.
 */

public class ISO8583CaseB extends ISO8583 {

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
            {
                    {	TYPE_BCD	,	4, 0, 0	},	// field	0
                    {	TYPE_BIN	,	8, 0, 0	},	// field	1
                    {	TYPE_L_BCD	,	19, 0, 0	},	// field	2
                    {	TYPE_BCD	,	6, 0, 0	},	// field	3
                    {	TYPE_BCD + TYPE_FILL_ZERO_LEFT	,	12, 0, 0	},	// field	4
                    {	TYPE_BCD	,	12, 0, 0	},	// field	5

                    {	TYPE_BCD	,	10, 0, 0	},	// field	6
                    {	TYPE_BCD	,	8, 0, 0	},	// field	7
                    {	TYPE_BCD	,	8, 0, 0	},	// field	8
                    {	TYPE_BCD	,	8, 0, 0	},	// field	9
                    {	TYPE_BCD	,	8, 0, 0	},	// field	10

                    {	TYPE_BCD	,	6, 0, 0	},	// field	11
                    {	TYPE_BCD	,	6, 0, 0	},	// field	12
                    {	TYPE_BCD	,	4, 0, 0	},	// field	13
                    {	TYPE_BCD	,	4, 0, 0	},	// field	14
                    {	TYPE_BCD	,	4, 0, 0	},	// field	15

                    {	TYPE_BCD	,	4, 0, 0	},	// field	16
                    {	TYPE_BCD	,	4, 0, 0	},	// field	17
                    {	TYPE_BCD	,	4, 0, 0	},	// field	18
                    {	TYPE_BCD	,	3, 0, 0	},	// field	19
                    {	TYPE_BCD	,	3, 0, 0	},	// field	20

                    {	TYPE_BCD	,	2, 0, 0	},	// field	21
                    {	TYPE_BCD	,	1, 0, 0	},	// field	22
                    {	TYPE_BCD	,	8, 0, 0	},	// field	23
                    {	TYPE_BCD	,	6, 0, 0	},	// field	24
                    {	TYPE_BCD	,	8, 0, 0	},	// field	25

                    {	TYPE_L_BCD	,	11, 0, 0	},	// field	26
                    {	TYPE_L_BCD	,	11, 0, 0	},	// field	27
                    {	TYPE_L_BCD	,	28, 0, 0	},	// field	28
                    {	TYPE_L_BCD	,	37, 0, 0	},	// field	29
                    {	TYPE_L_BCD	,	104, 0, 0	},	// field	30

                    {	TYPE_BCD	,	8, 0, 0	},	// field	31
                    {	TYPE_L_BCD	,	11, 0, 0	},	// field	32
                    {	TYPE_L_BCD	,	11, 0, 0	},	// field	33
                    {	TYPE_L_BCD	,	28, 0, 0	},	// field	34
                    {	TYPE_L_BCD	,	37, 0, 0	},	// field	35

                    {	TYPE_LL_BCD	,	104, 0, 0	},	// field	36
                    {	TYPE_ASC	,	12, 0, 0	},	// field	37
                    {	TYPE_ASC	,	6, 0, 0	},	// field	38
                    {	TYPE_ASC	,	2, 0, 0	},	// field	39
                    {	TYPE_ASC	,	3, 0, 0	},	// field	40

                    {	TYPE_ASC	,	8, 0, 0	},	// field	41
                    {	TYPE_ASC	,	15, 0, 0	},	// field	42
                    {	TYPE_ASC	,	40, 0, 0	},	// field	43
                    {	TYPE_ASC	,	25, 0, 0	},	// field	44
                    {	TYPE_L_ASC	,	75, 0, 0	},	// field	45

                    {	TYPE_LL_ASC	,	999, 0, 0	},	// field	46
                    {	TYPE_LL_ASC	,	999, 0, 0	},	// field	47
                    {	TYPE_L_BCD	,	999, 0, 0	},	// field	48
                    {	TYPE_ASC	,	3, 0, 0	},	// field	49
                    {	TYPE_ASC	,	3, 0, 0	},	// field	50

                    {	TYPE_ASC	,	3, 0, 0	},	// field	51
                    {	TYPE_BIN	,	8, 0, 0	},	// field	52
                    {	TYPE_BCD	,	16, 0, 0	},	// field	53
                    {	TYPE_L_ASC	,	120, 0, 0	},	// field	54
                    {	TYPE_L_ASC	,	999, 0, 0	},	// field	55

                    {	TYPE_LL_ASC	,	999, 0, 0	},	// field	56
                    {	TYPE_LL_ASC	,	999, 0, 0	},	// field	57
                    {	TYPE_LL_ASC	,	999, 0, 0	},	// field	58
                    {	TYPE_LL_ASC	,	999, 0, 0	},	// field	59
                    {	TYPE_LL_ASC	,	999, 0, 0	},	// field	60

                    {	TYPE_L_ASC	,	999, 0, 0	},	// field	61
                    {	TYPE_L_ASC	,	999, 0, 0	},	// field	62
                    {	TYPE_L_ASC	,	999, 0, 0	},	// field	63
                    {	TYPE_BIN	,	8, 0, 0	},	// field	64
            };
    public ISO8583CaseB(){
        super.attribute_array = this.FIELD_ATTRIBUTE_ARRAY;
    }

    @Override
    protected byte[] calculateMac(byte[] packet, int offset, int length) {
        return null;
    }

    @Override
    public int getFieldIndex( ATTRIBUTE attribute ) {
        int field = -1;
        switch (attribute) {
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
    protected int getHeaderLen() {
        return 5;
    }


}
