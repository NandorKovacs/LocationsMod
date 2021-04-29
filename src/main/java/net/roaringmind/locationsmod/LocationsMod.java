package net.roaringmind.locationsmod;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class LocationsMod implements ModInitializer {

  public static Logger LOGGER = LogManager.getLogger();

  public static final String MOD_ID = "locationsmod";
  public static final String MOD_NAME = "LocationsMod";

  public Map<UUID, Map<String, BlockPos>> locations = new HashMap<>();
  public List<UUID> privatePlayers = new ArrayList<>();
  public Map<UUID, List<String>> publicLocations = new HashMap<>();

  public String playerHasNoLocations = "playerHasNoLocations";
  public String noSuchLocation = "noSuchLocation";
  public String locationIsPrivate = "locationIsPrivate";
  public String successfullStore = "successfullStore";
  public String playerHasNoPublicLocations = "playerHasNoPublicLocations";
  public String playerLocIsPrivate = "playerLocIsPrivate";
  public String playerAlreadyPrivate = "playerAlreadyPrivate";
  public String playerAlreadyPublic = "playerAlreadyPublic";
  public String playerSetPublic = "playerSetPublic";
  public String playerSetPrivate = "playerSetPrivate";
  public String helpMessage = "helpMessage";
  public String youHaveNoLocations = "youHaveNoLocations";
  public String locAlreadyPrivate = "locAlreadyPrivate";
  public String locAlreadyPublic = "locAlreadyPublic";
  public String locSetPublic = "locSetPublic";
  public String locSetPrivate = "locSetPrivate";

  @Override
  public void onInitialize() {
    log(Level.INFO, "Initializing");
    registerCommands();
  }

  private void registerCommands() {
    CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
      dispatcher
          .register(literal("getloc").then(argument("location name", StringArgumentType.string()).executes(ctx -> {
            getLoc(ctx, true);
            return 0;
          })).then(argument("player", EntityArgumentType.player())
              .then(argument("location name", StringArgumentType.string()).executes(ctx -> {
                getLoc(ctx, false);
                return 0;
              })).executes(ctx -> {
                getLocPlayer(ctx);
                return 0;
              })));

      dispatcher.register(literal("setloc").then(argument("location name", StringArgumentType.string())
          .then(argument("private", BoolArgumentType.bool()).executes(ctx -> {
            setLocPrivate(ctx);
            return 0;
          })))
          .then(argument("coords", BlockPosArgumentType.blockPos())
              .then(argument("location name", StringArgumentType.string())
                  .then(argument("private", BoolArgumentType.bool()).executes(ctx -> {
                    setLoc(ctx);
                    return 0;
                  })))));

      dispatcher.register(
          literal("loc").then(literal("private").then(argument("private", BoolArgumentType.bool()).executes(ctx -> {
            setPlayerPrivate(ctx);
            return 0;
          }))).then(literal("list").then(argument("player", EntityArgumentType.player()).executes(ctx -> {
            locList(ctx, false);
            return 0;
          })).executes(ctx -> {
            locList(ctx, true);
            return 0;
          })).then(literal("help").executes(ctx -> {
            locHelp(ctx);
            return 0;
          })));
    });
  }

  private void setLocPrivate(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
    ServerWorld world = ctx.getSource().getWorld();
    UUID uuid = getUUID(ctx, true);
    String locName = StringArgumentType.getString(ctx, "location name");
    Boolean setPrivate = BoolArgumentType.getBool(ctx, "private");

    if (!locations.containsKey(uuid)) {
      sendPlayerMessage(uuid, youHaveNoLocations, world, false);
      return;
    }

    if (!locations.get(uuid).containsKey(locName)) {
      sendPlayerMessage(uuid, noSuchLocation, world, false);
      return;
    }

    if (setPrivate) {
      if (!publicLocations.containsKey(uuid) || !publicLocations.get(uuid).contains(locName)) {
        sendPlayerMessage(uuid, locAlreadyPrivate, world, false);
        return;
      }
      publicLocations.get(uuid).remove(locName);
      sendPlayerMessage(uuid, locSetPrivate, world, false);
      return;
    }

    if (publicLocations.containsKey(uuid) && publicLocations.get(uuid).contains(locName)) {
      sendPlayerMessage(uuid, locAlreadyPublic, world, false);
      return;
    }

    if (!publicLocations.containsKey(uuid)) {
      publicLocations.put(uuid, new ArrayList<>());
    }
    publicLocations.get(uuid).add(locName);
    sendPlayerMessage(uuid, locSetPublic, world, false);
  }

  private void locHelp(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
    UUID uuid = getUUID(ctx, true);
    sendPlayerMessage(uuid, helpMessage, ctx.getSource().getWorld(), false);
  }

  private void setPlayerPrivate(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
    ServerWorld world = ctx.getSource().getWorld();
    UUID uuid = getUUID(ctx, true);
    Boolean isPrivate = true;
    Boolean setPrivate = BoolArgumentType.getBool(ctx, "private");

    if (!privatePlayers.contains(uuid)) {
      isPrivate = false;
    }

    if (setPrivate) {
      if (isPrivate) {
        sendPlayerMessage(uuid, playerAlreadyPrivate, world, false);
        return;
      }
      privatePlayers.add(uuid);
      sendPlayerMessage(uuid, playerSetPrivate, world, false);
      return;
    }

    if (!isPrivate) {
      sendPlayerMessage(uuid, playerAlreadyPublic, world, false);
      return;
    }
    privatePlayers.remove(uuid);
    sendPlayerMessage(uuid, playerSetPublic, world, false);
  }

  private void locList(CommandContext<ServerCommandSource> ctx, boolean self) throws CommandSyntaxException {
    UUID uuid = getUUID(ctx, self);
    ServerWorld world = ctx.getSource().getWorld();

    if (!locations.containsKey(uuid)) {
      sendPlayerMessage(uuid, playerHasNoLocations, world, false);
      return;
    }

    if (!publicLocations.containsKey(uuid)) {
      sendPlayerMessage(uuid, playerHasNoPublicLocations, world, false);
    }

    String listMessage = "Public Locations saved by " + world.getPlayerByUuid(uuid) + ":";
    for (String l : publicLocations.get(uuid)) {
      String listEntry = " - " + l + " ===== " + blockPosToString(locations.get(uuid).get(l));
      listMessage = String.join("\n", listMessage, listEntry);
    }

    sendPlayerMessage(uuid, listMessage, world, false);
  }

  private void setLoc(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
    UUID uuid = getUUID(ctx, true);
    String name = StringArgumentType.getString(ctx, "location name");
    BlockPos pos = BlockPosArgumentType.getBlockPos(ctx, "coords");
    Boolean isPrivate = BoolArgumentType.getBool(ctx, "private");

    locations.put(uuid, Map.of(name, pos));

    if (!isPrivate) {
      if (!publicLocations.containsKey(uuid)) {
        publicLocations.put(uuid, new ArrayList<>());
      }
      publicLocations.get(uuid).add(name);
    }

    sendPlayerMessage(uuid, successfullStore, ctx.getSource().getWorld(), false);
  }

  private void getLocPlayer(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
    ServerWorld world = ctx.getSource().getWorld();
    UUID uuid = getUUID(ctx, false);

    if (privatePlayers.contains(uuid)) {
      sendPlayerMessage(uuid, playerLocIsPrivate, world, false);
      return;
    }

    sendPlayerMessage(uuid, blockPosToString(world.getPlayerByUuid(uuid).getBlockPos()), world, false);
  }

  private void getLoc(CommandContext<ServerCommandSource> ctx, Boolean self) throws CommandSyntaxException {
    UUID uuid = getUUID(ctx, self);
    String locName = StringArgumentType.getString(ctx, "location name");
    ServerWorld world = ctx.getSource().getWorld();

    if (!locations.containsKey(uuid)) {
      sendPlayerMessage(uuid, playerHasNoLocations, world, false);
      return;
    }

    if (!locations.get(uuid).containsKey(locName)) {
      sendPlayerMessage(uuid, noSuchLocation, world, false);
      return;
    }

    if (!(publicLocations.containsKey(uuid) && publicLocations.get(uuid).contains(locName))) {
      sendPlayerMessage(uuid, locationIsPrivate, world, false);
    }

    BlockPos pos = locations.get(uuid).get(locName);
    sendPlayerMessage(uuid, blockPosToString(pos), world, false);
  }

  private void sendPlayerMessage(UUID uuid, String message, ServerWorld world, Boolean toolBar) {
    LiteralText prefix = new LiteralText("[LocationsMod] ");
    LiteralText literalMessage = new LiteralText(message);
    prefix.setStyle(Style.EMPTY.withColor(Formatting.AQUA));
    literalMessage.setStyle(Style.EMPTY.withColor(Formatting.WHITE));

    world.getPlayerByUuid(uuid).sendMessage(prefix.append(literalMessage), false);
  }

  private UUID getUUID(CommandContext<ServerCommandSource> ctx, boolean self) throws CommandSyntaxException {
    if (self) {
      return ctx.getSource().getPlayer().getUuid();
    }
    return EntityArgumentType.getPlayer(ctx, "player").getUuid();
  }

  private String blockPosToString(BlockPos pos) {
    return "" + pos.getX() + " " + pos.getY() + " " + pos.getZ();
  }

  public static void log(Level level, String message) {
    LOGGER.log(level, "[" + MOD_NAME + "] " + message);
  }

}
