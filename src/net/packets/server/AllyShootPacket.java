package net.packets.server;

import java.io.IOException;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class AllyShootPacket extends Packet {
    public byte bulletId;       //ubyte
    public int ownerId;
    public short containerType;
    public float angle;
    
    @Override
    public PacketType type() {return PacketType.ALLYSHOOT;}

    @Override
    public void read(PacketReader r) throws IOException {
        this.bulletId = r.readByte();
        this.ownerId = r.readInt();
        this.containerType = r.readShort();
        this.angle = r.readFloat();
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeByte(this.bulletId);
        w.writeInt(this.ownerId);
        w.writeShort(this.containerType);
        w.writeFloat(this.angle);
    }    
}
