package net.roaringmind.locationsmod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.Level;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

public class Saver extends PersistentState {
  private String kecs = "wohooo";
  private Set<UUID> privatePlayers = new HashSet<>();
  private Map<UUID, Map<String, Position>> locations = new HashMap<>();

  public Saver(String kex) {
    super(kex);
  }

  public String getString() {
    return kecs;
  }

  public void setString(String kecs) {
    this.kecs = kecs;
    markDirty();
  }

  public boolean getPlayerPublicity(UUID player) {
    return !privatePlayers.contains(player);
  }

  public void setPlayerPublicity(UUID player, boolean isPublic) {
    if (isPublic) {
      privatePlayers.remove(player);
    } else {
      privatePlayers.add(player);
    }
    markDirty();
  }

  public Position getLoc(UUID uuid, String locname) {
    if (!locations.containsKey(uuid) || !locations.get(uuid).containsKey(locname)) {
      return null;
    }
    return locations.get(uuid).get(locname);
  }

  public void setLoc(UUID uuid, String locname, BlockPos pos, DimensionEnum dim, boolean isPublic) {
    setLoc(uuid, locname, new Position(pos, dim, isPublic));
  }

  public void setLoc(UUID uuid, String locname, Position pos) {
    locations.computeIfAbsent(uuid, value -> {
      return new HashMap<>();
    });

    locations.get(uuid).put(locname, pos);
    markDirty();
  }

  public Map<String, Position> getAllLocs(UUID uuid) {
    if (!locations.containsKey(uuid)) {
      return new HashMap<>();
    }

    return locations.get(uuid);
  }

  public void removeLoc(UUID uuid, String locname) {
    locations.get(uuid).remove(locname);
    markDirty();
  }

  public void setLocPublicity(UUID uuid, String locname, boolean isPublic) {
    locations.get(uuid).get(locname).setPublic(isPublic);
  }

  @Override
  public void fromTag(CompoundTag tag) {
    LocationsMod.log(Level.WARN, "fromTag indul");
    CompoundTag privates = tag.getCompound("privatePlayers");
    privates.getKeys().forEach(stringUUID -> {
      privatePlayers.add(UUID.fromString(stringUUID));
    });

    CompoundTag locationPlayers = tag.getCompound("locationPlayers");
    locationPlayers.getKeys().forEach(stringUUID -> {
      locations.computeIfAbsent(UUID.fromString(stringUUID), value -> {
        return new HashMap<>();
      });

      CompoundTag locNames = locationPlayers.getCompound(stringUUID);
      locNames.getKeys().forEach(locname -> {
        Position pos = Position.fromTag(locNames.getCompound(locname));
        locations.get(UUID.fromString(stringUUID)).put(locname, pos);
      });
    });

    kecs = tag.getString("kecs2");
    LocationsMod.log(Level.WARN, "fromTag vége, kecs = " + kecs);
  }

  @Override
  public CompoundTag toTag(CompoundTag tag) {
    LocationsMod.log(Level.WARN, "totag fut");
    CompoundTag privates = new CompoundTag();
    privatePlayers.forEach(uuid -> {
      privates.putBoolean(uuid.toString(), true);
    });

    CompoundTag locationPlayers = new CompoundTag();
    locations.keySet().forEach(uuid -> {
      CompoundTag locNames = new CompoundTag();
      locations.get(uuid).entrySet().forEach(loc -> {
        locNames.put(loc.getKey(), loc.getValue().toTag(new CompoundTag()));
      });
      locationPlayers.put(uuid.toString(), locNames);
    });

    tag.put("locationPlayers", locationPlayers);
    tag.put("privatePlayers", privates);
    tag.putString("kecs2", kecs);
    return tag;
  }
}
