package net.qiujuer.web.italker.push.factory;

import com.gs.imsdk.data.DeviceInfo;
import net.qiujuer.web.italker.push.bean.db.Device;
import net.qiujuer.web.italker.push.utils.Hib;

public class DeviceFactory {

    // 通过id找到Device
    public static Device findById(String id) {
        // 通过Id查询，更方便
        return Hib.query(session -> session.get(Device.class, id));
    }

    /**
     * 更新Device信息到数据库
     * @param device
     * @return
     */
    public static Device update(Device device) {
        return Hib.query(session -> {
            session.saveOrUpdate(device);

            // 写入到数据库
            session.flush();

            // 紧接着从数据库中查询出来
            session.refresh(device);
            return device;
        });
    }

    /**
     * 保存数据库
     * @param deviceInfo
     * @param pushID
     * @return
     */
    public static Device createDevice(DeviceInfo deviceInfo, String pushID) {
        Device device = new Device(deviceInfo,pushID);

        // 数据库存储
        return Hib.query(session -> {
            session.save(device);
            // 写入到数据库
            session.flush();

            // 紧接着从数据库中查询出来
            session.refresh(device);
            return device;
        });
    }
}
