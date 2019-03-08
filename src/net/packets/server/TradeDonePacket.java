package net.packets.server;

import java.io.IOException;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class TradeDonePacket extends Packet {
    public int result; //0 => trade successful; 1 => trade canceled
    public String message;
    
    @Override
    public PacketType type() {return PacketType.TRADEDONE;}
    
    @Override
    public void read(PacketReader r) throws IOException {
        this.result = r.readInt();
        this.message = r.readUTF();
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeInt(this.result);
        w.writeUTF(this.message);
    }    
}
