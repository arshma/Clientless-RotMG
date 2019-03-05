package net.packets.server;

import java.io.IOException;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class NotificationPacket extends Packet {
    public int objectId;
    public String message;
    public int color;
    
    @Override
    public PacketType type() {return PacketType.NOTIFICATION;}
    
    @Override
    public void read(PacketReader r) throws IOException {
        this.objectId = r.readInt();
        this.message = r.readUTF();
        this.color = r.readInt();
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeInt(this.objectId);
        w.writeUTF(this.message);
        w.writeInt(this.color);
    }    
}
