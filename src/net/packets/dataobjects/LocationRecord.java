package net.packets.dataobjects;

import java.io.IOException;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class LocationRecord extends Location {
    public int time;
    
    @Override
    public DataObject read(PacketReader r) throws IOException {
        this.time = r.readInt();
        super.read(r);
        return this;
    }
    
    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeInt(this.time);
        super.write(w);
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    @Override
    public String toString() {
        return "{ Time=" + this.time + ", x=" + this.x + ", y=" + this.y + " }";
    }
}
