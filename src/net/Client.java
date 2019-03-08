package net;

import crypto.RC4;
import gamedata.GameData;
import gamedata.structs.PacketNode;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import listeners.Proxy;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;
import net.packets.dataobjects.Location;

public class Client {
    private static final String key0 = "6a39570cc9de4ec71d64821894";  //outgoing encoding.
    private static final String key1 = "c79332b197f92ba85ed281a023";  //incoming decoding.
    private RC4 cipherSendServer;
    private RC4 cipherReceiveServer;
    private java.net.Socket serverConnection;
    private java.io.DataInputStream serverInputStream;
    private java.io.DataOutputStream serverOutputStream;
    
    private Proxy proxy;
    public List<Packet> sendQueue;
    private Thread sendThread;
    private Thread receiveThead;
    
    private boolean running = false;
    public boolean loggedin = false;
    public int errorId = -1;
    public String errorMsg = "";
    public int charId = 85;
    public int objectId;
    public Location position;// = new Location();
    public int currentTickTime = 0;
    public int lastTickTime = 0;
    public int lastTickId = 0;
    public int connectTime = 0;
    public String connectedServerName = "USWest";
    public String connectedServerIp = "13.57.254.131";
    
    //public String server = "ec2-13-57-254-131.us-west-1.compute.amazonaws.com";
    //public String server = "13.57.254.131";
    public int port = 2050;
    
    public Client(Proxy proxy) {
        try {
            this.cipherSendServer = new RC4(key0);
            this.cipherReceiveServer = new RC4(key1);
            this.proxy = proxy;
            this.sendQueue = new ArrayList<Packet>(40);
            this.connectTime = (int)System.currentTimeMillis();
        } catch (Exception e) {
            System.out.println("ERROR::Client2.java: Failed to create Client.");
            e.printStackTrace();
            System.exit(-2);
        }
    }
    
    public boolean connect(String server) {
        try {
            //this.serverConnection = new Socket(java.net.InetAddress.getByName(server), this.port);
            this.serverConnection = new Socket();
            this.serverConnection.connect(new java.net.InetSocketAddress(java.net.Inet4Address.getByName(server), 2050));
            this.serverInputStream = new DataInputStream(this.serverConnection.getInputStream());
            this.serverOutputStream = new DataOutputStream(this.serverConnection.getOutputStream());
            this.running = true;
            
            if(this.serverConnection.isConnected())
                System.out.println("Connected to the server...");
            //Create thread for sending.
            this.sendThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while(Client.this.running && Client.this.serverConnection.isConnected()) {
                            long start = System.currentTimeMillis();
                            while(Client.this.sendQueue.size() != 0) {
                                Packet pkt = Client.this.sendQueue.remove(0);
                                Client.this.send(pkt);
                            }
                            long end = System.currentTimeMillis();
                            int time = (int)(end - start);
                            Thread.sleep(100 - (time > 100? 0 : time));
                        }
                    } catch(Exception e) {
                        System.out.println("ERROR::Client: Unable to send packet in send thread.");
                        e.printStackTrace();
                    }
                }
            });
            
            //Receive packets.
            this.receiveThead = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while(Client.this.running && Client.this.serverConnection.isConnected()) {
                            Packet pkt = Client.this.read();
                            Client.this.proxy.fireServerPacket(Client.this, pkt);
                        }
                    } catch(IOException e) {
                        System.out.println("ERROR::Client: Unable to receive packet in receive thead.");
                        //Client.this.disconnect();
                    }                    
                }
            });
            this.sendThread.start();
            this.receiveThead.start();           
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public void disconnect() {
        try {
            this.running = false;
            this.loggedin = false;
            this.sendThread.join();
            this.receiveThead.join();
            this.serverInputStream.close();
            this.serverOutputStream.close();
            this.serverConnection.close();
            this.serverConnection = null;
            this.cipherSendServer.reset();
            this.cipherReceiveServer.reset();
            this.sendQueue.clear();
            this.connectedServerName = "";
            this.connectedServerIp = "";
            this.errorId = -1;
            this.errorMsg = "";
            System.out.println("NOTICE::Client.java: Client disconnected.");
        } catch(Exception e) {
            System.out.println("ERROR::Client: Failed to disconnect client.");
            e.printStackTrace();
        }
    }
    
    public boolean isLoggedIn() {
        return this.loggedin;
    }
    
    public int getTime() {
        return (((int)System.currentTimeMillis()) - this.connectTime);
    }
    
    public int getObjectId() {
        return this.objectId;
    }
    
    public boolean isConnected() {
        return running;
    }
    
    public Packet read() throws IOException {
        int packetSize = this.serverInputStream.readInt();
        byte packetId = this.serverInputStream.readByte();
        
        PacketNode pNode = GameData.packets.byId(packetId);
        Packet packet = Packet.create(pNode.packetType);        
        
        byte[] encryptedData = new byte[packetSize - 5];
        this.serverInputStream.readFully(encryptedData);
        
        //NOTE: The java cipher lib returns 'null' when byte[] is of size 0
        byte[] decryptedData = this.cipherReceiveServer.cipher(encryptedData);
        if(decryptedData == null) {
            decryptedData = encryptedData;
        }
        
        if(packet != null) {
            packet.read(new PacketReader(decryptedData));
        } else {
            System.out.println("ERROR::Client: Unable to create packet for storing data.");
        }
        System.out.println("Finished reading: " + pNode.name + ", id: " + pNode.id + ", bytes: " + (encryptedData.length + 5));
        return packet; 
    }
    
    public void send(Packet packet) throws IOException {
        PacketWriter w = new PacketWriter(50000);
        packet.write(w);
        
        byte[] data = w.getArray(); //This will always return an array even when no data to write.
        //NOTE: The java cipher lib returns 'null' when byte[] is of size 0
        byte[] encryptedData = this.cipherSendServer.cipher(data); 
        if(encryptedData == null) {
            encryptedData = data;
        }
        PacketNode pNode = GameData.packets.byName(packet.type().toString());
        if(pNode == null) {
            System.out.println("ERROR::Client: Unable to get 'PacketNode' by packet name.");
        }
        
        this.serverOutputStream.writeInt(encryptedData.length + 5);
        this.serverOutputStream.writeByte(pNode.id);
        this.serverOutputStream.write(encryptedData);
        this.serverOutputStream.flush();
        System.out.println("Sent packet name: " + pNode.name + ", id: " + pNode.id + ", bytes: " + (encryptedData.length+5));
    }
}
