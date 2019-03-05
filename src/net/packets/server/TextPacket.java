package net.packets.server;

import java.io.IOException;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class TextPacket extends Packet {
    public String name;
    public int objectId;
    public int numStars;
    public byte bubbleTime;
    public String recipient;
    public String text;
    public String cleanText;
    
    @Override
    public PacketType type() {return PacketType.TEXT;}
    
    @Override
    public void read(PacketReader r) throws IOException {
        this.name = r.readUTF();
        this.objectId = r.readInt();
        this.numStars = r.readInt();
        this.bubbleTime = r.readByte();
        this.recipient = r.readUTF();
        this.text = r.readUTF();
        this.cleanText = r.readUTF();
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeUTF(this.name);
        w.writeInt(this.objectId);
        w.writeInt(this.numStars);
        w.writeByte(this.bubbleTime);
        w.writeUTF(this.recipient);
        w.writeUTF(this.text);
        w.writeUTF(this.cleanText);
    }   
}
