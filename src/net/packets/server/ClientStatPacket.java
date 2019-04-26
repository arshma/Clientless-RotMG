package net.packets.server;

import java.io.IOException;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class ClientStatPacket extends Packet {
    public String name;
    public int value;
    
    @Override
    public PacketType type() {return PacketType.CLIENTSTAT;}

    @Override
    public void read(PacketReader r) throws IOException {
        this.name = r.readUTF();
        this.value = r.readInt();
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeUTF(this.name);
        w.writeInt(this.value);
    }
    
}
