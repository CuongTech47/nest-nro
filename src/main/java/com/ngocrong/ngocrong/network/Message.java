package com.ngocrong.ngocrong.network;

import java.io.*;

public class Message {

    private byte command;
    private ByteArrayOutputStream os;
    private DataOutputStream dos;
    private ByteArrayInputStream is;
    private DataInputStream dis;

    // Constructor for initializing command with an int
    public Message(int command) {
        this((byte) command);
    }

    // Constructor for initializing command with a byte
    public Message(byte command) {
        this.command = command;
        os = new ByteArrayOutputStream();
        dos = new DataOutputStream(os);
    }

    // Constructor for initializing command and data
    public Message(byte command, byte[] data) {
        this.command = command;
        is = new ByteArrayInputStream(data);
        dis = new DataInputStream(is);
    }

    // Get the command
    public byte getCommand() {
        return command;
    }

    // Set the command using an int
    public void setCommand(int cmd) {
        setCommand((byte) cmd);
    }

    // Set the command using a byte
    public void setCommand(byte cmd) {
        this.command = cmd;
    }

    // Get the data as a byte array
    public byte[] getData() {
        return os.toByteArray();
    }

    // Get the DataInputStream for reading data
    public DataInputStream reader() {
        return dis;
    }

    // Get the DataOutputStream for writing data
    public DataOutputStream writer() {
        return dos;
    }

    // Cleanup resources
    public void cleanup() {
        try {
            if (dis != null) {
                dis.close();
            }
            if (dos != null) {
                dos.close();
            }
        } catch (IOException e) {
            // Log the exception or handle it appropriately
            e.printStackTrace(); // Consider using a logger instead of printing the stack trace
        }
    }
}
