package net.packets.client;

import java.io.IOException;
import java.util.ArrayList;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class ChangeTradePacket extends Packet {
    public ArrayList<Boolean> offers;
    
    @Override
    public PacketType type() {return PacketType.CHANGETRADE;}
    
    @Override
    public void read(PacketReader r) throws IOException {
        short size = r.readShort();
        this.offers = new ArrayList<Boolean>();
        for(int i = 0; i < size; i++) {
            this.offers.add(r.readBoolean());
        }
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeShort(offers.size());
        for(Boolean b : this.offers) {
            w.writeBoolean(b);
        }
    }    
}
