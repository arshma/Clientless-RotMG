package net.packets.server;

import java.io.IOException;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class InvResultPacket extends Packet {
    public int result;
    
    @Override
    public PacketType type() {return PacketType.INVRESULT;}
    
    @Override
    public void read(PacketReader r) throws IOException {
        this.result = r.readInt();
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.write(this.result);
    }    
}
