package listeners;

import gamedata.GameData;
import gamedata.structs.PacketNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.Client;
import net.packets.Packet;
import net.packets.Packet.*;
import net.packets.client.*;
import net.packets.dataobjects.Entity;
import net.packets.dataobjects.Location;
import net.packets.dataobjects.LocationRecord;
import net.packets.dataobjects.StatData;
import net.packets.dataobjects.Status;
import net.packets.dataobjects.VaultChest;
import net.packets.server.*;

//This class maintains lists of all the listerners that need to be informed upon a certain event.
public class Proxy {
    private ArrayList<PacketListener> serverPacketReceived;
    private Map<PacketType, ArrayList<PacketListener>> packetHooks;
    
    
    public Proxy() {
        this.packetHooks = new HashMap<PacketType, ArrayList<PacketListener>>(20);
        this.serverPacketReceived = new ArrayList<PacketListener>(5);
        
        this.defaultHooks();
        System.out.println("Number of different packets being hooked: " + this.packetHooks.size());
    }    

    
    //Registers listeners/callbacks for specified packet types.
    //Single packet type can be associated with multiple callback/listeners objects.
    public void hookPacket(PacketType pType, PacketListener listener) {
        //check invalid packet types.
        PacketNode pNode = GameData.packets.byName(pType.toString());
        if(pNode == null) {
            System.out.println("ERROR::Proxy.java: Packet type not supported for hooking.");
            return;
        }
            
        if(pNode.packetType == PacketType.UNKNOWN) {
            throw new java.lang.IllegalArgumentException();
        } else if(this.packetHooks.containsKey(pType)) {
            this.packetHooks.get(pType).add(listener);
        } else {
            ArrayList<PacketListener> list = new ArrayList<PacketListener>();
            list.add(listener);
            this.packetHooks.put(pType, list);
        }
    }
    
    //Notifys registered listeners/callbacks when a packet is received from the server.
    public void fireServerPacket(Client client, Packet packet) {
        //Notify listeners/callbacks when a server packet is received(regardless of type/hooks packets). --needs implementation        
        
        ArrayList<PacketListener> listeners = this.packetHooks.get(packet.type());
        if(listeners == null) {
            System.out.println("ERROR::proxy.java: Unable to initiate listeners for packet of type '" + packet.type().toString() + "'");
            return;
        }        
        for(PacketListener pl : listeners) {
            pl.onPacketReceived(client, packet);
        }
    }
    
