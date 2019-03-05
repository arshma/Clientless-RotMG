package net.packets.client;

import java.io.IOException;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class HelloPacket extends Packet {
    public String buildVersion;
    public int gameId;
    public String guid;
    public int random1;
    public String password;
    public int random2;
    public String secret;
    public int keyTime;
    public byte[] key;          //Size needs to be written/read before writing/reading rest of the data.
    public String mapJson;      //Not UTF-8 and thus needs to be read in a special way.
    public String entryTag;
    public String gameNet;
    public String gameNetUserId;
    public String playPlatform;
    public String platformToken;
    public String userToken;

    @Override
    public PacketType type() {return PacketType.HELLO;}

    @Override
    public void read(PacketReader r) throws IOException {
        this.buildVersion = r.readUTF();
        this.gameId = r.readInt();
        this.guid = r.readUTF();
        this.random1 = r.readInt();
        this.password = r.readUTF();
        this.random2 = r.readInt();
        this.secret = r.readUTF();
        this.keyTime = r.readInt();
        
        //extract array length before reading the rest of the array.
        this.key = new byte[r.readShort()];
        r.readFully(this.key);
        //This is not UTF-8, the size is stored as int rather than short. what is correct format?
        byte[] buf = new byte[r.readInt()];
        r.readFully(buf);
        
        this.mapJson = new String(buf, java.nio.charset.Charset.forName("UTF-8"));
        this.entryTag = r.readUTF();
        this.gameNet = r.readUTF();
        this.gameNetUserId = r.readUTF();
        this.playPlatform = r.readUTF();
        this.platformToken = r.readUTF();
        this.userToken = r.readUTF();
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeUTF(this.buildVersion);
        w.writeInt(this.gameId);
        w.writeUTF(this.guid);
        w.writeInt(this.random1);
        w.writeUTF(this.password);
        w.writeInt(this.random2);
        w.writeUTF(this.secret);
        w.writeInt(this.keyTime);
        
        //size of byte array is written before writing the data.
        w.writeShort(this.key.length);
        w.write(this.key);
        //size of the string is written since it's not UTF-8 but other type.
        w.writeInt(this.mapJson.getBytes("UTF-8").length);
        w.write(this.mapJson.getBytes("UTF-8"));
        
        w.writeUTF(this.entryTag);
        w.writeUTF(this.gameNet);
        w.writeUTF(this.gameNetUserId);
        w.writeUTF(this.playPlatform);
        w.writeUTF(this.platformToken);
        w.writeUTF(this.userToken);
    }
    
    @Override
    public String toString() {
        String s =  "HELLO [" + this.buildVersion + ", " + this.gameId + ", " + this.guid + ", " + 
                    this.random1 + ", " + this.password + ", " + this.random2 + ", " + this.secret + ", " +
                    this.keyTime + ", " + this.key.length + ", " + this.mapJson + ", " + this.entryTag + ", " + 
                    this.gameNet + ", " + this.gameNetUserId + ", " + this.playPlatform + ", " +
                    this.platformToken + ", " + this.userToken + "]";        
        return s;
    }
}
