package net.packets.server;

import java.io.IOException;
import java.util.ArrayList;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class TradeAcceptedPacket extends Packet {
    public ArrayList<Boolean> myOffers;
    public ArrayList<Boolean> partnerOffers;
    
    @Override
    public PacketType type() {return PacketType.TRADEACCEPTED;}
    
    @Override
    public void read(PacketReader r) throws IOException {
        short size = r.readShort();
        this.myOffers = new ArrayList<Boolean>(size);
        for(short i = 0; i < size; i++) {
            this.myOffers.add(r.readBoolean());
        }
        
        size = r.readShort();
        this.partnerOffers = new ArrayList<Boolean>(size);
        for(int i = 0; i < size; i++) {
            this.partnerOffers.add(r.readBoolean());
        }
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeShort(this.myOffers.size());
        for(Boolean b : this.myOffers) {
            w.writeBoolean(b);
        }
        
        w.writeShort(this.partnerOffers.size());
        for(Boolean b : this.partnerOffers) {
            w.writeBoolean(b);
        }
    }    
}
