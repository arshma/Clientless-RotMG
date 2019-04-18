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
    private static final java.util.logging.Logger logger = util.Logger.getLogger(Client.class.getSimpleName());
    private RC4 cipherSendServer;
    private RC4 cipherReceiveServer;
    private java.net.Socket serverConnection;
    private java.io.DataInputStream serverInputStream;
    private java.io.DataOutputStream serverOutputStream;
    
    private Proxy proxy;
    private Thread sendThread;
    private Thread receiveThead;
    public List<Packet> sendQueue;
    
    private boolean connected = false;
    public boolean loggedin = false;
    public int errorId = -1;
    public String errorMsg = "";
    private String email;
    private String password;
    public int charId = -1;
    public int objectId = -1;
    public String accountName;
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
    
    public boolean itemListsUpdated = false;
    public boolean isReconnecting = false;
    private Float clientSpeed = 0.8f;
    public Boolean hasBackpack = false;
    public int vaultDataLastUpdated = -1;
    
    public HashMap<Integer, VaultChest> vaultChests = new HashMap<>(10);
    public ArrayList<Integer> inv = new ArrayList<Integer>(8);
    public ArrayList<Integer> backpack = new ArrayList<Integer>(8);
    
    public Client(Proxy proxy) {
        try {
            this.cipherSendServer = new RC4(GameData.keyOut);
            this.cipherReceiveServer = new RC4(GameData.keyIn);
            this.proxy = proxy;
            this.sendQueue = new ArrayList<Packet>(40);
        } catch (Exception e) {
            Client.logger.log(Level.SEVERE, e, () -> "Failed to create client");
            throw new java.lang.IllegalStateException("Failed to create Client.");
        }
    }
    
    //Establish a connection to the server
    public boolean connect(ServerNode sNode) {
        if(sNode == null)
            return false;
        this.connectedServer = sNode;
        this.connectedServerName = sNode.name;
        return this.connect(sNode.ip);
    }
    
    //Establish a connecdtion to the server
    public boolean connect(String ip) {
        try {
            this.serverConnection = new Socket();
            this.serverConnection.connect(new java.net.InetSocketAddress(java.net.Inet4Address.getByName(ip), this.port));
            this.serverInputStream = new DataInputStream(this.serverConnection.getInputStream());
            this.serverOutputStream = new DataOutputStream(this.serverConnection.getOutputStream());
            this.connected = true;
            this.connectedServerIp = ip;
            if(this.serverConnection.isConnected())
                Client.logger.log(Level.FINE, () -> "Client connected to server...");
            this.connectTime = (int)System.currentTimeMillis();
            
            //Sends packets
            this.sendThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while(Client.this.connected && Client.this.serverConnection.isConnected()) {
                            long start = System.currentTimeMillis();
                            while(!Client.this.sendQueue.isEmpty()) {
                                Packet pkt = Client.this.sendQueue.remove(0);
                                Client.this.send(pkt);
                            }
                            long end = System.currentTimeMillis();
                            int time = (int)(end - start);
                            Thread.sleep(100 - (time > 100? 0 : time));
                        }
                    } catch(IOException | InterruptedException e) {
                        Client.logger.log(Level.WARNING, e, () -> "Unable to send packet in send thread.");
                        Client.this.disconnect();
                    }
                }
            });
            
            //Receives packets
            this.receiveThead = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while(Client.this.connected && Client.this.serverConnection.isConnected()) {
                            Packet pkt = Client.this.read();
                            Client.this.proxy.fireServerPacket(Client.this, pkt);
                        }
                    } catch(IOException e) {
                        Client.logger.log(Level.WARNING, e, () -> "Unable to receive packet in receive thead.");
                        Client.this.disconnect();
                    }                    
                }
            });
            
            this.sendThread.start();
            this.receiveThead.start();           
        } catch(IOException e) {
            Client.logger.log(Level.WARNING, e, () -> "Unable to connect to the server.");
            return false;
        }
        return true;
    }
    
    public void disconnect() {        
        Thread disconThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if(Client.this.connected) {
                    try {
                        Client.logger.log(Level.INFO, () -> "Client disconnect started.");
                        Client.this.connected = false;
                        Client.this.proxy.fireClientDisconnect(Client.this);
                        Client.this.loggedin = false;
                        //Client.this.sendThread.join();
                        //Client.this.receiveThead.join();
                        Client.this.serverInputStream.close();
                        Client.this.serverOutputStream.close();
                        Client.this.serverConnection.close();
                        Client.this.cipherSendServer.reset();
                        Client.this.cipherReceiveServer.reset();
                        Client.this.sendQueue.clear();
                        Client.this.connectedServerName = "UNKNOWN_SERVER_NAME";
                        Client.this.connectedServerIp = "";
                        Client.this.errorId = -1;
                        Client.this.errorMsg = "";
                        //Client.this.charId = -1;
                        Client.this.objectId = -1;
                        Client.this.accountName = null;
                        Client.this.currentTickTime = 0;
                        Client.this.lastTickTime = 0;
                        Client.this.lastTickId = 0;
                        Client.this.connectTime = 0;
                        Client.this.position = null;
                        Client.this.moveToPos = null;
                        Client.this.vaultChests.clear();       
                        Client.this.inv.clear();
                        Client.this.backpack.clear();
                        //Client.this.sendQueue.clear();
                        Client.logger.log(Level.INFO, () -> "Client disconnected.");
                        Client.this.proxy.fireClientDisconnected(Client.this);
                    } catch(Exception e) {
                        Client.logger.log(Level.SEVERE, e, () -> "Failed to disconnect client.");
                        throw new java.lang.IllegalStateException("Failed to disconnect client.");
                    }
                }
            }
        });   
        disconThread.start();
        try {
            disconThread.join();
        } catch(InterruptedException e) {
            Client.logger.log(Level.WARNING, e, () -> "Disconnect method failed.");
        }
    }
    
    //Note: acc.charId > 0 => use id specified in param account 
    //      acc.charId < 0 => load ids from server
    //      acc.charid == 0 => create new char (not implemented; possible future implementation)
    public boolean login(Account acc, GameId gameId) {
        Thread loginTask = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(acc.charId < 0) {
                        String path = "res/temp/" + acc.guid + ".acc";
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
                        
                        //Client will login first char on list.
                        Client.this.charId = GameData.charIds.get(0);
                    } else if(acc.charId > 0) {
                        Client.this.charId = acc.charId;
                    }
                    
                    Client.this.sendQueue.clear();
                    //notify listeners of beginning of connection
                    Client.this.proxy.fireClientBeginConnect(Client.this);
                    
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
                        if(time > 10000 || (!Client.this.connected)) {
                            Client.logger.log(Level.INFO, () -> "Terminating login. Clearing send queue");
                            Client.this.sendQueue.clear();
                            return;
                        }
                        time += 200;
                        Thread.sleep(200);
                    }
                    
                    //Remember who is logged in.
                    Client.this.email = acc.guid;
                    Client.this.password = acc.password;
                } catch (Exception e) {
                    Client.logger.log(Level.WARNING, e, () -> "Failed to login.");
                }
            }
        });
        loginTask.start();
        try {
            loginTask.join();
        } catch (InterruptedException e) {
            Client.logger.log(Level.WARNING, e, () -> "Login method join failed");
        }
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
                    Client.logger.log(Level.INFO, () -> "Setting reconnect flag.");
                    Client.this.isReconnecting = true;
                    
                    //disconnect from previous connection
                    Client.this.disconnect();
                    
                    /*
                    //NOTE: Disconnect is blocking, thus, there is no need to wait for connection to terminate.
                    //give server sufficient time to terminate connection fully
                    int time = 0;
                    while(Client.this.connected || Client.this.loggedin) {
                        if(time > 3000) {
                            return;
                        }
                        time += 500;
                        Thread.sleep(500);
                    }
                    */
                    
                    //Reconnect to the same server
                    if(!Client.this.connect(Client.this.connectedServer)) {
                        return;
                    }
                    
                    //Notify listeners of attempt to reconnect
                    Client.this.proxy.fireClientReconnect(Client.this);

                    //Reconnect to the same character but with @param gameid
                    if(!Client.this.login(new Account(Client.this.email, Client.this.password, Client.this.charId), gameId)) {
                        Client.logger.log(Level.INFO, ()->"Reconnect login failed. Clearing the send queue.");
                        Client.this.disconnect();
                        Client.this.sendQueue.clear();
                        return;
                    }                    
                } catch (Exception e) {
                    Client.logger.log(Level.WARNING, e, () -> "Failed to reconnect to the server");
                    Client.this.disconnect();
                } finally {
                    Client.logger.log(Level.INFO, () -> "Turning off reconnect flag.");
                    Client.this.isReconnecting = false;
                }
            }            
        });
        
        reconnectTask.start();
        try {
            reconnectTask.join();
        } catch (InterruptedException e) {
            Client.logger.log(Level.WARNING, e, () -> "Reconnect method failed");
        }
        return this.connected && this.loggedin;        
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
            Client.logger.log(Level.INFO, () -> "Finished reading: " + pNode.name + ", id: " + pNode.id + ", bytes: " + (encryptedData.length + 5));
        } else {
            Client.logger.log(Level.WARNING, () -> "Unable to create packet for storing data.");
        }        
        
        return packet; 
    }
    
    public void send(Packet packet) throws IOException {
        PacketWriter w = new PacketWriter(50000);
        packet.write(w);
        
        byte[] data = w.getArray(); //This will always return an array even when no data to write(array of size 0).
        //NOTE: The java cipher lib returns 'null' when byte[] is of size 0
        byte[] encryptedData = this.cipherSendServer.cipher(data); 
        if(encryptedData == null) {
            encryptedData = data;
        }
        PacketNode pNode = GameData.packets.byName(packet.type().toString());
        if(pNode == null) {
            Client.logger.log(Level.SEVERE, () -> "Unable to get 'PacketNode' by packet name. Failed to send packet.");
            throw new java.lang.NullPointerException("Failed to send a packet"); 
        }
        
        this.serverOutputStream.writeInt(encryptedData.length + 5);
        this.serverOutputStream.writeByte(pNode.id);
        this.serverOutputStream.write(encryptedData);
        this.serverOutputStream.flush();
        Client.logger.log(Level.FINE, () -> "Sent packet name: " + pNode.name + ", id: " + pNode.id + ", bytes: " + (data.length + 5));
    }
    
    public void moveToFreely(Location loc) {
        synchronized(this) {
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
    //NOTE: Backpack slots range from {12, 19] or backpack + 12.
    public int moveItemsToBackpack() {
        if(!this.hasBackpack) {
            return 0;
        }
        if(this.isInventoryEmpty() || this.isBackpackFull()) {
            return 0;
        }
        
        final int[] numItemsMoved = {0};
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
                                    numItemsMoved[0]++;
                                    //System.out.println("From: " + isp.slotObject1);
                                    //System.out.println("To: " + isp.slotObject2);
                                    
                                    Thread.sleep(650);                                    
                                    break;
                                }
                            }
                        }
                    }
                } catch(CloneNotSupportedException | InterruptedException e) {
                    Client.logger.log(Level.WARNING, e, () -> "Failed to move items from inventory to backpack");
                }
            }
        });
     
        moveItems.start();
        try {
            moveItems.join();
        } catch (InterruptedException e) {
            Client.logger.log(Level.WARNING, e, () -> "MoveItemsBackToBackpack method failed");
        }
        return numItemsMoved[0];
    }
}
