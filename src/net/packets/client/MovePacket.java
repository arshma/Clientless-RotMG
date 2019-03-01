package net.packets.client;

import java.io.IOException;
import java.util.ArrayList;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;
import net.packets.dataobjects.Location;
import net.packets.dataobjects.LocationRecord;

public class MovePacket extends Packet {
    public int tickId;
    public int time;
    public Location newPosition;
    public ArrayList<LocationRecord> records;
    
    @Override
    public PacketType type() {return PacketType.MOVE;}
    
    @Override
    public void read(PacketReader r) throws IOException {
        this.tickId = r.readInt();
        this.time = r.readInt();
        this.newPosition = (Location)new Location().read(r);
        
        int size = r.readShort();
        this.records = new ArrayList<LocationRecord>(size);
        for(int i = 0; i < size; i++) {
            this.records.add((LocationRecord)new LocationRecord().read(r));
        }
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeInt(this.tickId);
        w.writeInt(this.time);
        this.newPosition.write(w);
        
        w.writeShort(this.records.size());
        for(LocationRecord lr : this.records) {
            lr.write(w);
        }
    }    
}
