package gamedata.structs;

public class ServerNode extends gamedata.structs.DataNode<String> implements java.lang.Comparable<ServerNode> {
    //public String abbr;     //abbreviated name of the packet will id the server nodes.
    public String ip;
    public static java.util.HashMap<String, String> abbreviations = new java.util.HashMap<>(25); 
    
    static {
        abbreviations.put("USWest", "USW");
        abbreviations.put("USMidWest", "USMW");
        abbreviations.put("EUWest", "EUW");
        abbreviations.put("USEast", "USE");
        abbreviations.put("AsiaSouthEast", "ASE");
        abbreviations.put("USSouth", "USS");
        abbreviations.put("USSouthWest", "USSW");
        abbreviations.put("EUEast", "EUE");
        abbreviations.put("EUSouthWest", "EUSW");
        abbreviations.put("USEast3", "USE3");
        abbreviations.put("USWest2", "USW2");
        abbreviations.put("USMidWest2", "USMW2");
        abbreviations.put("USEast2", "USE2");
        abbreviations.put("USNorthWest", "USNW");
        abbreviations.put("AsiaEast", "AE");
        abbreviations.put("USSouth3", "USS3");
        abbreviations.put("EUNorth", "EUN");
        abbreviations.put("EUNorth2", "EUN2");
        abbreviations.put("EUWest2", "EUW2");
        abbreviations.put("EUSouth", "EUS");
        abbreviations.put("USSouth2", "USS2");
        abbreviations.put("USWest3", "USW3");
        abbreviations.put("Australia", "AUS");
        
    }
    
    public ServerNode(String name, String ip) {
        super(name, ServerNode.abbreviations.get(name));
        this.ip = ip;
        //this.abbr = ServerNode.abbreviations.get(name);
    }
            
    @Override
    public String toString() {
        return "[" + this.name + ", " + this.id + ", " + this.ip + "]";
    }
    @Override
    public int compareTo(ServerNode s) {
        return this.name.compareTo(s.name);
    }
    
    
    public static java.util.HashMap<String, ServerNode> load(org.w3c.dom.Document doc) {
        java.util.HashMap<String, ServerNode> map = new java.util.HashMap<>(25);
        org.w3c.dom.NodeList sElements;
        
        sElements = doc.getElementsByTagName("Server");
        for(int i = 0; i < sElements.getLength(); i++) {
            String name = sElements.item(i).getChildNodes().item(1).getTextContent().trim();
            String ip = sElements.item(i).getChildNodes().item(3).getTextContent().trim();
            map.put(ServerNode.abbreviations.get(name), new ServerNode(name, ip));
        }
        return map;
    }
    
    public static java.util.ArrayList<ServerNode> allServersList(org.w3c.dom.Document doc) {
        return new java.util.ArrayList<ServerNode>(ServerNode.load(doc).values());
    }
    
    /*
    //Extract all server data from servers.xml.
    public static java.util.HashMap<String, ServerNode> allServersMap() {
        java.util.HashMap<String, ServerNode> map = new java.util.HashMap<>(25);
        javax.xml.parsers.DocumentBuilderFactory factory;
        javax.xml.parsers.DocumentBuilder builder;
        org.w3c.dom.Document doc;
        org.w3c.dom.NodeList sElements;
        
        try {           
            factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            //factory.setValidating(true);
            //factory.setIgnoringElementContentWhitespace(true);
            builder = factory.newDocumentBuilder();
            doc = builder.parse(new java.io.File("includes/res/servers.xml"));
            sElements = doc.getElementsByTagName("Server");
            
            for(int i = 0; i < sElements.getLength(); i++) {
                String name = sElements.item(i).getChildNodes().item(1).getTextContent().trim();
                String ip = sElements.item(i).getChildNodes().item(3).getTextContent().trim();                
                map.put(ServerNode.abbreviations.get(name), new ServerNode(name, ip));
            }           
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            e.printStackTrace();
        } catch (org.xml.sax.SAXException e) {
            e.printStackTrace();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } finally {
        }
        return map;
    }
    //Extract all packet data from packet.xml.
    public static java.util.ArrayList<ServerNode> allServersList() {
        return new java.util.ArrayList<ServerNode>(ServerNode.allServersMap().values());
    }
    */
}
