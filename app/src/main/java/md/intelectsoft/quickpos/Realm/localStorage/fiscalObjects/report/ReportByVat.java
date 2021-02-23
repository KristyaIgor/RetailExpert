package md.intelectsoft.quickpos.Realm.localStorage.fiscalObjects.report;

import io.realm.RealmObject;

public class ReportByVat extends RealmObject {
    private int vatCode;
    private int vatValue;
    private double sumVatCode;   //Totalul sumei TVA care nu se impozitează (CIRCUL)
    private double sumVatTax;    //Totalul sumei cotei de taxă pe valoare adăugată care  se impozitează (IMPOZIT)

    public int getVatCode() {
        return vatCode;
    }

    public void setVatCode(int vatCode) {
        this.vatCode = vatCode;
    }

    public int getVatValue() {
        return vatValue;
    }

    public void setVatValue(int vatValue) {
        this.vatValue = vatValue;
    }

    public double getSumVatCode() {
        return sumVatCode;
    }

    public void setSumVatCode(double sumVatCode) {
        this.sumVatCode = sumVatCode;
    }

    public double getSumVatTax() {
        return sumVatTax;
    }

    public void setSumVatTax(double sumVatTax) {
        this.sumVatTax = sumVatTax;
    }
}
