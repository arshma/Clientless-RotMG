package net;

import crypto.RC4;
import gamedata.GameData;
import gamedata.structs.Account;
import gamedata.structs.PacketNode;
import gamedata.structs.ServerNode;
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
    //private static final String key0 = "6a39570cc9de4ec71d64821894";  //outgoing encoding.
    //private static final String key1 = "c79332b197f92ba85ed281a023";  //incoming decoding.
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
    public int charId = -1;
    public int objectId = -1;
    public Location position;// = new Location();
    public int currentTickTime = 0;
    public int lastTickTime = 0;
    public int lastTickId = 0;
    public int connectTime = 0;
    public String connectedServerName = "UNKNOWN_SERVER_NAME";
    public String connectedServerIp = "";
    public int port = 2050;
    public ServerNode connectedServer;
    //public String server = "ec2-13-57-254-131.us-west-1.compute.amazonaws.com";
    //public String server = "13.57.254.131";
    
    public Client(Proxy proxy) {
        try {
            this.cipherSendServer = new RC4(GameData.keyOut);
            this.cipherReceiveServer = new RC4(GameData.keyIn);
            this.proxy = proxy;
            this.sendQueue = new ArrayList<Packet>(40);
            //this.connectTime = (int)System.currentTimeMillis();
        } catch (Exception e) {
            System.out.println("ERROR::Client: Failed to create Client.");
            e.printStackTrace();
        }
    }
    
    public boolean connect(ServerNode sNode) {
        if(sNode == null)
            return false;
        this.connectedServer = sNode;
        this.connectedServerName = sNode.name;
        return this.connect(sNode.ip);
    }
    
    public boolean connect(String ip) {
        try {
            //this.serverConnection = new Socket(java.net.InetAddress.getByName(server), this.port);
            this.serverConnection = new Socket();
            this.serverConnection.connect(new java.net.InetSocketAddress(java.net.Inet4Address.getByName(ip), 2050));
            this.serverInputStream = new DataInputStream(this.serverConnection.getInputStream());
            this.serverOutputStream = new DataOutputStream(this.serverConnection.getOutputStream());
            this.running = true;
            this.connectedServerIp = ip;
            if(this.serverConnection.isConnected())
                System.out.println("Connected to the server...");
            this.connectTime = (int)System.currentTimeMillis();
            
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
                        e.printStackTrace();
                    }                    
                }
            });
            
            this.sendThread.start();
            this.receiveThead.start();           
        } catch(Exception e) {
            System.out.println("ERROR:Client: Unable to connect to the server.");
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
            this.charId = -1;
            this.objectId = -1;
            this.currentTickTime = 0;
            this.lastTickTime = 0;
            this.lastTickId = 0;
            this.connectTime = 0;
            this.position = null;
            System.out.println("NOTICE::Client: Client disconnected.");
        } catch(Exception e) {
            System.out.println("ERROR::Client: Failed to disconnect client.");
            e.printStackTrace();
        }
    }
    
    //Note: acc.charId > 0 => use id specified in param account 
    //      acc.charId < 0 => load ids from server
    //      acc.charid == 0 => create new char
    public boolean login(Account acc) throws Exception {
        Thread loginTask = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(acc.charId < 0) {
                        String path = "" + acc.guid + ".acc";
                        java.io.File accFile = new java.io.File(path);

                        if(accFile.exists()) {
                            GameData.charIds = new ArrayList<Integer>(10);
                            java.io.DataInputStream in = new java.io.DataInputStream(new java.io.BufferedInputStream(new java.io.FileInputStream(path)));
                            while(in.available() > 0) {
                                GameData.charIds.add(in.readInt());
                            }
                            in.close();
                        } else {
                            GameData.loadCharIds(acc);
                            java.io.DataOutputStream out = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(path)));
                            for(int i : GameData.charIds) {
                                out.writeInt(i);
                            }
                            out.close();
                        }
                        if(GameData.charIds.size() < 1) {
                            throw new Exception();
                        }
                        //Client will login first char on list.
                        Client.this.charId = GameData.charIds.get(0);
                    } else if(acc.charId > 0) {
                        Client.this.charId = acc.charId;
                    }
                    
                    net.packets.client.HelloPacket hp = new net.packets.client.HelloPacket();
                    hp.buildVersion = GameData.gameVersion;
                    hp.gameId = -2;
                    hp.guid = new crypto.RSA().encrypt(acc.guid);
                    hp.random1 = (int)Math.floor(Math.random()*1000000000);
                    hp.password = new crypto.RSA().encrypt(acc.password);
                    hp.random2 = (int)Math.floor(Math.random()*1000000000);
                    hp.secret = "";
                    hp.keyTime = -1;
                    hp.key = new byte[0];
                    hp.mapJson = "";
                    hp.entryTag = "";
                    hp.gameNet = "rotmg";
                    hp.gameNetUserId = "";
                    hp.playPlatform = "rotmg";
                    hp.platformToken = "";
                    hp.userToken = "";

                    Client.this.sendQueue.add(hp);

                    int time = 0;
                    while(!Client.this.loggedin) {
                        if(time > 10000) {
                            return; //throw new Exception("Email or password is invalid.");
                        }
                        time += 200;
                        Thread.sleep(200);
                    }            
                } catch (Exception e) {
                    System.out.println("ERROR::Client: Unable to login.");
                    e.printStackTrace();
                }
            }
        });
        loginTask.start();
        loginTask.join();
        return this.loggedin;
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
