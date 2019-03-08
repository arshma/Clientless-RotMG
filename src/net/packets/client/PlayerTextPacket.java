package net.packets.client;

import java.io.IOException;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class PlayerTextPacket extends Packet {
    public String text;
    
    @Override
    public PacketType type() {return PacketType.PLAYERTEXT;}
    
    @Override
    public void read(PacketReader r) throws IOException {
        this.text = r.readUTF();
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeUTF(this.text);
    }    
}
