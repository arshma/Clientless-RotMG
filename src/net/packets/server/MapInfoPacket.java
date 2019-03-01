package net.packets.server;

import java.io.IOException;
import java.util.ArrayList;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class MapInfoPacket extends Packet {
    public int width;
    public int height;
    public String name;
    public String displayName;
    public int difficulty;
    public int fp;                      //is uint but will be read/written as int
    public int background;
    public boolean allowPlayerTeleport; 
    public boolean showDisplays;
    //public String[] clientXML;
    //public String[] extraXML;
    public ArrayList<String> clientXML;
    public ArrayList<String> extraXML;
    
    @Override
    public PacketType type() {return PacketType.MAPINFO;}

    @Override
    public void read(PacketReader r) throws IOException {
        this.width = r.readInt();
        this.height = r.readInt();
        this.name = r.readUTF();
        this.displayName = r.readUTF();
        this.difficulty = r.readInt();
        this.fp = r.readInt();
        this.background = r.readInt();
        this.allowPlayerTeleport = r.readBoolean();
        this.showDisplays = r.readBoolean();
        
        int size = r.readShort();
        this.clientXML = new ArrayList<String>(size);
        for(int i = 0; i < size; i++) {
            this.clientXML.add(r.readUTF32());
        }
        
        size = r.readShort();
        this.extraXML = new ArrayList<String>(size);
        for(int i = 0; i < size; i++) {
            this.extraXML.add(r.readUTF32());
        }
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeInt(this.width);
        w.writeInt(this.height);
        w.writeUTF(this.name);
        w.writeUTF(this.displayName);
        w.writeInt(this.difficulty);
        w.writeInt(this.fp);
        w.writeInt(this.background);
        w.writeBoolean(this.allowPlayerTeleport);
        w.writeBoolean(this.showDisplays);
        
        w.writeShort(this.clientXML.size());
        for(String s : this.clientXML) {
            w.writeUTF32(s);
        }
        
        w.writeShort(this.extraXML.size());
        for(String s : this.extraXML) {
            w.writeUTF32(s);
        }
    }
}
