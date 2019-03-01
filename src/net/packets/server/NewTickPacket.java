package net.packets.server;

import java.io.IOException;
import java.util.ArrayList;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;
import net.packets.dataobjects.Status;

public class NewTickPacket extends Packet {
    public int tickId;
    public int tickTime;
    public ArrayList<Status> statuses;
        
    @Override
    public PacketType type() {return PacketType.NEWTICK;}
    
    @Override
    public void read(PacketReader r) throws IOException {
        this.tickId = r.readInt();
        this.tickTime = r.readInt();
        
        int size = r.readShort();
        this.statuses = new ArrayList<Status>(size);
        for(int i = 0; i < size; i++) {
            this.statuses.add((Status)new Status().read(r));
        }
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeInt(this.tickId);
        w.writeInt(this.tickTime);
        w.writeShort(this.statuses.size());
        for(Status s : this.statuses) {
            s.write(w);
        }
    }
    
}
