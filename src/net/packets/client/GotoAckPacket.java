package net.packets.client;

import java.io.IOException;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class GotoAckPacket extends Packet {
    public int time;
    
    @Override
    public PacketType type() {return PacketType.GOTOACK;}

    @Override
    public void read(PacketReader r) throws IOException {
        this.time = r.readInt();
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeInt(this.time);
    }    
}
