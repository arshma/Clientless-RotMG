package net.packets.dataobjects;

import java.io.IOException;
import java.util.ArrayList;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class Status extends DataObject {
    public int objectId;
    public Location position = new Location();
    public ArrayList<StatData> data = new ArrayList<StatData>();
    
    @Override
    public DataObject read(PacketReader r) throws IOException {
        this.objectId = r.readInt();
        this.position.read(r);
        
        short size = r.readShort();
        this.data = new ArrayList<StatData>(size);
        for(int i = 0; i < size; i++) {
            StatData sd = new StatData();
            sd.read(r);
            this.data.add(sd);
        }
        return this;
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeInt(this.objectId);
        this.position.write(w);
        w.writeShort(this.data.size());
        for(StatData sd : this.data) {
            sd.write(w);
        }
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        Status s = (Status)super.clone();
        s.position = (Location)this.position.clone();
        //deep copy each element in the array list.
        for(StatData sd : this.data) {
            s.data.add((StatData)sd.clone());
        }
        return s;
    }    
}
