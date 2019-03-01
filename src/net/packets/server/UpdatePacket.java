package net.packets.server;

import java.io.IOException;
import java.util.ArrayList;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;
import net.packets.dataobjects.Entity;
import net.packets.dataobjects.Tile;

public class UpdatePacket extends Packet {
    public ArrayList<Tile> tiles;
    public ArrayList<Entity> newObjs;
    public ArrayList<Integer> drops;
    
    @Override
    public PacketType type() {return PacketType.UPDATE;}
    
    @Override
    public void read(PacketReader r) throws IOException {        
        int size = r.readShort();        
        this.tiles = new ArrayList<Tile>(size);
        for(int i = 0; i < size; i++) {
            this.tiles.add((Tile)new Tile().read(r));
        }
        
        size = r.readShort();
        this.newObjs = new ArrayList<Entity>(size);
        for(int i = 0; i < size; i++) {
            this.newObjs.add((Entity)new Entity().read(r));
        }
        
        size = r.readShort();
        this.drops = new ArrayList<Integer>(size);
        for(int i = 0; i < size; i++) {
            this.drops.add(r.readInt());
        }        
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeShort(this.tiles.size());
        for(Tile t : this.tiles) {
            t.write(w);
        }
        
        w.writeShort(this.newObjs.size());
        for(Entity e : this.newObjs) {
            e.write(w);
        }
        
        w.writeShort(this.drops.size());
        for(Integer i : this.drops) {
            w.writeInt(i.intValue());
        }
    }    
}
