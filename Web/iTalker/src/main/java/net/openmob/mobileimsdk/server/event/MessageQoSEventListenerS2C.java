package net.openmob.mobileimsdk.server.event;

import java.util.ArrayList;
import net.openmob.mobileimsdk.server.protocal.Protocal;

public interface MessageQoSEventListenerS2C {
    void messagesLost(ArrayList<Protocal> var1);

    void messagesBeReceived(String var1);
}
