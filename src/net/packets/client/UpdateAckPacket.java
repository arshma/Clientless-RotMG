package net.packets.client;

import java.io.IOException;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class UpdateAckPacket extends Packet {
    
    @Override
    public PacketType type() {return PacketType.UPDATEACK;}
    
    @Override
    public void read(PacketReader r) throws IOException {
    }

    @Override
    public void write(PacketWriter w) throws IOException {
    }    
}
