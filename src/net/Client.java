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
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import listeners.Proxy;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;
import net.packets.client.InvSwapPacket;
import net.packets.dataobjects.Location;
import net.packets.dataobjects.SlotObject;
import net.packets.dataobjects.VaultChest;
import util.Constants.GameId;

public class Client {
    private RC4 cipherSendServer;
    private RC4 cipherReceiveServer;
    private java.net.Socket serverConnection;
    private java.io.DataInputStream serverInputStream;
    private java.io.DataOutputStream serverOutputStream;
    
    private Proxy proxy;
    public List<Packet> sendQueue;
    private Thread sendThread;
    private Thread receiveThead;
    
    private boolean connected = false;
    public boolean loggedin = false;
    public int errorId = -1;
    public String errorMsg = "";
    private String email;
    private String password;
    public int charId = -1;
    public int objectId = -1;
    public Location position;
    public Location moveToPos;
    public int currentTickTime = 0;
    public int lastTickTime = 0;
    public int lastTickId = 0;
    public int connectTime = 0;
    public String connectedServerName = "UNKNOWN_SERVER_NAME";
    public String connectedServerIp = "";
    public ServerNode connectedServer;
    public int port = 2050;
    //public String server = "ec2-13-57-254-131.us-west-1.compute.amazonaws.com";
    //public String server = "13.57.254.131";
    public boolean itemListsUpdated = false;
    private Float clientSpeed = 0.5f;
    public Boolean hasBackpack = false;
    public int itemListLastUpdate = -1;
    
    public HashMap<Integer, VaultChest> vaultChests = new HashMap<>(10);
    public ArrayList<Integer> inv = new ArrayList<Integer>(8);
    public ArrayList<Integer> backpack = new ArrayList<Integer>(8);
    
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
            throw new java.lang.IllegalStateException("ERROR::Client: Failed to create Client.");
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
            this.connected = true;
            this.connectedServerIp = ip;
            if(this.serverConnection.isConnected())
                System.out.println("Connected to the server...");
            this.connectTime = (int)System.currentTimeMillis();
            
