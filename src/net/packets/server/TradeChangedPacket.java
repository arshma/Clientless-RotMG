package net.packets.server;

import java.io.IOException;
import java.util.ArrayList;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class TradeChangedPacket extends Packet {
    public ArrayList<Boolean> offers;   //list of selected trade items in partner's inv
    
    @Override
    public PacketType type() {return PacketType.TRADECHANGED;}
    
    @Override
    public void read(PacketReader r) throws IOException {
        short size = r.readShort();
        this.offers = new ArrayList<Boolean>(size);
        for(short i = 0; i < size; i++) {
            this.offers.add(r.readBoolean());
        }
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeShort(this.offers.size());
        for(Boolean b : this.offers) {
            w.writeBoolean(b);
        }
    }    
}
