package gamedata.structs;

import java.util.HashMap;

public class ItemNode extends DataNode<Integer> implements Comparable<ItemNode> {
    //Id is ushort
    
    //public ProjectileNode projectile;
    //public int numProjectiles;
    public int tier;
    public short slotType;          //ubyte
    public float rateOfFire;
    public long feedPower;          //uint
    public short bagType;           //ubyte
    public short mpCost;            //ubyte
    public short fameBonus;         //ubyte
    public boolean soulbound;
    public boolean usable;
    public boolean consumable;
        
    public ItemNode(String name, int id) {
        super(name, id);
    }
    
    @Override
    public int compareTo(ItemNode o) {
        return this.id - o.id;
    }
    
    public static HashMap<Integer, ItemNode> load(org.w3c.dom.Document doc) {
        java.util.HashMap<Integer, ItemNode> map = new HashMap<>(2500);
        org.w3c.dom.NodeList objElements;
        
        //Get a list of "item" objects and then extract info from that item's parent.
        objElements = doc.getElementsByTagName("Item");
        //System.out.println("Items found: " + objElements.getLength());
        for(int i = 0; i < objElements.getLength(); i++) {
            org.w3c.dom.Node parent = objElements.item(i).getParentNode();
            int id = Integer.decode(parent.getAttributes().getNamedItem("type").getNodeValue());
            String name = parent.getAttributes().getNamedItem("id").getNodeValue();
            map.put(id, new ItemNode(name, id));
            //System.out.println("Item: [" + id + ", " + name + "]");
        }
        return map;
    }
}
