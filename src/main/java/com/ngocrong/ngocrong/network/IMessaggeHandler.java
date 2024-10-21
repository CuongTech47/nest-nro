package com.ngocrong.ngocrong.network;

import com.ngocrong.ngocrong.user.Char;

import java.io.IOException;

public interface IMessaggeHandler {
    public void setService(IService service);

    public void onMessage(Message message) throws IOException;

    public void setChar(Char _char);

    public void onConnectionFail();

    public void onDisconnected();

    public void onConnectOK();

    public void close();
}
