package net.packets.client;

import java.io.IOException;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class CancelTradePacket extends Packet {
    
    @Override
    public PacketType type() {return PacketType.CANCELTRADE;}
    
    @Override
    public void read(PacketReader r) throws IOException {   
    }

    @Override
    public void write(PacketWriter w) throws IOException {
    }    
}
