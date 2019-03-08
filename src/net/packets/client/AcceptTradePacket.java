package net.packets.client;

import java.io.IOException;
import java.util.ArrayList;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class AcceptTradePacket extends Packet {
    public ArrayList<Boolean> myOffers;
    public ArrayList<Boolean> yourOffers;
    
    @Override
    public PacketType type() {return PacketType.ACCEPTTRADE;}
    
    @Override
    public void read(PacketReader r) throws IOException {
        short size = r.readShort();
        this.myOffers = new ArrayList<Boolean>(size);
        for(short i = 0; i < size; i++) {
            this.myOffers.add(r.readBoolean());
        }
        
        size = r.readShort();
        this.yourOffers = new ArrayList<Boolean>(size);
        for(short i = 0; i < size; i++) {
            this.yourOffers.add(r.readBoolean());
        }
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeShort(this.myOffers.size());
        for(Boolean b : this.myOffers) {
            w.writeBoolean(b);
        }
        
        w.writeShort(this.yourOffers.size());
        for(Boolean b : this.yourOffers) {
            w.writeBoolean(b);
        }
    }    
}
