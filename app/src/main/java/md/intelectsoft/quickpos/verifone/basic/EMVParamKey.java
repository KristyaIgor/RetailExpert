package md.intelectsoft.quickpos.verifone.basic;

/**
 * Created by Simon on 2018/8/31.
 */

public class EMVParamKey extends EMVTLVParam  {


    /**
     * the RID Tag list
     *
     * */
    public static int TAG_RID_9F06 = 0x9F06;    //Application Identifier (AID) - card
    public static int TAG_Index_9F22 = 0x9F22;  //Certification Authority Public Key Index
    public static int TAG_ExpiryDate_DF05 = 0xDF05;      //	Dedicated File (DF)
//    public static int TAG_ = 0xDF06;
    public static int TAG_Algorithm_DF07 = 0xDF07;
    public static int TAG_KEY_DF02 = 0xDF02;
    public static int TAG_Exponent_DF04 = 0xDF04;
    public static int TAG_Hash_DF03 = 0xDF03;

    /**
     * the RID DKV Tag list
     *
     * */
    public static int DKV_TAG_AID_4F0B = 0x4F0B;   //Application Identifier (AID) - card in DKV
    public static int DKV_TAG_Index_8F = 0x8F01;  //Certification Authority Public Key Index in DKV
    public static int DKV_TAG_ExpiryDate_840E = 0x840E; // 	Dedicated File (DF) in DKV
    //    public static int TAG_ = 0xDF06;
    public static int DKV_TAG_Algorithm_DF07 = 0xDF07;
    public static int DKV_TAG_KEY_DF02 = 0xDF02;
    public static int DKV_TAG_Exponent_DF04 = 0xDF04;
    public static int DKV_TAG_Hash_DF03 = 0xDF03;


    public EMVParamKey(){
        super();
        /*
         * default value of some tags
         * value null means the tag is optional
         * */
        defaultTagValue = new DefaultTagValue[]{
            new DefaultTagValue(0xDF06, "01"),
            new DefaultTagValue(TAG_Algorithm_DF07, "01"),
            new DefaultTagValue(TAG_Exponent_DF04, "03"),
        };
    }

    @Override
    public void clean(){
        super.clean();
        if( defaultTagValue != null ) {
            for( DefaultTagValue tagValue: defaultTagValue ){
                tagValue.available = true;
            }
        }
    }


}
