package net.packets.dataobjects;

import java.io.IOException;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class Entity extends DataObject {
    public int objectType;        //short
    public Status status = new Status();

    @Override
    public DataObject read(PacketReader r) throws IOException {
        this.objectType = r.readShort();
        this.status.read(r);
        
        return this;
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeShort(this.objectType);
        this.status.write(w);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Entity e = (Entity)super.clone();
        e.status = (Status)this.status.clone();
        
        return e;
    }    
}
