package net.packets.client;

import java.io.IOException;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;
import net.packets.dataobjects.Location;
import net.packets.dataobjects.SlotObject;

public class InvSwapPacket extends Packet {
    public int time;
    public Location position;
    public SlotObject slotObject1;
    public SlotObject slotObject2;
    
    @Override
    public PacketType type() {return PacketType.INVSWAP;}

    @Override
    public void read(PacketReader r) throws IOException {
        this.time = r.readInt();
        this.position = (Location)new Location().read(r);
        this.slotObject1 = (SlotObject)new SlotObject().read(r);
        this.slotObject2 = (SlotObject)new SlotObject().read(r);
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeInt(this.time);
        this.position.write(w);
        this.slotObject1.write(w);
        this.slotObject2.write(w);
    }    
}
