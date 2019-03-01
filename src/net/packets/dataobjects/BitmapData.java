package net.packets.dataobjects;

import java.io.IOException;
import java.util.ArrayList;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class BitmapData extends DataObject {
    public int width;
    public int height;
    public ArrayList<Byte> bytes;
    @Override
    public DataObject read(PacketReader r) throws IOException {
        this.width = r.readInt();
        this.height = r.readInt();
        int size = this.width * this.height * 4;
        this.bytes = new ArrayList<Byte>(size);
        for(int i = 0; i < size; i++) {
            this.bytes.add(r.readByte());
        }
        
        return this;
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeInt(this.width);
        w.writeInt(this.height);
        for(byte b : this.bytes) {
            w.writeByte(b);
        }
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        BitmapData bd = (BitmapData)super.clone();
        bd.bytes = new ArrayList<Byte>(this.bytes.size());
        for(byte b : this.bytes) {
            bd.bytes.add(b);
        }
        return bd;
    }
    
    @Override
    public String toString() {
        return "{width=" + this.width + ", height=" + this.height + " }";
    }
}
