package net.packets.dataobjects;

import java.lang.Cloneable;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public abstract class DataObject implements Cloneable {
    public abstract DataObject read(PacketReader r) throws java.io.IOException;
    public abstract void write(PacketWriter w) throws java.io.IOException;
}
