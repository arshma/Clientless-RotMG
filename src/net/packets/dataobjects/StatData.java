package net.packets.dataobjects;

import java.io.IOException;
import net.packets.Packet;
import net.packets.PacketReader;
import net.packets.PacketWriter;

public class StatData extends DataObject {
    public int type;           //ubyte (aka id)
    public int intValue;
    public String stringValue;
    
    public boolean isStringData() {
        switch(this.type) {
            case StatsType.Name:
            case StatsType.GuildName:
            case StatsType.PetName:
            case StatsType.AccountId:
            case StatsType.OwnerAccountId:
                return true;
            default:
                return false;
        }
    }

    @Override
    public DataObject read(PacketReader r) throws IOException {
        this.type = r.readUnsignedByte();
        if(isStringData()) {
            this.stringValue = r.readUTF();
        } else {
            this.intValue = r.readInt();
        }
        return this;
    }

    @Override
    public void write(PacketWriter w) throws IOException {
        w.writeByte(this.type);
        if(isStringData()) {
            w.writeUTF(this.stringValue);
        } else {
            w.writeInt(this.intValue);
        }
    }   
    
    @Override
    public Object clone() throws CloneNotSupportedException {
        StatData sd = (StatData)super.clone();
        sd.stringValue = this.stringValue; 
        return sd;
    }
    
    @Override
    public String toString() {
        return "{Type=" + this.type + ",value=" + (isStringData()?stringValue : (new Integer(intValue)).toString()) + "}";
    }
    
    public static class StatsType {        
        public static final byte MaximumHp = 0;
        public static final byte HP = 1;
        public static final byte Size = 2;
        public static final byte MaximumMP = 3;
        public static final byte MP = 4;
        public static final byte NextLevelExperience = 5;
        public static final byte Experience = 6;
        public static final byte Level = 7;
        public static final byte Inventory0 = 8;
        public static final byte Inventory1 = 9;
        public static final byte Inventory2 = 10;
        public static final byte Inventory3 = 11;
        public static final byte Inventory4 = 12;
        public static final byte Inventory5 = 13;
        public static final byte Inventory6 = 14;
        public static final byte Inventory7 = 15;
        public static final byte Inventory8 = 16;
        public static final byte Inventory9 = 17;
        public static final byte Inventory10 = 18;
        public static final byte Inventory11 = 19;
        public static final byte Attack = 20;
        public static final byte Defense = 21;
        public static final byte Speed = 22;
        public static final byte Vitality = 26;
        public static final byte Wisdom = 27;
        public static final byte Dexterity = 28;
        public static final byte Effects = 29;
        public static final byte Stars = 30;
        public static final byte Name = 31;                 //Is UTF
        public static final byte Texture1 = 32;
        public static final byte Texture2 = 33;
        public static final byte MerchandiseType = 34;
        public static final byte Credits = 35;
        public static final byte MerchandisePrice = 36;
        public static final byte PortalUsable = 37;         //"ACTIVE_STAT"
        public static final byte AccountId = 38;            //Is UTF
        public static final byte AccountFame = 39;
        public static final byte MerchandiseCurrency = 40;
        public static final byte ObjectConnection = 41;
        
        public static final byte MerchandiseRemainingCount = 42;
        public static final byte MerchandiseRemainingMinutes = 43;
        public static final byte MerchandiseDiscount = 44;
        public static final byte MerchandiseRankRequirement = 45;
        public static final byte HealthBonus = 46;
        public static final byte ManaBonus = 47;
        public static final byte AttackBonus = 48;
        public static final byte DefenseBonus = 49;
        public static final byte SpeedBonus = 50;
        public static final byte VitalityBonus = 51;
        public static final byte WisdomBonus = 52;
        public static final byte DexterityBonus = 53;
        public static final byte OwnerAccountId = 54;       //Is UTF
        public static final byte RankRequired = 55;
        public static final byte NameChosen = 56;
        public static final byte CharacterFame = 57;
        public static final byte CharacterFameGoal = 58;
        public static final byte Glowing = 59;
        public static final byte SinkLevel = 60;
        public static final byte AltTextureIndex = 61;
        public static final byte GuildName = 62;            //Is UTF
        public static final byte GuildRank = 63;
        public static final byte OxygenBar = 64;
        public static final byte XpBoosterActive = 65;
        public static final byte XpBoostTime = 66;
        public static final byte LootDropBoostTime = 67;
        public static final byte LootTierBoostTime = 68;
        public static final byte HealthPotionCount = 69;
        public static final byte MagicPotionCount = 70;
        public static final byte Backpack0 = 71;
        public static final byte Backpack1 = 72;
        public static final byte Backpack2 = 73;
        public static final byte Backpack3 = 74;
        public static final byte Backpack4 = 75;
        public static final byte Backpack5 = 76;
        public static final byte Backpack6 = 77;
        public static final byte Backpack7 = 78;
        public static final byte HasBackpack = 79;
        public static final byte Skin = 80;
        public static final byte PetInstanceId = 81;
        public static final byte PetName = 82;              //Is UTF
        public static final byte PetType = 83;
        public static final byte PetRarity = 84;
        public static final byte PetMaximumLevel = 85;
        public static final byte PetFamily = 86;            //This does do nothing in the client
        public static final byte PetPoints0 = 87;
        public static final byte PetPoints1 = 88;
        public static final byte PetPoints2 = 89;
        public static final byte PetLevel0 = 90;
        public static final byte PetLevel1 = 91;
        public static final byte PetLevel2 = 92;
        public static final byte PetAbilityType0 = 93;
        public static final byte PetAbilityType1 = 94;
        public static final byte PetAbilityType2 = 95;
        public static final byte Effects2 = 96;             //Used for things like Curse, Petrify etc...
        public static final byte FortuneTokens = 97;
        public static final byte SupporterPoints = 98;
        public static final byte Supporter = 99;
    }    
}
