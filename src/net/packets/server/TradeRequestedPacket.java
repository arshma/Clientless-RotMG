package net.packets.server;

import java.io.IOException;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class TradeRequestedPacket extends Packet {
    public String name;
    
    @Override
    public PacketType type() { return PacketType.TRADEREQUESTED; }
    
    @Override
    public void read(PacketReader r) throws IOException {
        this.name = r.readUTF();
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeUTF(this.name);
    }    
}
