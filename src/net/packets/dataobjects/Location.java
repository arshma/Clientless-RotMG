package net.packets.dataobjects;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class Location extends DataObject {
    public float x;
    public float y;
    
    public Location() {}
    
    public Location(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public static Location empty() {
        return new Location(0, 0);
    }
    
    @Override
    public DataObject read(PacketReader r) throws IOException {
        this.x = r.readFloat();
        this.y = r.readFloat();
        return this;
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeFloat(this.x);
        w.writeFloat(this.y);
    }
    
    public float distanceSquaredTo(Location loc) {
        float dx = loc.x;
        float dy = loc.y;
        return dx*dx + dy*dy;
    }
    
    public float distanceTo(Location loc) {
        return (float)Math.sqrt(distanceSquaredTo(loc));
    }
    
    private float getAngle(Location l1, Location l2) {
        float dx = l2.x - l1.x;
        float dy = l2.y - l1.y;
        return (float)Math.atan2(dx, dy);
    }
    
    private float getAngle(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float)Math.atan2(dx, dy);
    }
    
    public boolean isSameAs(Location other) {
        if(other == null) {
            return false;
        } else if(other == this) {
            return true;
        }
        return ((this.x == other.x) && (this.y == other.y));
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    
    @Override
    public String toString() {
        return "{x=" + x + ", y=" + y + "}";
    }
}
