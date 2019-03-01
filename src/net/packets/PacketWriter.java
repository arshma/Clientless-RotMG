package net.packets;

import java.io.IOException;

public class PacketWriter implements java.io.DataOutput {
    private java.nio.ByteBuffer buffer;
    
    public PacketWriter() {
        this.buffer = java.nio.ByteBuffer.wrap(new byte[100000]);
    }
    public PacketWriter(byte[] dst) {
        this.buffer = java.nio.ByteBuffer.wrap(dst);
    }
    
    public byte[] getArray() {
        return java.util.Arrays.copyOf(this.buffer.array(), this.buffer.position());
    }

    @Override
    public void write(int b) throws IOException {
        writeByte(b);        
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.buffer.put(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.buffer.put(b, off, len);
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        if(v) {
            this.buffer.put((byte)0x01);
        } else {
            this.buffer.put((byte)0x00);
        }
    }

    @Override
    public void writeByte(int v) throws IOException {
        this.buffer.put((byte)v);
    }

    @Override
    public void writeShort(int v) throws IOException {
        this.buffer.putShort((short)v);
    }

    @Override
    public void writeChar(int v) throws IOException {
        this.buffer.putChar((char)v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        this.buffer.putInt(v);
    }
    
    public void writeUnsignedInt(long v) throws IOException {
        this.buffer.putInt((int)(v));
    }

    @Override
    public void writeLong(long v) throws IOException {
        this.buffer.putLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        this.buffer.putFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        this.buffer.putDouble(v);
    }

    @Override
    public void writeBytes(String s) throws IOException {
        this.buffer.put(s.getBytes());
    }

    @Override
    public void writeChars(String s) throws IOException {
        if(s.length() == 0) {
            return;
        }
        char[] cArr = s.toCharArray();
        for(char ch :  cArr) {
            this.writeChar(ch);
        }
    }

    @Override
    public void writeUTF(String s) throws IOException {
        byte[] bytes = s.getBytes("UTF-8");
        this.writeShort(bytes.length);
        this.write(bytes);
    }
    
    public void writeUTF32(String s) throws IOException {
        byte[] bytes = s.getBytes("UTF-8");
        this.writeInt(bytes.length);
        this.write(bytes);
    }
}
