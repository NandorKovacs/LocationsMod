package net.roaringmind.locationsmod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.Level;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

public class Saver extends PersistentState {
  private Set<UUID> privatePlayers = new HashSet<>();
  private Map<UUID, Map<String, Position>> locations = new HashMap<>();

  public Saver() {
  }

  public Saver(Set<UUID> privatePlayers, Map<UUID, Map<String, Position>> locations) {
    this.privatePlayers = privatePlayers;
    this.locations = locations;
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
  public NbtCompound writeNbt(NbtCompound tag) {
    LocationsMod.log(Level.WARN, "totag fut");
    NbtCompound privates = new NbtCompound();
    privatePlayers.forEach(uuid -> {
      privates.putBoolean(uuid.toString(), true);
    });

    NbtCompound locationPlayers = new NbtCompound();
    locations.keySet().forEach(uuid -> {
      NbtCompound locNames = new NbtCompound();
      locations.get(uuid).entrySet().forEach(loc -> {
        locNames.put(loc.getKey(), loc.getValue().toTag(new NbtCompound()));
      });
      locationPlayers.put(uuid.toString(), locNames);
    });

    tag.put("locationPlayers", locationPlayers);
    tag.put("privatePlayers", privates);
    return tag;
  }
}
