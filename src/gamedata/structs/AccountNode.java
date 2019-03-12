package gamedata.structs;

public class AccountNode extends DataNode<Integer> implements Comparable<AccountNode> {
    
    public AccountNode(String name, int id) {
        super(name, id);
    }
    
    @Override
    public String toString() {
        return "[" + this.name + ", " + this.id + "]";
    }

    @Override
    public int compareTo(AccountNode o) {
        return this.id - o.id;
    }
    
    public static java.util.HashMap<Integer, AccountNode> load(org.w3c.dom.Document doc) {
        java.util.HashMap<Integer, AccountNode> map = new java.util.HashMap<>(10);
        org.w3c.dom.NodeList cElements;
        
        cElements = doc.getElementsByTagName("Char");
        for(int i = 0; i < cElements.getLength(); i++) {
            String charId = cElements.item(i).getAttributes().getNamedItem("id").getNodeValue();
            map.put(Integer.parseInt(charId), new AccountNode(charId, Integer.parseInt(charId)));
        }        
        return map;
    }   
    
    public static java.util.ArrayList<Integer> allCharacterIdList(org.w3c.dom.Document doc) {
        java.util.ArrayList<Integer> idList = new java.util.ArrayList<>(10);
        org.w3c.dom.NodeList cElements;
        
        cElements = doc.getElementsByTagName("Char");
        for(int i = 0; i < cElements.getLength(); i++) {
            int charId = Integer.parseInt(cElements.item(i).getAttributes().getNamedItem("id").getNodeValue());
            idList.add(charId);
        }
        return idList;
    }
}
