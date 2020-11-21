package net.qiujuer.web.italker.push.factory;

import com.gs.imsdk.data.CommonInfo;
import com.gs.imsdk.data.DeviceInfo;
import net.qiujuer.web.italker.push.bean.db.Device;
import net.qiujuer.web.italker.push.bean.db.UdpMessage;
import net.qiujuer.web.italker.push.utils.Hib;

public class UdpMessageFactory {

    // 通过id找到Device
    public static UdpMessage findById(String id) {
        // 通过Id查询，更方便
        return Hib.query(session -> session.get(UdpMessage.class, id));
    }

    /**
     * 更新Device信息到数据库
     * @param message
     * @return
     */
    public static UdpMessage update(UdpMessage message) {
        return Hib.query(session -> {
            session.saveOrUpdate(message);

            // 写入到数据库
            session.flush();

            // 紧接着从数据库中查询出来
            session.refresh(message);


            return message;
        });
    }

    /**
     * 保存数据库
     * @param info
     * @return
     */
    public static UdpMessage createDevice(CommonInfo info) {
        UdpMessage message = new UdpMessage(info);

        // 数据库存储
        return Hib.query(session -> {
            session.save(message);

            // 写入到数据库
            session.flush();

            // 紧接着从数据库中查询出来
            session.refresh(message);

            return message;
        });
    }
}
