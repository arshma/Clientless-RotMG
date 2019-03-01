package net.packets.server;

import java.io.IOException;
import java.util.ArrayList;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class AccountListPacket extends Packet {
    public int accountListId;
    //public String[] accountIds;
    public ArrayList<String> accountIds;
    public int lockAction;
    
    @Override
    public PacketType type() {return PacketType.ACCOUNTLIST;}
    
    @Override
    public void read(PacketReader r) throws IOException {
        this.accountListId = r.readInt();
        
        int size = r.readShort();
        this.accountIds = new ArrayList<String>(size);
        for(int i = 0; i < size; i++) {
            this.accountIds.add(r.readUTF());
        }
        
        this.lockAction = r.readInt();
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeInt(this.accountListId);
        w.writeShort(this.accountIds.size());
        for(String s : this.accountIds) {
            w.writeUTF(s);
        }
        w.writeInt(this.lockAction);
    }    
}
