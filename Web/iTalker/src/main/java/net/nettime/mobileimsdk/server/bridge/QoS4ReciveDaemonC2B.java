//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.nettime.mobileimsdk.server.bridge;

import net.openmob.mobileimsdk.server.qos.QoS4ReciveDaemonRoot;

public class QoS4ReciveDaemonC2B extends QoS4ReciveDaemonRoot {
    private static QoS4ReciveDaemonC2B instance = null;

    public static QoS4ReciveDaemonC2B getInstance() {
        if (instance == null) {
            instance = new QoS4ReciveDaemonC2B();
        }

        return instance;
    }

    public QoS4ReciveDaemonC2B() {
        super(5000, 15000, true, "-桥接QoS！");
    }
}
