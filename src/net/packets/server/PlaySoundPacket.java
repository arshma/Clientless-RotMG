package net.packets.server;

import java.io.IOException;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class PlaySoundPacket extends Packet {
    public int ownerId;
    public int soundId;     //ubyte
    
    @Override
    public PacketType type() {return PacketType.PLAYSOUND; }
    
    @Override
    public void read(PacketReader r) throws IOException {
        this.ownerId = r.readInt();
        this.soundId = r.readUnsignedByte();
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeInt(this.ownerId);
        w.writeByte(this.soundId);
    }    
}
