package net.packets.server;

import java.io.IOException;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;
import net.packets.dataobjects.Location;

public class GotoPacket extends Packet {
    public int objectId;
    public Location location;
    
    @Override
    public PacketType type() {return PacketType.GOTO;}
    
    @Override
    public void read(PacketReader r) throws IOException {
        this.objectId = r.readInt();
        this.location = (Location)new Location().read(r);
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeInt(this.objectId);
        this.location.write(w);
    }    
}
