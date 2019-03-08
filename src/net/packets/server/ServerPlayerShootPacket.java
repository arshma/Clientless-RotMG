package net.packets.server;

import java.io.IOException;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;
import net.packets.dataobjects.Location;

public class ServerPlayerShootPacket extends Packet {
    public int bulletId;        //ubyte
    public int ownerId;
    public int containerType;
    public Location startingLoc;
    public float angle;
    public short damage;
    
    @Override
    public PacketType type() {return PacketType.SERVERPLAYERSHOOT;}

    @Override
    public void read(PacketReader r) throws IOException {
        this.bulletId = r.readUnsignedByte();
        this.ownerId = r.readInt();
        this.containerType = r.readInt();
        this.startingLoc = (Location)new Location().read(r);
        this.angle = r.readFloat();
        this.damage = r.readShort();
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeByte(this.bulletId);
        w.writeInt(this.ownerId);
        w.writeInt(this.containerType);
        this.startingLoc.write(w);
        w.writeFloat(this.angle);
        w.writeShort(this.damage);
    }    
}
