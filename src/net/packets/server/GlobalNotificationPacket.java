package net.packets.server;

import java.io.IOException;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class GlobalNotificationPacket extends Packet{
    public int typeId;
    public String text;
    
    @Override
    public PacketType type() {return PacketType.GLOBALNOTIFICATION;}
    
    @Override
    public void read(PacketReader r) throws IOException {
        this.typeId = r.readInt();
        this.text = r.readUTF();
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeInt(this.typeId);
        w.writeUTF(this.text);
    }    
}
