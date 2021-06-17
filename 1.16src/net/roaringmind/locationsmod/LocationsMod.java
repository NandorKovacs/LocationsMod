package net.roaringmind.locationsmod;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class LocationsMod implements ModInitializer {

  private static final Logger LOGGER = LogManager.getLogger();
  private Saver saver;

  public static final String MOD_ID = "locationsmod";
  public static final String MOD_NAME = "LocationsMod";
  @Override
  public void onInitialize() {
    log(Level.INFO, "Initializing");
    registerCommands();

    ServerLifecycleEvents.SERVER_STARTED.register(server -> {
      saver = server.getWorld(World.OVERWORLD).getPersistentStateManager().getOrCreate(() -> new Saver(MOD_ID), MOD_ID);

    });
  }

  //@formatter:off
  private void registerCommands() {
    CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
      dispatcher.register(literal("loc")
        .then(literal("get")
          .then(argument("player", EntityArgumentType.player())
            .then(argument("locname", StringArgumentType.string())
              .executes(ctx -> {
                MutableText message = get(ctx.getSource().getPlayer().getUuid(), EntityArgumentType.getPlayer(ctx, "player").getUuid(), StringArgumentType.getString(ctx, "locname"));
                sendPlayerMessage(ctx.getSource().getPlayer().getUuid(), message, ctx.getSource().getWorld(), false, true);
                return 0;
              })
            )
          )
          .then(argument("locname", StringArgumentType.string())
            .executes(ctx -> {
              MutableText message = get(ctx.getSource().getPlayer().getUuid(), ctx.getSource().getPlayer().getUuid(), StringArgumentType.getString(ctx, "locname"));
              sendPlayerMessage(ctx.getSource().getPlayer().getUuid(), message, ctx.getSource().getWorld(), false, true);
              return 0;
            })
          )
        )
        .then(literal("player")
          .then(literal("get")
            .then(argument("player", EntityArgumentType.player())
              .executes(ctx -> {
                MutableText message = playerGet(ctx.getSource().getPlayer().getUuid(), EntityArgumentType.getPlayer(ctx, "player").getUuid(), ctx.getSource().getWorld());
                sendPlayerMessage(ctx.getSource().getPlayer().getUuid(), message, ctx.getSource().getWorld(), false, true);
                return 0;
              })
            )
            .executes(ctx -> {
              MutableText message = playerGet(ctx.getSource().getPlayer().getUuid(), ctx.getSource().getPlayer().getUuid(), ctx.getSource().getWorld());
              sendPlayerMessage(ctx.getSource().getPlayer().getUuid(), message, ctx.getSource().getWorld(), false, true);
              return 0;
            })
          )
          .then(literal("publicity")
            .then(argument("public", BoolArgumentType.bool())
              .executes(ctx -> {
                MutableText message = playerPublicity(ctx.getSource().getPlayer().getUuid(), BoolArgumentType.getBool(ctx, "public"));
                sendPlayerMessage(ctx.getSource().getPlayer().getUuid(), message, ctx.getSource().getWorld(), false, true);
                return 0;
              })
            )
            .executes(ctx -> {
              MutableText message = playerPublicity(ctx.getSource().getPlayer().getUuid());
              sendPlayerMessage(ctx.getSource().getPlayer().getUuid(), message, ctx.getSource().getWorld(), false, true);
              return 0;
            })
          )
        )
        .then(literal("set")
          .then(argument("coords", BlockPosArgumentType.blockPos())
            .then(argument("locname", StringArgumentType.string())
              .then(argument("public", BoolArgumentType.bool())
                .executes(ctx -> {
                  MutableText message = set(ctx.getSource().getPlayer().getUuid(), BlockPosArgumentType.getBlockPos(ctx, "coords"), StringArgumentType.getString(ctx, "locname"), BoolArgumentType.getBool(ctx, "public"), getDimension(ctx.getSource().getWorld().getDimension(), ctx.getSource().getMinecraftServer()));
                  sendPlayerMessage(ctx.getSource().getPlayer().getUuid(), message, ctx.getSource().getWorld(), false, true);
                  return 0;
                })
              )
              .executes(ctx -> {
                MutableText message = set(ctx.getSource().getPlayer().getUuid(), BlockPosArgumentType.getBlockPos(ctx, "coords"), StringArgumentType.getString(ctx, "locname"), true, getDimension(ctx.getSource().getWorld().getDimension(), ctx.getSource().getMinecraftServer()));
                sendPlayerMessage(ctx.getSource().getPlayer().getUuid(), message, ctx.getSource().getWorld(), false, true);
                return 0;
              })
            )
          )
        )
        .then(literal("help")
          .then(literal("get")
            .executes(ctx -> {
              MutableText message = help(HelpEnum.GET);
              sendPlayerMessage(ctx.getSource().getPlayer().getUuid(), message, ctx.getSource().getWorld(), false, true);
              return 0;
            })
          )
          .then(literal("set")
            .executes(ctx -> {
              MutableText message = help(HelpEnum.SET);
              sendPlayerMessage(ctx.getSource().getPlayer().getUuid(), message, ctx.getSource().getWorld(), false, true);
              return 0;
            })
          )
          .then(literal("list")
            .executes(ctx -> {
              MutableText message = help(HelpEnum.LIST);
              sendPlayerMessage(ctx.getSource().getPlayer().getUuid(), message, ctx.getSource().getWorld(), false, true);
              return 0;
            })
          )
          .then(literal("player")
            .executes(ctx -> {
              MutableText message = help(HelpEnum.PLAYER);
              sendPlayerMessage(ctx.getSource().getPlayer().getUuid(), message, ctx.getSource().getWorld(), false, true);
              return 0;
            })
          )
          .then(literal("manage")
            .executes(ctx -> {
              MutableText message = help(HelpEnum.MANAGE);
              sendPlayerMessage(ctx.getSource().getPlayer().getUuid(), message, ctx.getSource().getWorld(), false, true);
              return 0;
            })
          )
          .then(literal("help")
            .executes(ctx -> {
              MutableText message = help(HelpEnum.HELP);
              sendPlayerMessage(ctx.getSource().getPlayer().getUuid(), message, ctx.getSource().getWorld(), false, true);
              return 0;
            })
          )
          .executes(ctx -> {
              MutableText message = help(HelpEnum.ALL);
              sendPlayerMessage(ctx.getSource().getPlayer().getUuid(), message, ctx.getSource().getWorld(), false, true);
            return 0;
          })
        )
        .then(literal("list")
          .executes(ctx -> {
            MutableText message = list(ctx.getSource().getPlayer().getUuid(), ctx.getSource().getPlayer().getUuid(), ctx.getSource().getName());
            sendPlayerMessage(ctx.getSource().getPlayer().getUuid(), message, ctx.getSource().getWorld(), false, false);
            return 0;
          })
          .then(argument("player", EntityArgumentType.player())
            .executes(ctx -> {
              MutableText message = list(ctx.getSource().getPlayer().getUuid(), EntityArgumentType.getPlayer(ctx, "player").getUuid(), EntityArgumentType.getPlayer(ctx, "player").getName().asString());
              sendPlayerMessage(ctx.getSource().getPlayer().getUuid(), message, ctx.getSource().getWorld(), false, false);
              return 0;
            })
          )
        )
        .then(literal("manage")
          .then(literal("publicity")
            .then(argument("locname", StringArgumentType.string())
              .then(argument("public", BoolArgumentType.bool())
                .executes(ctx -> {
                  MutableText message = managePublicity(ctx.getSource().getPlayer().getUuid(), StringArgumentType.getString(ctx, "locname"), BoolArgumentType.getBool(ctx, "public"));
                  sendPlayerMessage(ctx.getSource().getPlayer().getUuid(), message, ctx.getSource().getWorld(), false, true);
                  return 0;
                })
              )
              .executes(ctx -> {
                MutableText message = managePublicity(ctx.getSource().getPlayer().getUuid(), StringArgumentType.getString(ctx, "locname"));
                sendPlayerMessage(ctx.getSource().getPlayer().getUuid(), message, ctx.getSource().getWorld(), false, true);
                return 0;
              })
            )
          )
          .then(literal("rename")
            .then(argument("locname", StringArgumentType.string())
              .then(argument("newname", StringArgumentType.string())
                .executes(ctx -> {
                  MutableText message = manageRename(ctx.getSource().getPlayer().getUuid(), StringArgumentType.getString(ctx, "locname"), StringArgumentType.getString(ctx, "newname"));
                  sendPlayerMessage(ctx.getSource().getPlayer().getUuid(), message, ctx.getSource().getWorld(), false, true);
                  return 0;
                })
              )
            )
          )
          .then(literal("delete")
            .then(argument("locname", StringArgumentType.string())
              .executes(ctx -> {
                MutableText message = manageDelete(ctx.getSource().getPlayer().getUuid(), StringArgumentType.getString(ctx, "locname"));
                sendPlayerMessage(ctx.getSource().getPlayer().getUuid(), message, ctx.getSource().getWorld(), false, true);
                return 0;
              })
            )
          )
        )
      );
    });
  }
  //@formatter:on

  String couldntFindLoc = "Couldn't find the specified location, either it doesnt exist, or it is private";

  private MutableText get(UUID source, UUID target, String locname) {
    Position pos = saver.getLoc(target, locname);

    if (pos == null || (pos.getPublic() && source != target)) {
      return createDefaultMutable(couldntFindLoc);
    }

    return pos.toMutableText(locname);
  }

  String playerPrivate = "The specified player has probably set his coordinates private";

  private MutableText playerGet(UUID source, UUID target, World world) {
    if (!saver.getPlayerPublicity(target) && source != target) {
      return createDefaultMutable(playerPrivate);
    }

    return createDefaultMutable(blockPosToString(world.getPlayerByUuid(target).getBlockPos()));
  }

  String playerPrivacyDidntChange = "Your coordinates are already %s, no changes applied";
  String playerPrivacyConfirmChange = "Your coordinates are now %s";

  private MutableText playerPublicity(UUID source, boolean isPublic) {
    String privateStr;
    if (isPublic) {
      privateStr = "public";
    } else {
      privateStr = "private";
    }

    if (saver.getPlayerPublicity(source) == isPublic) {
      return createDefaultMutable(String.format(playerPrivacyDidntChange, privateStr));
    }

    saver.setPlayerPublicity(source, isPublic);

    return createDefaultMutable(String.format(playerPrivacyConfirmChange, privateStr));
  }

  String playerPrivacyGet = "Your coordinates are currently %s";

  private MutableText playerPublicity(UUID source) {
    if (!saver.getPlayerPublicity(source)) {
      return createDefaultMutable(String.format(playerPrivacyGet, "private"));
    }
    return createDefaultMutable(String.format(playerPrivacyGet, "public"));
  }

  String locAlreadyExists = "A location with that name is already registered under your name. Try a different name, or rename the existing one with /loc manage rename";
  String locCreateConfirm = "Created location \"%s\" with the coordinates %s";

  private MutableText set(UUID source, BlockPos coords, String locname, boolean isPublic, DimensionEnum dim) {

    if (saver.getLoc(source, locname) != null) {
      return createDefaultMutable(locAlreadyExists);
    }

    saver.setLoc(source, locname, coords, dim, isPublic);

    return createDefaultMutable(String.format(locCreateConfirm, locname, blockPosToString(coords)));
  }

  String couldntFindPublicLocations = "Couldn't find any public locations registered by the specified player";
  String listHead = "Locations saved by %s that are accessible to you: ";

  private MutableText list(UUID source, UUID target, String targetName) {
    List<MutableText> res = new ArrayList<>();
    res.add(createDefaultMutable(String.format(listHead, targetName)).setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE)));
    for (Map.Entry<String, Position> entry : saver.getAllLocs(target).entrySet()) {
      if (target != source && !entry.getValue().getPublic()) {
        continue;
      }
      res.add(entry.getValue().toMutableText(entry.getKey()));
    }

    if (res.size() <= 1) {
      return createDefaultMutable(couldntFindPublicLocations);
    }

    return joinMutable(res, "\n      ");
  }

  String couldntFindLocSelf = "Couldn't find specified location";
  String locPrivacyDidntChange = "\"%s\" is already %s, no changes applied";
  String locPrivacyConfirmChange = "\"%s\" is now %s";

  private MutableText managePublicity(UUID source, String locname, boolean isPublic) {
    Position pos = saver.getLoc(source, locname);
    if (pos == null) {
      return createDefaultMutable(couldntFindLocSelf);
    }

    String privateStr;
    if (isPublic) {
      privateStr = "public";
    } else {
      privateStr = "private";
    }

    if (pos.getPublic() == isPublic) {
      return createDefaultMutable(String.format(locPrivacyDidntChange, locname, privateStr));
    }

    pos.setPublic(isPublic);
    saver.setLoc(source, locname, pos);
    return createDefaultMutable(String.format(locPrivacyConfirmChange, locname, privateStr));
  }

  String locPrivacyGet = "\"%s\" is %s";

  private MutableText managePublicity(UUID source, String locname) {
    Position pos = saver.getLoc(source, locname);

    if (pos == null) {
      return createDefaultMutable(couldntFindLocSelf);
    }

    String privateStr;
    if (pos.getPublic()) {
      privateStr = "public";
    } else {
      privateStr = "private";
    }

    return createDefaultMutable(String.format(locPrivacyGet, locname, privateStr));
  }

  String locRenameConfirm = "Successfully renamed \"%s\" to \"%s\"";

  private MutableText manageRename(UUID source, String locname, String newname) {
    Position pos = saver.getLoc(source, locname);
    if (pos == null) {
      return createDefaultMutable(couldntFindLocSelf);
    }

    saver.setLoc(source, newname, pos);
    saver.removeLoc(source, locname);
    return createDefaultMutable(String.format(locRenameConfirm, locname, newname));
  }

  String successfullDelete = "Successfully deleted location \"%s\"";
  private MutableText manageDelete(UUID source, String locname) {
    if (saver.getLoc(source, locname) == null) {
      return createDefaultMutable(couldntFindLocSelf);
    }

    saver.removeLoc(source, locname);

    return createDefaultMutable(String.format(successfullDelete, locname));
  }

  private MutableText help(HelpEnum type) {
    return createDefaultMutable("Look on CurseForge, i am too lazy (will be done in a few days)");
  }

  private void sendPlayerMessage(UUID uuid, MutableText message, ServerWorld world, boolean toolBar,
      boolean hasPrefix) {
    LiteralText prefix = new LiteralText("[LocationsMod] ");
    prefix.setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE));

    if (!hasPrefix) {
      world.getPlayerByUuid(uuid).sendMessage(message, toolBar);
      return;
    }

    world.getPlayerByUuid(uuid).sendMessage(prefix.append(message), toolBar);
  }

  public static MutableText joinMutable(List<MutableText> mutables, String delim) {
    MutableText delimMutable = new LiteralText(delim);
    MutableText res = new LiteralText("");
    boolean firstRound = true;

    for (MutableText mt : mutables) {
      if (firstRound) {
        firstRound = false;
        res = mt;
        continue;
      }

      res = res.append(delimMutable).append(mt);
    }

    return res;
  }

  private MutableText createDefaultMutable(String s) {
    return new LiteralText(s).setStyle(Style.EMPTY.withColor(Formatting.WHITE));
  }

  public static String blockPosToString(BlockPos pos) {
    return "" + pos.getX() + " " + pos.getY() + " " + pos.getZ();
  }

  private DimensionEnum getDimension(DimensionType dimtype, MinecraftServer mcserver) {
    DimensionType end = mcserver.getRegistryManager().getDimensionTypes().get(DimensionType.THE_END_ID);
    DimensionType overworld = mcserver.getRegistryManager().getDimensionTypes().get(DimensionType.OVERWORLD_ID);
    DimensionType nether = mcserver.getRegistryManager().getDimensionTypes().get(DimensionType.THE_NETHER_ID);

    if (dimtype.equals(end)) {
      return DimensionEnum.END;
    } else if (dimtype.equals(overworld)) {
      return DimensionEnum.OVERWOLRD;
    } else if (dimtype.equals(nether)) {
      return DimensionEnum.NETHER;
    } else {
      return DimensionEnum.NODIM;
    }
  }

  public static void log(Level level, String message) {
    LOGGER.log(level, "[" + MOD_NAME + "] " + message);
  }

}