    private void defaultHooks() {
        //Add default listeners
        this.hookPacket(PacketType.MAPINFO, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                LoadPacket loadPacket = (LoadPacket)Packet.create(PacketType.LOAD);
                loadPacket.characterId = client.charId;
                loadPacket.isFromArena = false;
                client.sendQueue.add(loadPacket);
                System.out.println("Connected to map: [" + ((MapInfoPacket)packet).name + "]");
            }
        });
        this.hookPacket(PacketType.CREATESUCCESS, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                CreateSuccessPacket csp = (CreateSuccessPacket)packet;
                client.objectId = csp.objectId;
                client.charId = csp.charId;
                client.loggedin = true;
            }
        });
        this.hookPacket(PacketType.UPDATE, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                UpdateAckPacket uap = (UpdateAckPacket)Packet.create(PacketType.UPDATEACK);
                client.sendQueue.add(uap);
                
                //Update client state data.
                UpdatePacket up = (UpdatePacket)packet;
                for(Entity ent : up.newObjs) {
                    if(ent.status.objectId == client.objectId) {
                        try {
                            client.position = (Location)ent.status.position.clone();
                            System.out.println("NOTICE::Proxy.java: Client location updated by 'Update' packet." + client.position);
                            if(client.moveToPos == null) {
                                client.moveToPos = (Location)ent.status.position.clone();
                            }
                            break;
                        } catch (CloneNotSupportedException e) {
                            System.out.println("ERROR::Proxy.java: Unable to set char position");
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        this.hookPacket(PacketType.NEWTICK, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                try {                    
                    NewTickPacket ntp = (NewTickPacket)packet;
                    client.lastTickId = ntp.tickId;
                    client.lastTickTime = ntp.tickTime; //time since last tick packet was sent (not very accurate).
                    client.currentTickTime = client.getTime();
                    for(Status s : ntp.statuses) {
                        if(client.objectId == s.objectId) {
                            client.position = (Location)s.position.clone();
                            System.out.println("NOTICE::Proxy.java: Client location updated by 'NewTick' packet." + client.position);
                            break;
                        }
                    }                   
                    //reply
                    MovePacket movePacket = (MovePacket)Packet.create(PacketType.MOVE);  
                    movePacket.tickId = client.lastTickId;
                    movePacket.time = client.getTime();
                    
                    //Determine new location to move to. Movement restricted by client spd.
                    if(client.position.isSameAs(client.moveToPos)) {
                        movePacket.newPosition = (Location)client.position.clone();
                    } else {
                        Location temp = (Location)client.moveToPos.clone();                        
                        float clientSpd = client.getClientSpeed();
                        //Finds correct x-coord
                        float coordDiff = temp.x - client.position.x;
                        if(Math.abs(coordDiff) > clientSpd) {
                            if(coordDiff < 0.0) {
                                temp.x = client.position.x - clientSpd;
                            } else {
                                temp.x = client.position.x + clientSpd;
                            }
                        }
                        coordDiff = temp.y - client.position.y;
                        if(Math.abs(coordDiff) > clientSpd) {
                            if(coordDiff < 0.0) {
                                temp.y = client.position.y - clientSpd;
                            } else {
                                temp.y = client.position.y + clientSpd;
                            }
                        }
                        movePacket.newPosition = temp;
                    }
                    
                    movePacket.records = new ArrayList<LocationRecord>();
                    client.sendQueue.add(movePacket);
                    
                    System.out.println("Replied to Tick [" + client.lastTickId + "] Time since last ticket: [" + client.lastTickTime + "]");
                } catch (CloneNotSupportedException e) {
                    System.out.println("ERROR::Proxy.java: Unable to set location. 'Move' packet not sent.");
                    e.printStackTrace();
                }
            }
        });
        this.hookPacket(PacketType.PING, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                PingPacket pingPacket = (PingPacket)packet;
                
                PongPacket pongPacket = (PongPacket)Packet.create(PacketType.PONG);                
                pongPacket.serial = pingPacket.serial;
                pongPacket.time = client.getTime();
                client.sendQueue.add(pongPacket);
            }
        });
        this.hookPacket(PacketType.FAILURE, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                FailurePacket fp = (FailurePacket)packet;
                client.errorId = fp.errorId;
                client.errorMsg = fp.errorMessage + ""; //creates a new string rather than referencing old one.
                //Logout upon failure
                //client.disconnect();
            }
        });
        this.hookPacket(PacketType.GOTO, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                GotoAckPacket gap = (GotoAckPacket)Packet.create(PacketType.GOTOACK);
                gap.time = client.getTime();
                client.sendQueue.add(gap);
                
                try {
                    //Update own character position.
                    GotoPacket gp = (GotoPacket)packet;
                    if(client.objectId == gp.objectId) {
                        client.position = (Location)gp.location.clone();
                        System.out.println("NOTICE::Proxy.java: Client location updated by 'Goto' packet." + client.position);
                    }
                } catch(java.lang.CloneNotSupportedException e) {
                    System.out.println("ERROR::Proxy.java: Unable to update client location by 'Goto Packet'.");
                    e.printStackTrace();
                }
            }
        });
        
        //Parses stat data about inventory, backpack, and vault chests; possibly new info.
        this.hookPacket(PacketType.UPDATE, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                UpdatePacket up = (UpdatePacket)packet; 
                for(Entity ent : up.newObjs) {
                    //Loads inventory and backpack items
                    if(ent.status.objectId == client.objectId) {
                        for(StatData sd : ent.status.data) {
                            //Ignore first 4 inv slots which refer to player equip slots in this context
                            if(sd.type >= StatData.StatsType.Inventory4 && sd.type <= StatData.StatsType.Inventory11) {
                                client.inv.add(sd.type - StatData.StatsType.Inventory4, sd.intValue);
                            } else if(sd.type >= StatData.StatsType.Backpack0 && sd.type <= StatData.StatsType.Backpack7) {
                                client.backpack.add(sd.type - StatData.StatsType.Backpack0, sd.intValue);
                            }
                        }
                        System.out.print("INV: [");
                        for(int i = 0; i < client.inv.size(); i++) {
                            System.out.print(GameData.items.byId(client.inv.get(i)).name + (i == client.inv.size()-1? "]\n" : ", "));
                        }
                        System.out.print("BACKPACK: [");
                        for(int i = 0; i < client.backpack.size(); i++) {
                            System.out.print(GameData.items.byId(client.backpack.get(i)).name + (i == client.backpack.size()-1? "]\n" : ", "));
                        }
                        client.itemListsUpdated = true;
                    }
                    //Loads vault chest data
                    else if(ent.objectType == 0x0504) {
                        System.out.println("Chest ID is: " + ent.status.objectId);
                        System.out.println("Items: ");
                        ArrayList<Integer> chestItems = new ArrayList<>(8);
                        for(StatData sd : ent.status.data) {
                            if(sd.type >= StatData.StatsType.Inventory0 && sd.type <= StatData.StatsType.Inventory7) {
                                System.out.println(sd.type + "\t" + GameData.items.byId(sd.intValue).name);
                                chestItems.add(sd.type - StatData.StatsType.Inventory0, sd.intValue);
                            }
                        }
                        VaultChest chest;
                        try {
                            chest = new VaultChest(ent.status.objectId, (Location)ent.status.position.clone(), chestItems);
                        } catch (CloneNotSupportedException e) {
                            chest = new VaultChest(ent.status.objectId, Location.empty(), chestItems);
                        }
                        client.vaultChests.put(ent.status.objectId, chest);
                        client.itemListsUpdated = true;
                        /*
                        System.out.println("CHEST ITEM ORDER: [");
                        for(int i = 0; i < chest.size(); i++) {
                            System.out.println("\t" + GameData.items.byId(chest.get(i)).name);
                        }
                        */
                        //System.out.println("Item added to the map");
                        //client.vaultChests.size();
                    }
                }
            }
        });
        
        //Parses stat data about inventory, backpack, and vault chests; updates old info.
        this.hookPacket(PacketType.NEWTICK, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                NewTickPacket ntp = (NewTickPacket)packet;
                for(Status s : ntp.statuses) {
                    //Update inventory and backpack items
                    if(s.objectId == client.objectId) {
                        for(StatData sd : s.data) {
                            //Ignore first 4 inventory slots which refer to player equip slots in this context
                            if(sd.type >= StatData.StatsType.Inventory4 && sd.type <= StatData.StatsType.Inventory11) {
                                client.inv.set(sd.type - StatData.StatsType.Inventory4, sd.intValue);
                            } else if(sd.type >= StatData.StatsType.Backpack0 && sd.type <= StatData.StatsType.Backpack7) {
                                client.backpack.set(sd.type - StatData.StatsType.Backpack0, sd.intValue);
                            }
                        }
                        System.out.print("INV: [");
                        for(int i = 0; i < client.inv.size(); i++) {
                            System.out.print(GameData.items.byId(client.inv.get(i)).name + (i == client.inv.size()-1? "]\n" : ", "));
                        }
                        System.out.print("BACKPACK: [");
                        for(int i = 0; i < client.backpack.size(); i++) {
                            System.out.print(GameData.items.byId(client.backpack.get(i)).name + (i == client.backpack.size()-1? "]\n" : ", "));
                        }
                        client.itemListsUpdated = true;
                    }
                    //Update vault chest data
                    else if(client.vaultChests.containsKey(s.objectId)) {
                        System.out.println("Chest ID is: " + s.objectId);
                        System.out.println("Chest pos: [" + s.position + "]");
                        System.out.println("Items: ");
                        VaultChest chest = client.vaultChests.get(s.objectId);
                        for(StatData sd : s.data) {
                            if(sd.type >= StatData.StatsType.Inventory0 && sd.type <= StatData.StatsType.Inventory7) {
                                System.out.println(sd.type + "\t" + GameData.items.byId(sd.intValue).name);
                                chest.items.set(sd.type - StatData.StatsType.Inventory0, sd.intValue);                                
                            }
                        }
                        client.vaultChests.put(s.objectId, chest);
                        client.itemListsUpdated = true;
                        
                        //move char to main chest
                        client.moveToPos.x = 44.5f;
                        client.moveToPos.y = 70.5f;
                        /*
                        System.out.println("CHEST ITEM ORDER: [");
                        for(int i = 0; i < chest.size(); i++) {
                            System.out.println("\t" + GameData.items.byId(chest.get(i)).name);
                        }
                        */
                        //System.out.println("Item added to the map");
                        //client.vaultChests.size();
                    }
                }
            }
        }); 
        
        this.hookPacket(PacketType.INVRESULT, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                InvResultPacket irp = (InvResultPacket)packet;
                System.out.println("InvSwap result: [" + irp.result + "]");
            }
        });
        
        //Accepts trade when other player accepts trade
        this.hookPacket(PacketType.TRADEACCEPTED, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                TradeAcceptedPacket tap = (TradeAcceptedPacket)packet;
                /*
                System.out.print("My offers: [");
                for(boolean b : tap.myOffers) {
                    System.out.print(b + ", ");
                }
                System.out.println();
                
                System.out.print("Partner's offers: [");
                for(boolean b : tap.partnerOffers) {
                    System.out.print(b + ", ");
                }
                System.out.println();   
                */
                AcceptTradePacket atp = new AcceptTradePacket();
                atp.myOffers = tap.myOffers;
                atp.yourOffers = tap.partnerOffers;
                client.sendQueue.add(atp);
            }
        });
        
        this.hookPacket(PacketType.TRADEDONE, new PacketListener() {
            @Override
            public void onPacketReceived(Client client, Packet packet) {
                //TradeDonePacket tdp = (TradeDonePacket)packet;
                //System.out.println(tdp.result + ", " + tdp.message);
            }
        });
    }
    
}
