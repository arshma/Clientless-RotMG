package net.packets.dataobjects;

import net.packets.dataobjects.StatData.StatsType;
import net.packets.server.MapInfoPacket;
import net.packets.server.NewTickPacket;
import net.packets.server.UpdatePacket;
import util.Constants.Classes;
import util.Constants.ConditionEffects;

public class PlayerData {
    public int ownerObjectId;
    public String mapName;
    public boolean teleportAllowed;
    public int mapWidth;
    public int mapHeight;

    public int maxHealth;
    public int health;
    public int maxMana;
    public int mana;
    public int xpGoal;
    public int xp;
    public int level = 1;
    public int[] slot = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };
    public int[] backPack = { -1, -1, -1, -1, -1, -1, -1, -1 };
    public int attack;
    public int defense;
    public int speed;
    public int vitality;
    public int wisdom;
    public int dexterity;
    public int effects;
    public int stars;
    public String name;
    public int realmGold;
    public int price;
    public boolean canEnterPortal;
    public String accountId;
    public int accountFame;
    public int healthBonus;
    public int manaBonus;
    public int attackBonus;
    public int defenseBonus;
    public int speedBonus;
    public int vitalityBonus;
    public int wisdomBonus;
    public int dexterityBonus;
    public int nameChangeRankRequired;
    public boolean nameChosen;
    public int characterFame;
    public int characterFameGoal;
    public boolean glowingEffect;
    public String guildName;
    public int guildRank;
    public int breath;
    public int healthPotionCount;
    public int magicPotionCount;
    public boolean hasBackpack;
    public int skin;
    public Location pos = new Location();
    // Custom
    public Classes charClass;
    
    
    public PlayerData(int ownerObjecdtId) {
        this.ownerObjectId = ownerObjectId;
        this.name = "";
    }
    
    public PlayerData(int ownerObjectId, MapInfoPacket mapInfo)
    {
        this.ownerObjectId = ownerObjectId;
        this.name = "";
        this.mapName = mapInfo.name;
        this.teleportAllowed = mapInfo.allowPlayerTeleport;
        this.mapWidth = mapInfo.width;
        this.mapHeight = mapInfo.height;
    }

    public void parse(UpdatePacket update)
    {
        for(Entity newObject : update.newObjs)
            if (newObject.status.objectId == this.ownerObjectId)
            {
                this.charClass = Classes.shortToEnum((short)newObject.objectType);
                for (StatData data : newObject.status.data)
                    parse(data.type, data.intValue, data.stringValue);
            }
    }

    public void parse(NewTickPacket newTick)
    {
        for(Status status : newTick.statuses)
            if (status.objectId == this.ownerObjectId)
                for(StatData data : status.data)
                {
                    this.pos = status.position;
                    parse(data.type, data.intValue, data.stringValue);
                }
    }

    public void parse(int id, int intValue, String stringValue)
    {
        if (id == StatsType.MaximumHp) this.maxHealth = intValue;
        else if (id == StatsType.HP) this.health = intValue;
        else if (id == StatsType.MaximumMP) this.maxMana = intValue;
        else if (id == StatsType.MP) this.mana = intValue;
        else if (id == StatsType.NextLevelExperience) this.xpGoal = intValue;
        else if (id == StatsType.Experience) this.xp = intValue;
        else if (id == StatsType.Level) this.level = intValue;
        else if (id == StatsType.Inventory0) this.slot[0] = intValue;
        else if (id == StatsType.Inventory1) this.slot[1] = intValue;
        else if (id == StatsType.Inventory2) this.slot[2] = intValue;
        else if (id == StatsType.Inventory3) this.slot[3] = intValue;
        else if (id == StatsType.Inventory4) this.slot[4] = intValue;
        else if (id == StatsType.Inventory5) this.slot[5] = intValue;
        else if (id == StatsType.Inventory6) this.slot[6] = intValue;
        else if (id == StatsType.Inventory7) this.slot[7] = intValue;
        else if (id == StatsType.Inventory8) this.slot[8] = intValue;
        else if (id == StatsType.Inventory9) this.slot[9] = intValue;
        else if (id == StatsType.Inventory10) this.slot[10] = intValue;
        else if (id == StatsType.Inventory11) this.slot[11] = intValue;
        else if (id == StatsType.Attack) this.attack = intValue;
        else if (id == StatsType.Defense) this.defense = intValue;
        else if (id == StatsType.Speed) this.speed = intValue;
        else if (id == StatsType.Vitality) this.vitality = intValue;
        else if (id == StatsType.Wisdom) this.wisdom = intValue;
        else if (id == StatsType.Dexterity) this.dexterity = intValue;
        else if (id == StatsType.Effects) this.effects = intValue;
        else if (id == StatsType.Stars) this.stars = intValue;
        else if (id == StatsType.Name) this.name = stringValue;
        else if (id == StatsType.Credits) this.realmGold = intValue;
        else if (id == StatsType.MerchandisePrice) this.price = intValue;
        //else if (id == 37) CanEnterPortal = bool.Parse(stringValue);
        else if (id == StatsType.AccountId) this.accountId = stringValue;
        else if (id == StatsType.AccountFame) this.accountFame = intValue; //fame you got when you died
        else if (id == StatsType.HealthBonus) this.healthBonus = intValue;
        else if (id == StatsType.ManaBonus) this.manaBonus = intValue;
        else if (id == StatsType.AttackBonus) this.attackBonus = intValue;
        else if (id == StatsType.DefenseBonus) this.defenseBonus = intValue;
        else if (id == StatsType.SpeedBonus) this.speedBonus = intValue;
        else if (id == StatsType.VitalityBonus) this.vitalityBonus = intValue;
        else if (id == StatsType.WisdomBonus) this.wisdomBonus = intValue;
        else if (id == StatsType.DexterityBonus) this.dexterityBonus = intValue;
        else if (id == StatsType.RankRequired) this.nameChangeRankRequired = intValue;
        else if (id == StatsType.NameChosen) this.nameChosen = intValue > 0;
        else if (id == StatsType.CharacterFame) this.characterFame = intValue; //fame on this character
        else if (id == StatsType.CharacterFameGoal) this.characterFameGoal = intValue;
        else if (id == StatsType.Glowing) this.glowingEffect = intValue > -1;
        else if (id == StatsType.GuildName) this.guildName = stringValue;
        else if (id == StatsType.GuildRank) this.guildRank = intValue;
        else if (id == StatsType.OxygenBar) this.breath = intValue;
        else if (id == StatsType.HealthPotionCount) this.healthPotionCount = intValue;
        else if (id == StatsType.MagicPotionCount) this.magicPotionCount = intValue;
        else if (id == StatsType.Backpack0) this.backPack[0] = intValue;
        else if (id == StatsType.Backpack1) this.backPack[1] = intValue;
        else if (id == StatsType.Backpack2) this.backPack[2] = intValue;
        else if (id == StatsType.Backpack3) this.backPack[3] = intValue;
        else if (id == StatsType.Backpack4) this.backPack[4] = intValue;
        else if (id == StatsType.Backpack5) this.backPack[5] = intValue;
        else if (id == StatsType.Backpack6) this.backPack[6] = intValue;
        else if (id == StatsType.Backpack7) this.backPack[7] = intValue;
        else if (id == StatsType.HasBackpack) this.hasBackpack = intValue > 0;
        else if (id == StatsType.Skin) this.skin = intValue;
    }

    public boolean hasConditionEffect(ConditionEffects effect)
    {
        return (this.effects & effect.getVal()) != 0;
    }

    public float tilesPerTick()
    {
        // Ticks per second = 5
        return (4.0f + 5.6f * (this.speed / 75.0f)) / 5.0f;
    }
    
    /*
    @Override
    public String toString()
    {
        // Use reflection to get the the non-null fields and arrange them into a table.
        FieldInfo[] fields = GetType().GetFields(BindingFlags.Public |
                                          BindingFlags.NonPublic |
                                          BindingFlags.Instance);

        StringBuilder s = new StringBuilder();
        s.Append(OwnerObjectId + "'s PlayerData Instance");
        foreach (FieldInfo f in fields)
            if (f.GetValue(this) != null)
                s.Append("\n\t" + f.Name + " => " + f.GetValue(this));
        return s.ToString();
    }
    */
}
