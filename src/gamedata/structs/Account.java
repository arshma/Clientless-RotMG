package gamedata.structs;

public class Account {
    public String guid;
    public String password;
    public int charId = 0;      //0 => create new char; default setting on real client as well.
    public int nextCharId = 1;  //1 => Initially "next" IDs being at 1.
    
    public Account(String guid, String password, int charId, int nextCharId) {
        this.guid = guid;
        this.password = password;
        this.charId = charId;
        this.nextCharId = nextCharId;
    }
    
    public Account(String guid, String password) {
        this.guid = guid;
        this.password = password;
    }
}
