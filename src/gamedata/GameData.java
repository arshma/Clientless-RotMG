package gamedata;

import gamedata.structs.Account;
import gamedata.structs.AccountNode;
import gamedata.structs.ItemNode;
import gamedata.structs.PacketNode;
import gamedata.structs.ServerNode;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Scanner;
import javax.xml.parsers.ParserConfigurationException;
import net.packets.Packet;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class GameData {
    public static org.w3c.dom.Document rawPacketsXML;
    public static org.w3c.dom.Document rawObjectsXML;
    //public static org.w3c.dom.Document rawTilesXML;
    public static org.w3c.dom.Document rawServersXML;
    //public static org.w3c.dom.Document rawCharListXML; //Outdated; there is no need to store this XML
    
    public static GameDataMap<Byte, PacketNode> packets;   //maps packet id to packet node
    public static GameDataMap<String, ServerNode> servers; //maps server name to server node
    public static GameDataMap<Integer, ItemNode> items;    //maps item id to item node
    
    //IMPORTANT: Must always be checked to see if it is initialized.
    public static ArrayList<Integer> charIds;
    
    public static String gameVersion = "X0.0.0";
    public static String keyOut;
    public static String keyIn;
    
    //Store XML documents in memory since accessors are slow.
    static{
        try {
            System.out.println("NOTICE::GameData: Loading xml data...");
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            Reader inputFileCharStream;           
            
            //Explicitly load the xml data as UTF-8; prevents parsing errors.
            inputFileCharStream = new InputStreamReader(new BufferedInputStream(new FileInputStream("res/packets.xml")), "UTF-8");
            rawPacketsXML = builder.parse(new InputSource(inputFileCharStream));            
            inputFileCharStream = new InputStreamReader(new BufferedInputStream(new FileInputStream("res/servers.xml")), "UTF-8");
            rawServersXML = builder.parse(new InputSource(inputFileCharStream));            
            inputFileCharStream = new InputStreamReader(new BufferedInputStream(new FileInputStream("res/objects.xml")), "UTF-8");
            rawObjectsXML = builder.parse(new InputSource(inputFileCharStream));
            
            //Load game version
            Scanner in = new Scanner(new java.io.File("res/gameVersion.txt"));
            GameData.gameVersion = in.nextLine();
            System.out.println("Loaded game version: [" + GameData.gameVersion + "]");
            in.close();
            
            //Load RC4 keys
            in = new Scanner(new java.io.File("res/keys.txt"));
            GameData.keyOut = in.nextLine();
            GameData.keyIn = in.nextLine();
            System.out.println("Read keys(out, in): " + GameData.keyOut + ", " + GameData.keyIn);
            in.close();
            
        } catch (ParserConfigurationException | SAXException | IOException e) {
            System.out.println("ERROR::GameData: Failed to load static XML data...");
            //e.printStackTrace();
            throw new java.lang.IllegalStateException("Failed to load game XML data");
        }        
    }
    
    //Initialize static game data from XMLs.
    public static void load() {        
        java.util.concurrent.ForkJoinPool pool = new java.util.concurrent.ForkJoinPool();
        pool.invoke(new java.util.concurrent.RecursiveAction() {
           @Override
           protected void compute() {
               GameData.packets = new GameDataMap<Byte, PacketNode>(PacketNode.load(rawPacketsXML));
               //Custom packet represents unknown packet types.
               packets.map.put((byte)255, new PacketNode(Packet.PacketType.UNKNOWN.toString(), 255, Packet.PacketType.UNKNOWN));
               System.out.println("Notice::GameData: " + GameData.packets.size() + " Packets loaded.");
               rawPacketsXML = null;
           }
        });
        
        pool.invoke(new java.util.concurrent.RecursiveAction() {
            @Override
            protected void compute() {
                GameData.servers = new GameDataMap<String, ServerNode>(ServerNode.load(rawServersXML));
                System.out.println("Notice::GameData: " + GameData.servers.size() + " Servers loaded.");
                rawServersXML = null;
            }
        });
        
        //Load items
        pool.invoke(new java.util.concurrent.RecursiveAction() {
            @Override
            protected void compute() {
                GameData.items = new GameDataMap<Integer, ItemNode>(ItemNode.load(rawObjectsXML));               
                //Custom item to indicate empty inv.
                GameData.items.map.put(-1, new ItemNode("EMPTY", -1));                
                System.out.println("Notice::GameData: " + GameData.items.size() + " Items loaded.");
                rawObjectsXML = null;
            }
        });
        
        pool.shutdown();
    }
    
    public static void loadCharIds(Account account) {
        try {
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            String url = "http://www.realmofthemadgod.com/char/list?guid=" + 
                         account.guid.replaceAll("\\+", "%2B") + "&password=" + account.password + "&version=31";
            //GameData.rawCharListXML = builder.parse(new java.io.BufferedInputStream(new java.net.URL(url).openStream()));
            GameData.charIds = AccountNode.allCharacterIdList(builder.parse(new java.io.BufferedInputStream(new java.net.URL(url).openStream())));
            
            if(GameData.charIds.isEmpty()) {
                throw new java.lang.IllegalStateException();
            } else {
                System.out.print("Printing character id list: [");
                for(int i = 0; i < GameData.charIds.size(); i++) {
                    System.out.print(GameData.charIds.get(i) + (i==(GameData.charIds.size()-1)? "]\n" : ", "));
                }  
            }
                   
        } catch (ParserConfigurationException | IOException | SAXException | java.lang.IllegalStateException e) {
            System.out.println("ERROR::GameData: Failed to load character IDs...");
            //e.printStackTrace();
            throw new java.lang.IllegalStateException("ERROR::GameData: Failed to load character IDs...");
        }
    }
    
    
    //Wraps Maps to allow for addition of new properties.
    public static class GameDataMap<IDType, NodeType extends gamedata.structs.DataNode<IDType>> {
        public java.util.Map<IDType, NodeType> map;
        
        public GameDataMap(java.util.Map<IDType, NodeType> map) {
            this.map = map;
        }
        
        public NodeType byId(IDType id) {
            return this.map.get(id);
        }
        
        public NodeType byName(String name) {
            for(NodeType p : this.map.values()) {
                if(name.matches(p.name())) {
                    return p;
                }
            }
            return null;
        }
        
        public int size() {
            return map.size();
        }
    }
    
}
