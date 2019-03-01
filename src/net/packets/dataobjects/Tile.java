package net.packets.dataobjects;

import java.io.IOException;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class Tile extends DataObject {
    public short x;
    public short y;
    public int type;        //ushort
    
    @Override
    public DataObject read(PacketReader r) throws IOException {
        this.x = r.readShort();
        this.y = r.readShort();
        this.type = r.readUnsignedShort();
        
        return this;
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeShort(this.x);
        w.writeShort(this.y);
        w.writeShort(type);
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    @Override
    public String toString() {
        return "{ x=" + this.x + ", y=" + this.y + ", type=" + this.type + " }";
    }    
}
