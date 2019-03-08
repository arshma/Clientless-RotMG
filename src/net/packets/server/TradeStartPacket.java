package net.packets.server;

import java.io.IOException;
import java.util.ArrayList;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;
import net.packets.dataobjects.TradeItem;

public class TradeStartPacket extends Packet {
    public ArrayList<TradeItem> myItems;
    public String partnerName;
    public ArrayList<TradeItem> partnerItems;
    
    @Override
    public PacketType type() {return PacketType.TRADESTART;}

    @Override
    public void read(PacketReader r) throws IOException {
        short size = r.readShort();
        this.myItems = new ArrayList<TradeItem>(size);
        for(short i = 0; i < size; i++) {
            this.myItems.add((TradeItem)new TradeItem().read(r));
        }
        
        this.partnerName = r.readUTF();
        
        size = r.readShort();
        this.partnerItems = new ArrayList<TradeItem>(size);
        for(short i = 0; i < size; i++) {
            this.partnerItems.add((TradeItem)new TradeItem().read(r));
        }
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeShort(this.myItems.size());
        for(TradeItem item : this.myItems) {
            item.write(w);
        }
        
        w.writeUTF(this.partnerName);
        
        w.writeShort(this.partnerItems.size());
        for(TradeItem item : this.partnerItems) {
            item.write(w);
        }
    }    
}
