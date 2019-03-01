package gamedata.structs;

public abstract class DataNode<IDType> {
    public String name;
    public IDType id;
    
    public DataNode(String name, IDType id) {
        this.name = name;
        this.id = id;
    }
    public String name() {
        return this.name;
    }
    public IDType id() {
        return this.id;
    }
}
