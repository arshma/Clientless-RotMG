package net.packets.server;

import java.io.IOException;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class FailurePacket extends Packet {
    public int errorId;
    public String errorMessage;
    
    @Override
    public PacketType type() {return PacketType.FAILURE;}
    
    @Override
    public void read(PacketReader r) throws IOException {
        this.errorId = r.readInt();
        this.errorMessage = r.readUTF();
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeInt(this.errorId);
        w.writeUTF(this.errorMessage);
    }    
}
