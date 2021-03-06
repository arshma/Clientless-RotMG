package net.packets.server;

import java.io.IOException;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class PingPacket extends Packet {
    public int serial;
    
    @Override
    public PacketType type() {return PacketType.PING;}

    @Override
    public void read(PacketReader r) throws IOException {
        this.serial = r.readInt();
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeInt(this.serial);
    }    
}
