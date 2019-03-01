package net.packets.server;

import java.io.IOException;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class CreateSuccessPacket extends Packet {
    public int objectId;
    public int charId;
    
    @Override
    public PacketType type() {return PacketType.CREATESUCCESS;}
    
    @Override
    public void read(PacketReader r) throws IOException {
        this.objectId = r.readInt();
        this.charId = r.readInt();
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeInt(this.objectId);
        w.writeInt(this.charId);
    }    
}
