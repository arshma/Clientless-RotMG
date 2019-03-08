package net.packets.server;

import java.io.IOException;
import java.util.ArrayList;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;
import util.Constants.ConditionEffect;

public class DamagePacket extends Packet{
    public int targetId;
    public ArrayList<ConditionEffect> effects;      //effect value should be read as ubyte
    public int damageAmount;            //ushort
    public boolean kill;
    public boolean armorPierce;
    public int bulletId;                //ubyte
    public int objectId;
    
    @Override
    public PacketType type() {return PacketType.DAMAGE;}
    
    @Override
    public void read(PacketReader r) throws IOException {
        this.targetId = r.readInt();
        
        short size = r.readShort();
        this.effects = new ArrayList<ConditionEffect>(size);
        for(short i = 0; i < size; i++) {
            this.effects.add(ConditionEffect.getConst(1 << r.readUnsignedByte()));
        }
        
        this.damageAmount = r.readUnsignedShort();
        this.kill = r.readBoolean();
        this.armorPierce = r.readBoolean();
        this.bulletId = r.readUnsignedByte();
        this.objectId = r.readInt();
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeInt(this.targetId);
        
        w.writeShort(this.effects.size());
        for(ConditionEffect c : this.effects) {
            w.writeByte(c.getVal());
        }
        
        w.writeShort(this.damageAmount);
        w.writeBoolean(this.kill);
        w.writeBoolean(this.armorPierce);
        w.writeByte(this.bulletId);
        w.writeInt(this.objectId);
    }    
}
