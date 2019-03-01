package net.packets.dataobjects;

import java.io.IOException;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class SlotObject extends DataObject {
    public int objectId;
    public byte slotId;
    public int objectType;
    
    @Override
    public DataObject read(PacketReader r) throws IOException {
        this.objectId = r.readInt();
        this.slotId = r.readByte();
        this.objectType = r.readInt();
        
        return this;
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeInt(this.objectId);
        w.writeByte(this.slotId);
        w.writeInt(this.objectType);
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    @Override
    public String toString() {
        return "{ objectId=" + this.objectId + ", slotId=" + this.slotId + ", objectType=" + this.objectType + " }";
    }    
}
