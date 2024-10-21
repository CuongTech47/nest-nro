package com.ngocrong.ngocrong.network;

import org.aspectj.bridge.IMessageHandler;

public interface ISession {
    public abstract boolean isConnected();

    public abstract void setHandler(IMessageHandler messageHandler);

    public abstract void sendMessage(Message message);

    public abstract void setService(IService service);

    public abstract void close();

    public abstract void disconnect();
}