            //Create thread for sending.
            this.sendThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while(Client.this.connected && Client.this.serverConnection.isConnected()) {
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
                        //throw new java.lang.IllegalStateException("ERROR::Client: Unable to send packet in send thread.");
                    }
                }
            });
            
            //Receive packets.
            this.receiveThead = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while(Client.this.connected && Client.this.serverConnection.isConnected()) {
                            Packet pkt = Client.this.read();
                            Client.this.proxy.fireServerPacket(Client.this, pkt);
                        }
                    } catch(IOException e) {
                        System.out.println("ERROR::Client: Unable to receive packet in receive thead.");
                        e.printStackTrace();
                        //throw new java.lang.IllegalStateException("ERROR::Client: Unable to receive packet in receive thead.");
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
            this.connected = false;
            this.loggedin = false;
            this.sendThread.join();
            this.receiveThead.join();
            this.serverInputStream.close();
            this.serverOutputStream.close();
            this.serverConnection.close();
            //this.serverConnection = null;
            this.cipherSendServer.reset();
            this.cipherReceiveServer.reset();
            this.sendQueue.clear();
            this.connectedServerName = "UNKNOWN_SERVER_NAME";
            this.connectedServerIp = "";
            this.errorId = -1;
            this.errorMsg = "";
            //this.charId = -1;
            this.objectId = -1;
            this.currentTickTime = 0;
            this.lastTickTime = 0;
            this.lastTickId = 0;
            this.connectTime = 0;
            this.position = null;
            this.moveToPos = null;
            this.vaultChests.clear();       
            this.inv.clear();
            this.backpack.clear();
            System.out.println("NOTICE::Client: Client disconnected.");
        } catch(Exception e) {
            System.out.println("ERROR::Client: Failed to disconnect client.");
            e.printStackTrace();
            throw new java.lang.IllegalStateException("ERROR::Client: Failed to disconnect client.");
        }
    }
    
    //Note: acc.charId > 0 => use id specified in param account 
    //      acc.charId < 0 => load ids from server
    //      acc.charid == 0 => create new char
    public boolean login(Account acc, GameId gameId) throws Exception {
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
                    hp.gameId = gameId.getValue();
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
                    
                    //Remember who logged in.
                    Client.this.email = acc.guid;
                    Client.this.password = acc.password;
                } catch (Exception e) {
                    System.out.println("ERROR::Client: Unable to login.");
                    e.printStackTrace();
                    //throw new java.lang.IllegalStateException("ERROR::Client: Unable to login.");
                }
            }
        });
        loginTask.start();
        loginTask.join();
        return this.loggedin;
    }
    
    //Reconnects to the server and logs back in; reconnection only allowed logged in already.
    public boolean reconnect(GameId gameId) {
        Thread reconnectTask = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(!Client.this.connected || !Client.this.loggedin) {
                        return;
                    }
                    //disconnect from previous connection
                    Client.this.disconnect();
                    
                    //give server sufficient time to terminate connection
                    Thread.sleep(500);

                    //Reconnect to the same server
                    if(!Client.this.connect(Client.this.connectedServer)) {
                        throw new java.net.ConnectException();
                    }

                    //Reconnect to the same character but with @param gameid
                    if(!Client.this.login(new Account(Client.this.email, Client.this.password, Client.this.charId), gameId)) {
                        throw new Exception();
                    }
                } catch(java.net.ConnectException e) {
                    System.out.println("ERROR::Client: Unable to reconnect to the server.");
                    e.printStackTrace();
                    //throw new java.lang.IllegalStateException("ERROR::Client: Unable to reconnect to the server.");
                } catch (Exception e) {
                    System.out.println("ERROR::Client: Unable to relog into the same character.");
                    e.printStackTrace();
                    //throw new java.lang.IllegalStateException("ERROR::Client: Unable to relog into the same character.");
                }
            }            
        });
        
        reconnectTask.start();
        try {
            reconnectTask.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        return connected;
    }
    
    public float getClientSpeed() {
        synchronized(this.clientSpeed) {
            return this.clientSpeed;
        }
    }
    
    public void setClientSpeed(float newSpeed) {
        synchronized(this.clientSpeed) {
            this.clientSpeed = newSpeed;
        }
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
    
    public void moveToFreely(Location loc) {
        synchronized(this.moveToPos) {
            this.moveToPos = loc;
        }
    }
    
    //When inventory is empty, it contains "empty" (-1) flag values
    public boolean isInventoryEmpty() {
        if(this.inv.isEmpty()) {
            return false;
        }
        for(Integer i : this.inv) {
            if(i >  -1) {
                return false;
            }
        }
        return true;
    }
    
    public boolean isInventoryFull() {
        if(this.inv.isEmpty()) {
            return false;
        }
        for(Integer i : this.inv) {
            if(i == -1) {
                return false;
            }
        }
        return true;
    }
    
    //When backpack is empty, it  still contains "empty" (-1) flag values
    public boolean isBackpackEmpty() {
        if(this.backpack.isEmpty()) {
            return false;
        }
        for(Integer i : this.backpack) {
            if(i > -1) {
                return false;
            }
        }
        return true;
    }
    
    public boolean isBackpackFull() {
        if(this.backpack.isEmpty()) {
            return false;
        }
        for(Integer i : this.backpack) {
            if(i == -1) {
                return false;
            }
        }
        return true;
    }
    
    //Moves items from inventory to backpack, if there is a backpack and it's not full.
    //NOTE: Backpack slots range from {12, 19] or backpack0 + 12.
    public void moveItemsToBackpack() {
        if(!this.hasBackpack) {
            return;
        }
        if(this.isInventoryEmpty() || this.isBackpackFull()) {
            return;
        }
        
        Thread moveItems = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int i = 0;
                    int b = 0;
                    for(; (i < 8) && (b < 8); i++) {
                        int iItem = Client.this.inv.get(i);
                        if(iItem != -1) {
                            for(; b < 8; b++) {
                                int bItem = Client.this.backpack.get(b);
                                if(bItem == -1) {
                                    InvSwapPacket isp = new InvSwapPacket();
                                    isp.position = (Location)Client.this.position.clone();
                                    isp.time = Client.this.getTime();
                                    isp.slotObject1 = new SlotObject();
                                    isp.slotObject1.objectId = Client.this.objectId;
                                    isp.slotObject1.slotId = i+4;
                                    isp.slotObject1.objectType = iItem;
                                    isp.slotObject2 = new SlotObject();
                                    isp.slotObject2.objectId = Client.this.objectId;
                                    isp.slotObject2.slotId = b+12;
                                    isp.slotObject2.objectType = bItem;
                                    Client.this.sendQueue.add(isp);
                                    b++;
                                    System.out.println("From: " + isp.slotObject1);
                                    System.out.println("To: " + isp.slotObject2);
                                    
                                    Thread.sleep(650);                                    
                                    break;
                                }
                            }
                        }
                    }
                } catch(CloneNotSupportedException | InterruptedException e) {
                    System.out.println("ERROR::Client: Failed to move items from inventory to backpack.");
                    e.printStackTrace();
                }
            }
        });
        
        moveItems.start();
        try {
            moveItems.join();
        } catch (Exception e) {
            System.out.println("ERROR::Client: Failed to move items due to an interruption.");
        }
    }
}
