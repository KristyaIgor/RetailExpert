package md.intelectsoft.quickpos.verifone.transaction.canvas_printer;

/**
 * Created by Simon on 2019/2/27.
 */

/**
 * Class {@code PrinterDefine } defined the style for printing
 */
public class PrinterDefine {
    // value less than 0x100 means the font size
    public static int PStyle_align_left = 0x100;
    public static int PStyle_align_center = 0x200;
    public static int PStyle_align_right = 0x400;
    public static int PStyle_align_remove = 0xFFFFF8FF;
    public static int PStyle_image_contrast_light = 0x1000;
    public static int PStyle_image_contrast_normal = 0x2000;
    public static int PStyle_image_contrast_heavy = 0x4000;
    public static int PStyle_image_revert = 0x8000;
    public static int PStyle_not_print = 0x10000000;    // not print to paper
    public static int PStyle_not_show  = 0x20000000;    // not show on canvas

    public static final String Font_default = "/system/fonts/DroidSansMono.ttf"; // not set will use the system default
    public static final String Font_Bold = "/system/fonts/DroidSans-Bold.ttf";

}
