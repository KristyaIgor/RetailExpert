package md.intelectsoft.quickpos.fisc.enums;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class BillServiceEnum {
    public static final int Sales = 0, Refilling = 1, CashOut = 2;

    @IntDef({Sales, Refilling, CashOut})
    @Retention(RetentionPolicy.SOURCE)
    public @interface productType {
    }
}
