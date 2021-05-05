package md.intelectsoft.quickpos.verifone.Utilities;
/*
 *  author: Derrick
 *  Time: 2019/5/27 14:26
 */

import android.os.RemoteException;

import com.vfi.smartpos.deviceservice.aidl.IBeeper;
import com.vfi.smartpos.deviceservice.aidl.IDeviceInfo;
import com.vfi.smartpos.deviceservice.aidl.IDeviceService;
import com.vfi.smartpos.deviceservice.aidl.IEMV;
import com.vfi.smartpos.deviceservice.aidl.IInsertCardReader;
import com.vfi.smartpos.deviceservice.aidl.ILed;
import com.vfi.smartpos.deviceservice.aidl.IMagCardReader;
import com.vfi.smartpos.deviceservice.aidl.IPinpad;
import com.vfi.smartpos.deviceservice.aidl.IPrinter;
import com.vfi.smartpos.deviceservice.aidl.IRFCardReader;
import com.vfi.smartpos.deviceservice.aidl.IScanner;
import com.vfi.smartpos.deviceservice.aidl.ISerialPort;
import com.vfi.smartpos.deviceservice.aidl.key_manager.IDukpt;

import md.intelectsoft.quickpos.POSApplication;

;

public class DeviceHelper {

    private static DeviceHelper instance;
    private IPinpad pinPad;
    private IEMV iemv;
    private IBeeper beeper;
    private ILed led;
    private IPrinter printer;
    private IDeviceInfo deviceInfo;
    private ISerialPort serialPort;
    private IScanner scanner;
    private IMagCardReader magCardReader;
    private IInsertCardReader insertCardReader;
    private IRFCardReader rfCardReader;
    private IDeviceService deviceService;
    private IDukpt iDukpt;

    private DeviceHelper(){
    }

    public static synchronized DeviceHelper getInstance() {
        if ( null == instance){
            instance = new DeviceHelper();

        }
        return instance;
    }

    public void initDeviceHelper(POSApplication application){
        this.deviceService = application.getDeviceService();
        try {
            this.iemv = deviceService.getEMV();
            this.pinPad = deviceService.getPinpad(5);
            this.beeper = deviceService.getBeeper();
            this.led = deviceService.getLed();
            this.printer = deviceService.getPrinter();
            this.deviceInfo = deviceService.getDeviceInfo();
            this.scanner = deviceService.getScanner(0);
            this.iDukpt = deviceService.getDUKPT();
            this.serialPort = deviceService.getSerialPort("usb-rs232");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public IPinpad getPinPad() {
        return pinPad;
    }

    public void setPinPad(IPinpad pinPad) {
        this.pinPad = pinPad;
    }

    public IEMV getIemv() {
        return iemv;
    }

    public void setIemv(IEMV iemv) {
        this.iemv = iemv;
    }

    public IBeeper getBeeper() {
        return beeper;
    }

    public void setBeeper(IBeeper beeper) {
        this.beeper = beeper;
    }

    public ILed getLed() {
        return led;
    }

    public void setLed(ILed led) {
        this.led = led;
    }

    public IPrinter getPrinter() {
        return printer;
    }

    public void setPrinter(IPrinter printer) {
        this.printer = printer;
    }

    public IDeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(IDeviceInfo deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public ISerialPort getSerialPort() {
        return serialPort;
    }

    public void setSerialPort(ISerialPort serialPort) {
        this.serialPort = serialPort;
    }

    public IScanner getScanner() {
        return scanner;
    }

    public void setScanner(IScanner scanner) {
        this.scanner = scanner;
    }

    public IMagCardReader getMagCardReader() {
        return magCardReader;
    }

    public void setMagCardReader(IMagCardReader magCardReader) {
        this.magCardReader = magCardReader;
    }

    public IInsertCardReader getInsertCardReader() {
        return insertCardReader;
    }

    public void setInsertCardReader(IInsertCardReader insertCardReader) {
        this.insertCardReader = insertCardReader;
    }

    public IRFCardReader getRfCardReader() {
        return rfCardReader;
    }

    public void setRfCardReader(IRFCardReader rfCardReader) {
        this.rfCardReader = rfCardReader;
    }

    public IDeviceService getDeviceService() {
        return deviceService;
    }

    public void setDeviceService(IDeviceService deviceService) {
        this.deviceService = deviceService;
    }

    public void setiDukpt(IDukpt iDukpt) {
        this.iDukpt = iDukpt;
    }

    public IDukpt getiDukpt() {
        return iDukpt;
    }
}
