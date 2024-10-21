package com.ngocrong.ngocrong.network;

import com.ngocrong.ngocrong.user.Char;

public interface IService {
    public abstract void setChar(Char _char);

    public abstract void close();

    public abstract void setResource();
}
