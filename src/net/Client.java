package net;

import crypto.RC4;
import gamedata.GameData;
import gamedata.structs.PacketNode;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
    private static String key0 = "6a39570cc9de4ec71d64821894";  //outgoing encoding.
    private static String key1 = "c79332b197f92ba85ed281a023";  //incoming decoding.
    public int lastUpdate = 0;
    public int previousTime = 0;
    private Object serverLock = new Object();
    private Object clientLock = new Object();
    private RC4 sendServer = new RC4(key0, RC4.ENCRYPT_MODE);
    private RC4 receiveServer = new RC4(key1, RC4.DECRYPT_MODE);
    private java.net.Socket serverConnection = null;
    private java.io.DataInputStream in;
    private java.io.DataOutputStream out;
    private boolean closed = false;
    private Proxy proxy;
    public List<Packet> sendQueue;
    
    public int clientStartTime = 0;
    public int myCharId;
    public int myObjectId;
    public Location position;
    public int previousTickTime;
    public int tickId;
    public int pingSerial;
    
    
    public int time() {
        return this.previousTime + ((int)System.currentTimeMillis() - this.lastUpdate);
    }
    
    public int objectId() {
        return 0;
    }
    
    public boolean connected() {
        return !this.closed;
    }
    
    public Client(Proxy proxy) {
        this.proxy = proxy;
        this.sendQueue = new ArrayList<Packet>(10);
        this.sendQueue = Collections.synchronizedList(this.sendQueue);
        //this.serverConnection = socket;
        //begin reading the packet.
    }
    
    public void connect() {
        String server = "ec2-13-57-254-131.us-west-1.compute.amazonaws.com";
        this.clientStartTime = (int)System.currentTimeMillis();
        
        try {
            this.serverConnection = new Socket();
            this.serverConnection.connect(new java.net.InetSocketAddress(server, 2050), 10000);
            //communication streams.
            this.in = new DataInputStream(this.serverConnection.getInputStream());
            this.out = new DataOutputStream(this.serverConnection.getOutputStream());
            
            //Poll list of packets to be sent.
            new Thread() {
                @Override
                public void run() {
                    Socket serverSock = Client.this.serverConnection;
                    try {
                        while(Client.this.connected() && serverSock.isConnected()) {
                            long start = System.currentTimeMillis();
                            while(Client.this.sendQueue.size() != 0) {
                                Packet p = sendQueue.remove(0);
                                Client.this.send(p);
                            }
                            long end = System.currentTimeMillis();
                            int time = (int)(end - start);
                            Thread.sleep(100 - (time > 100? 0 : time));
                        }
                    } catch(IOException e) {
                        e.printStackTrace();
                        System.exit(0);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }.start();
            
            //Set up a listeners that listener for an incoming data from the server.
            new Thread() {
                @Override
                public void run() {
                    Socket serverSock = Client.this.serverConnection;
                    try {
                        while(serverSock.isConnected() && Client.this.connected()) {
                            Packet packet = Client.this.read();
                            
                            //notify listeners in another thread.
                            new Thread() {
                                @Override
                                public void run() {
                                    Client.this.proxy.fireServerPacket(Client.this, packet);
                                }
                            }.start();
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                        System.exit(0);
                    }
                }
            }.start();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }       
    }
    
    //Sends a packet to the server.
    private void send(Packet packet) throws IOException {
        PacketWriter w = new PacketWriter();
        packet.write(w);
        byte[] data = w.getArray();
        
        /*
        //Test
        HelloPacket hp = (HelloPacket)Packet.create(Packet.PacketType.HELLO);
        PacketReader r = new PacketReader(data);
        hp.read(r);
        System.out.println("hp build: " + hp.buildVersion);
        System.out.println(hp.keyTime);
        System.out.println(hp.random1);
        System.out.println(hp.random2);
        System.out.println(hp.guid);
        System.out.println(hp.password);
        System.out.println(hp.key.length);
        System.out.println(hp.gameNet);
        System.out.println(hp.playPlatform);
        */
        
        data = this.sendServer.encrypt(data);
        
        this.out.writeInt((data.length+5));
        //System.out.println("p-size: " + (data.length+5));
        PacketNode p = GameData.packets.byName(packet.type().toString());
        this.out.writeByte(p.id);
        //System.out.println("p-id: " + p.id);
        this.out.write(data);
        this.out.flush();
        
        //System.out.println("Wrote packet: [" + p.name + ", length=" + data.length + ", total=" + (data.length + 5) + "]");
    }
    
    //Reads incoming packets.
    private Packet read() throws IOException {
        int size = this.in.readInt();
        byte packetId = this.in.readByte();
        
        //The size value includes the size of data + size bytes + packet id byte.
        byte[] encryptedData = new byte[size - 5];
        this.in.readFully(encryptedData);
        byte[] decryptedData = this.receiveServer.decrypt(encryptedData);
        
        PacketNode pNode = GameData.packets.byId(packetId);
        Packet packet = Packet.create(pNode.packetType);
        packet.read(new PacketReader(decryptedData));
        return packet;
    }    
}
