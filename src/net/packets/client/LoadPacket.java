package net.packets.client;

import java.io.IOException;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class LoadPacket extends Packet {
    public int characterId;
    public boolean isFromArena;
    
    @Override
    public PacketType type() {return PacketType.LOAD;}
    
    @Override
    public void read(PacketReader r) throws IOException {
        this.characterId = r.readInt();
        this.isFromArena = r.readBoolean();
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeInt(this.characterId);
        w.writeBoolean(this.isFromArena);
    }
    
}
