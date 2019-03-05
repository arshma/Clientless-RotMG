package net.packets.server;

import java.io.IOException;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;
import net.packets.dataobjects.Location;
import util.Constants.ARGB;
import util.Constants.EffectType;

public class ShowEffectPacket extends Packet {
    public EffectType effectType;
    public int targetId;
    public Location posA;
    public Location posB;
    public ARGB color;
    public float duration;
    
    @Override
    public PacketType type() {return PacketType.SHOWEFFECT;}
    
    @Override
    public void read(PacketReader r) throws IOException {
        this.effectType = EffectType.getConst(r.readByte());
        this.targetId = r.readInt();
        this.posA = (Location)new Location().read(r);
        this.posB = (Location)new Location().read(r);
        this.color = ARGB.read(r);
        this.duration = r.readFloat();
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeByte(this.effectType.getVal());
        w.writeInt(this.targetId);
        this.posA.write(w);
        this.posB.write(w);
        this.color.write(w);
        w.writeFloat(this.duration);
    }    
}
