package com.gs.imsdk.data;

/**
 * 接受来自客户端的信息
 */
public class DeviceInfo {
    private String id;  // 唯一标识
    private String androidID;
    private String fingerPrint;
    private String simSerial;   // sim 的序列号
    private String IMEI;
    private String serialNumber;
    private String phoneProducer;
    private String phoneModel;
    private String systemVersion;
    private String sdkVersion;
    private String mac;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAndroidID() {
        return androidID;
    }

    public void setAndroidID(String androidID) {
        this.androidID = androidID;
    }

    public String getFingerPrint() {
        return fingerPrint;
    }

    public void setFingerPrint(String fingerPrint) {
        this.fingerPrint = fingerPrint;
    }

    public String getSimSerial() {
        return simSerial;
    }

    public void setSimSerial(String simSerial) {
        this.simSerial = simSerial;
    }

    public String getIMEI() {
        return IMEI;
    }

    public void setIMEI(String IMEI) {
        this.IMEI = IMEI;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getPhoneProducer() {
        return phoneProducer;
    }

    public void setPhoneProducer(String phoneProducer) {
        this.phoneProducer = phoneProducer;
    }

    public String getPhoneModel() {
        return phoneModel;
    }

    public void setPhoneModel(String phoneModel) {
        this.phoneModel = phoneModel;
    }

    public String getSystemVersion() {
        return systemVersion;
    }

    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public void setSdkVersion(String sdkVersion) {
        this.sdkVersion = sdkVersion;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
}
