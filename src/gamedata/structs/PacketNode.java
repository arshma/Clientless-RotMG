package gamedata.structs;

public class PacketNode extends DataNode<java.lang.Byte> implements java.lang.Comparable<PacketNode> {
    public net.packets.Packet.PacketType packetType;
    
    public PacketNode(String name, int id, net.packets.Packet.PacketType packetType) {
        super(name, (byte)id);
        this.packetType = packetType;
    }
    
    public String type() {
        return this.packetType.toString();
    }    
    @Override
    public String toString() {
        return "[" + this.name + ", " + this.id + ", " + this.packetType.toString() + "]";
    }
    @Override
    public int compareTo(PacketNode p) {
        return this.id - p.id;
    }
    
    public static java.util.HashMap<Byte, PacketNode> load(org.w3c.dom.Document doc) {
        java.util.HashMap<Byte, PacketNode> map = new java.util.HashMap<>(100);
        org.w3c.dom.NodeList pElements;
        
        pElements = doc.getElementsByTagName("Packet");
        for(int i = 0; i < pElements.getLength(); i++) {
            String name = pElements.item(i).getChildNodes().item(1).getTextContent().trim();
            Byte id = Byte.parseByte(pElements.item(i).getChildNodes().item(3).getTextContent().trim());
            net.packets.Packet.PacketType packetType = null;
            //Determine type of packet.
            for(net.packets.Packet.PacketType pType : net.packets.Packet.PacketType.values()) {
                if(name.matches(pType.name())) {
                    packetType = pType;
                }
            }
            map.put(id, new PacketNode(name, id, packetType));
        }
        return map;
    }
    
    public static java.util.ArrayList<PacketNode> allPacketsList(org.w3c.dom.Document doc) {
        return new java.util.ArrayList<PacketNode>(PacketNode.load(doc).values());
    }
    
    /*
    //Extract all packet data from packets.xml.
    public static java.util.HashMap<Byte, PacketNode> allPacketsMap() {
        java.util.HashMap<Byte, PacketNode> map = new java.util.HashMap<>(100);
        javax.xml.parsers.DocumentBuilderFactory factory;
        javax.xml.parsers.DocumentBuilder builder;
        org.w3c.dom.Document doc;
        org.w3c.dom.NodeList pElements;
        
        try {
            factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            //factory.setValidating(true);
            //factory.setIgnoringElementContentWhitespace(true);
            builder = factory.newDocumentBuilder();
            doc = builder.parse(new java.io.File("includes/res/packets.xml"));
            pElements = doc.getElementsByTagName("Packet");
            
            for(int i = 0; i < pElements.getLength(); i++) {
                String name = pElements.item(i).getChildNodes().item(1).getTextContent().trim();
                Byte id = Byte.parseByte(pElements.item(i).getChildNodes().item(3).getTextContent().trim());
                net.packets.Packet.PacketType packetType = null;
                //Determine type of packet.
                for(net.packets.Packet.PacketType pType : net.packets.Packet.PacketType.values()) {
                    if(name.matches(pType.name())) {
                        packetType = pType;
                    }
                }
                map.put(id, new PacketNode(name, id, packetType));
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
    //Extract all packet data from packets.xml.
    public static java.util.ArrayList<PacketNode> allPacketsList() {
        return new java.util.ArrayList<PacketNode>(allPacketsMap().values());
        
        java.util.ArrayList<PacketNode> list = new java.util.ArrayList<>();
        javax.xml.parsers.DocumentBuilderFactory factory;
        javax.xml.parsers.DocumentBuilder builder;
        org.w3c.dom.Document doc;
        org.w3c.dom.NodeList pElements;
        
        try {           
            factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            //factory.setValidating(true);
            //factory.setIgnoringElementContentWhitespace(true);
            builder = factory.newDocumentBuilder();
            doc = builder.parse(new java.io.File("includes/res/packets.xml"));
            pElements = doc.getElementsByTagName("Packet");
            
            for(int i = 0; i < pElements.getLength(); i++) {
                String name = pElements.item(i).getChildNodes().item(1).getTextContent().trim();
                Byte id = Byte.parseByte(pElements.item(i).getChildNodes().item(3).getTextContent().trim());
                net.packets.Packet.PacketType packetType = null;
                //Determine type of packet.
                for(net.packets.Packet.PacketType pType : net.packets.Packet.PacketType.values()) {
                    if(name.matches(pType.name())) {
                        packetType = pType;
                    }
                }
                list.add(new PacketNode(name, id, packetType));
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
        return list;
        
    }
    */
}
