package net.qiujuer.web.italker.push.bean.db;

import com.gs.imsdk.data.DeviceInfo;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.security.Principal;
import java.time.LocalDateTime;

@Entity
@Table(name = "TB_DEVICE")
public class Device {

    // 这是一个主键
    @Id
    @PrimaryKeyJoinColumn
    // 主键生成存储的类型为UUID
    // 这里不自动生成UUID，Id由代码写入，由客户端负责生成
    // 避免复杂的服务器和客户端的映射关系
    //@GeneratedValue(generator = "uuid")
    // 把uuid的生成器定义为uuid2，uuid2是常规的UUID toString
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    // 不允许更改，不允许为null
    @Column(updatable = false, nullable = false)
    private String id;  // 唯一标识

    @Column(nullable = false)
    private String androidID;

    @Column(nullable = false)
    private String fingerPrint;

    @Column
    private String simSerial;   // sim 的序列号

    @Column
    private String IMEI;

    @Column
    private String serialNumber;

    @Column
    private String phoneProducer;

    @Column
    private String phoneModel;

    @Column
    private String systemVersion;

    @Column
    private String sdkVersion;

    @Column
    private String mac;

    @Column
    private String pushID;

    // 定义为创建时间戳，在创建时就已经写入
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createAt = LocalDateTime.now();

    // 定义为更新时间戳，在创建时就已经写入
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updateAt = LocalDateTime.now();

    public Device(){

    }

    public Device(DeviceInfo deviceInfo, String pushID){
        this.id = deviceInfo.getId();
        this.androidID = deviceInfo.getAndroidID();
        this.fingerPrint = deviceInfo.getFingerPrint();
        this.simSerial = deviceInfo.getSimSerial();
        this.IMEI = deviceInfo.getIMEI();
        this.serialNumber = deviceInfo.getSerialNumber();
        this.phoneProducer = deviceInfo.getPhoneProducer();
        this.phoneModel = deviceInfo.getPhoneModel();
        this.systemVersion = deviceInfo.getSystemVersion();
        this.sdkVersion = deviceInfo.getSdkVersion();
        this.mac = deviceInfo.getMac();
        this.pushID = pushID;
    }

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

    public String getPushID() {
        return pushID;
    }

    public void setPushID(String pushID) {
        this.pushID = pushID;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }
}
