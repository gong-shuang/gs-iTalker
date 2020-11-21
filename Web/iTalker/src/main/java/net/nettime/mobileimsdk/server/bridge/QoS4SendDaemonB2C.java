//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.nettime.mobileimsdk.server.bridge;

import net.openmob.mobileimsdk.server.qos.QoS4SendDaemonRoot;

public class QoS4SendDaemonB2C extends QoS4SendDaemonRoot {
    private static QoS4SendDaemonB2C instance = null;

    public static QoS4SendDaemonB2C getInstance() {
        if (instance == null) {
            instance = new QoS4SendDaemonB2C();
        }

        return instance;
    }

    private QoS4SendDaemonB2C() {
        super(3000, 2000, -1, true, "-桥接QoS！");
    }
}
