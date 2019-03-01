package net.packets.client;

import java.io.IOException;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class PongPacket extends Packet {
    public int serial;
    public int time;
    
    @Override
    public PacketType type() {return PacketType.PONG;}
    
    @Override
    public void read(PacketReader r) throws IOException {
        this.serial = r.readInt();
        this.time = r.readInt();
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeInt(this.serial);
        w.writeInt(this.time);
    }    
}
