package net;

import crypto.RC4;
import gamedata.GameData;
import gamedata.structs.PacketNode;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import listeners.Proxy;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;
import net.packets.client.HelloPacket;
import net.packets.dataobjects.Location;

public class Client {
    private static final int LOOP_SLEEP_TIME = 100;
    private static String key0 = "6a39570cc9de4ec71d64821894";  //outgoing encoding.
    private static String key1 = "c79332b197f92ba85ed281a023";  //incoming decoding.
    private RC4 sendServer;
    private RC4 receiveServer;
    private java.net.Socket serverConnection = null;
    private java.io.DataInputStream in;
    private java.io.DataOutputStream out;
    
    private boolean running = false;
    private Proxy proxy;
    public List<Packet> sendQueue;
    
    public int myCharId;
    public int myObjectId;
    public Location position = new Location();
    public int currentTickTime = 0;
    public int lastTickTime = 0;
    public int lastTickId = 0;
    public int connectTime = 0;
    public int pingSerial = 0;
    public String server = "ec2-13-57-254-131.us-west-1.compute.amazonaws.com";
    public long start;
    
    
    public int getTime() {
        return (((int)System.currentTimeMillis()) - this.connectTime);
    }
    
    public int objectId() {
        return 0;
    }
    
    public boolean connected() {
        return running;
    }
    
    public Client(Proxy proxy) {
        try {
            this.receiveServer = new RC4(key1);
            this.sendServer = new RC4(key0);
            this.proxy = proxy;
            this.sendQueue = new ArrayList<Packet>();
            //this.sendQueue = Collections.synchronizedList(this.sendQueue);
            this.connectTime = (int)System.currentTimeMillis();
        } catch (Exception e) {
            System.out.println("ERROR: Failed to create Client.");
            e.printStackTrace();
            System.exit(-2);
        }
    }
    
    public void connect() {        
        try {
            this.serverConnection = new Socket(server, 2050);
            //this.serverConnection.connect(new java.net.InetSocketAddress(java.net.Inet4Address.getByName(server), 2050), 15000);
            
            this.in = new DataInputStream(this.serverConnection.getInputStream());
            this.out = new DataOutputStream(this.serverConnection.getOutputStream());
            running = true;
            //sendQueue.clear();
            
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Socket sock = serverConnection;
                    try {
                        while(sock.isConnected() && running) {
                            start = System.currentTimeMillis();
                            while(sendQueue.size() != 0) {
                                Packet pkt = sendQueue.remove(0);
                                sendPacket(pkt);
                            }
                            long end = System.currentTimeMillis();
                            int time =(int) (end - start);
                            Thread.sleep(LOOP_SLEEP_TIME - (time > LOOP_SLEEP_TIME ? 0 : time));
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        sock.close();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Socket sock = serverConnection;
                    try {
                        while(sock.isConnected() && running) {
                            Packet pkt = readPacket();
                            proxy.fireServerPacket(Client.this, pkt);
                        }
                        
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        sock.close();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();            
        } catch (Exception ex) {
            ex.printStackTrace();
        }       
    }
    
    //Sends a packet to the server.
    private void sendPacket(Packet packet) throws IOException {
        PacketWriter w = new PacketWriter(100000);
        packet.write(w);
        byte[] data = w.getArray();
        byte[] encr = this.sendServer.cipher(data);
        
        if(encr == null) {
            encr = data;
        }
        PacketNode p = GameData.packets.byName(packet.type().toString());
        System.out.println("About to write packet: [" + p.name + "]");
        out.writeInt(encr.length + 5);        
        this.out.writeByte(p.id);
        this.out.write(encr);
        this.out.flush();
        
        /*
        if(data != null)
            System.out.println("Wrote packet: [" + p.name + ", length=" + data.length + ", total=" + (data.length + 5) + "]");
        else
            System.out.println("Wrote packet: [" + p.name + ", length=" + 0 + ", total=" + (0 + 5) + "]");
        */
    }
    
    //Reads incoming packets.
    private Packet readPacket() throws IOException {   
        //System.out.println("Buffer has bytes: [" + this.in.available() + "]");
        int size = in.readInt();
        byte packetId = in.readByte();
        //System.out.println("read packet id is: " + packetId + ", bytes to be read: " + size);
        
        PacketNode pNode = GameData.packets.byId(packetId);
        Packet packet = Packet.create(pNode.packetType);
        
        
        //The size value includes the size of data + size bytes + packet id byte.
        byte[] encryptedData = new byte[size - 5];
        in.readFully(encryptedData);
        byte[] decryptedData = this.receiveServer.cipher(encryptedData);       
        if(packet != null) {
            packet.read(new PacketReader(decryptedData));
        }    
       
        //System.out.println("Finished reading: " + pNode.name + ", id: " + pNode.id + ", bytes: " + (encryptedData.length + 5));
        return packet;
    }  
}
