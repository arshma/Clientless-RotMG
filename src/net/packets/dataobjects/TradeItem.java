package net.packets.dataobjects;

import java.io.IOException;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class TradeItem extends DataObject {
    public int item;
    public int slotType;
    public boolean tradable;
    public boolean included;
    
    @Override
    public DataObject read(PacketReader r) throws IOException {
        this.item = r.readInt();
        this.slotType = r.readInt();
        this.tradable = r.readBoolean();
        this.included = r.readBoolean();
        
        return this;
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeInt(this.item);
        w.writeInt(this.slotType);
        w.writeBoolean(this.tradable);
        w.writeBoolean(this.included);
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    @Override
    public String toString() {
        return "{ Item=" + this.item + ", slotType=" + this.slotType + ", Tradable=" + this.tradable + ", included=" + this.included + " }";
    }
}
