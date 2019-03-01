package net.packets;

import java.io.IOException;

public class PacketReader implements java.io.DataInput {
    private byte[] data;                        //holds reference to source of data.
    private java.nio.ByteBuffer buffer;
    
    public PacketReader(byte[] input) {
        this.data = input;
        this.buffer = java.nio.ByteBuffer.wrap(data);
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        this.buffer.get(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        try {
            this.buffer.get(b, 0, len);
        } catch(java.nio.BufferUnderflowException e) {
            throw new  java.io.EOFException();
        } catch(java.lang.IndexOutOfBoundsException e) {
            throw new java.lang.IllegalArgumentException();
        }
    }

    @Override
    public int skipBytes(int n) throws IOException {
        try {
            this.buffer.position(this.buffer.position() + n);
            return n;
        } catch(java.lang.IllegalArgumentException e) {
            throw e;
        }
    }
    
    //bool is 1-byte value in rotmg.
    @Override
    public boolean readBoolean() throws IOException {
        try {
            int b = this.buffer.get();
            if(b > 0) {
                return true;
            } else {
                return false;
            }
        } catch(java.nio.BufferUnderflowException e) {
            throw new java.io.EOFException();
        }
    }

    @Override
    public byte readByte() throws IOException {
        try {
            return this.buffer.get();
        } catch(java.nio.BufferUnderflowException e) {
            throw new java.io.EOFException();
        }
    }

    @Override
    public int readUnsignedByte() throws IOException {
        try {
            int b = this.buffer.get();
            return (b & 0xFF);
        } catch(java.nio.BufferUnderflowException e) {
            throw new java.io.EOFException();
        }
    }

    @Override
    public short readShort() throws IOException {
        try {
            return this.buffer.getShort();
        } catch(java.nio.BufferUnderflowException e) {
            throw new java.io.EOFException();
        }
    }

    @Override
    public int readUnsignedShort() throws IOException {
        try {
            int s = this.buffer.getShort();
            return (s & 0xFFFF);
        } catch(java.nio.BufferUnderflowException e) {
            throw new java.io.EOFException();
        }
    }

    @Override
    public char readChar() throws IOException {
        try {
            return this.buffer.getChar();
        } catch(java.nio.BufferUnderflowException e) {
            throw new java.io.EOFException();
        }
    }

    @Override
    public int readInt() throws IOException {
        try {
            return this.buffer.getInt();
        } catch(java.nio.BufferUnderflowException e) {
            throw new java.io.EOFException();
        }
    }
    
    public long readUnsignedInt() throws IOException {
        try {
            long num = this.readInt();
            return (num & 0xFFFFFFFFL);
        } catch(java.nio.BufferUnderflowException e) {
            throw new java.io.EOFException();
        }
    }

    @Override
    public long readLong() throws IOException {
        try {
            return this.buffer.getLong();
        } catch(java.nio.BufferUnderflowException e) {
            throw new java.io.EOFException();
        }
    }

    @Override
    public float readFloat() throws IOException {
        try {
            return this.buffer.getFloat();
        } catch(java.nio.BufferUnderflowException e) {
            throw new java.io.EOFException();
        }
    }

    @Override
    public double readDouble() throws IOException {
        try {
            return this.buffer.getDouble();
        } catch(java.nio.BufferUnderflowException e) {
            throw new java.io.EOFException();
        }
    }

    @Override
    public String readLine() throws IOException {
        throw new java.lang.UnsupportedOperationException("Cannot read a line from a buffer.");
    }
    
    //NOTE: Internally, this will use methods defined by DataIntput to extract UTF string.
    @Override
    public String readUTF() throws IOException {
        //This will throw IOException as defined in method header.
        try {
            return java.io.DataInputStream.readUTF(this);
        } catch(java.io.IOException e) {
            throw new java.io.IOException();
        }
    }
    
    public String readUTF32() throws IOException {
        byte[] buf = new byte[this.readInt()];
        this.readFully(buf);
        return new String(buf, java.nio.charset.Charset.forName("UTF-8"));
    }
}
