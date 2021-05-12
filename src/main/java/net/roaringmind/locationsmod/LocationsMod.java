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
import net.minecraft.text.ClickEvent;
import net.minecraft.text.ClickEvent.Action;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

public class LocationsMod implements ModInitializer {

  private static final Logger LOGGER = LogManager.getLogger();

  public static final String MOD_ID = "locationsmod";
  public static final String MOD_NAME = "LocationsMod";

  private Map<UUID, Map<String, Position>> locations = new HashMap<>();
  private List<UUID> privatePlayers = new ArrayList<>();
  private Map<UUID, List<String>> publicLocations = new HashMap<>();

  private String playerHasNoLocations = "playerHasNoLocations";
  private String noSuchLocation = "noSuchLocation";
  private String locationIsPrivate = "locationIsPrivate";
  private String successfullStore = "successfullStore";
  private String playerHasNoPublicLocations = "playerHasNoPublicLocations";
  private String playerLocIsPrivate = "playerLocIsPrivate";
  private String playerAlreadyPrivate = "playerAlreadyPrivate";
  private String playerAlreadyPublic = "playerAlreadyPublic";
  private String playerSetPublic = "playerSetPublic";
  private String playerSetPrivate = "playerSetPrivate";
  private String helpMessage = "helpMessage";
  private String youHaveNoLocations = "youHaveNoLocations";
  private String locAlreadyPrivate = "locAlreadyPrivate";
  private String locAlreadyPublic = "locAlreadyPublic";
  private String locSetPublic = "locSetPublic";
  private String locSetPrivate = "locSetPrivate";

  @Override
  public void onInitialize() {
    log(Level.INFO, "Initializing");
    registerCommands();
  }

  private void registerCommands() {
    CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
      dispatcher.register(literal("getloc").then(argument("player", EntityArgumentType.player())
          .then(argument("location name", StringArgumentType.string()).executes(ctx -> {
            getLoc(ctx, false);
            return 0;
          }))).then(argument("location name", StringArgumentType.string()).executes(ctx -> {
            getLoc(ctx, true);
            return 0;
          })));

      dispatcher.register(literal("getPloc").then(argument("player", EntityArgumentType.player()).executes(ctx -> {
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
    boolean setPrivate = BoolArgumentType.getBool(ctx, "private");

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
    boolean isPrivate = true;
    boolean setPrivate = BoolArgumentType.getBool(ctx, "private");

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
    UUID sourceUUID = getUUID(ctx, true);
    UUID targetUUID = getUUID(ctx, self);
    ServerWorld world = ctx.getSource().getWorld();

    if (!locations.containsKey(targetUUID)) {
      sendPlayerMessage(sourceUUID, playerHasNoLocations, world, false);
      return;
    }

    if (!publicLocations.containsKey(targetUUID) || publicLocations.get(targetUUID).isEmpty()) {
      if (self) {
        sendPlayerMessage(sourceUUID, youHaveNoLocations, world, false);
        return;
      }

      sendPlayerMessage(sourceUUID, playerHasNoPublicLocations, world, false);
      return;
    }
      
    List<MutableText> listMessage = new ArrayList<>();
    listMessage.add(new LiteralText("Public Locations saved by " + world.getPlayerByUuid(targetUUID).getName().asString() + ":").setStyle(Style.EMPTY.withColor(Formatting.AQUA)));

    for (String l : publicLocations.get(targetUUID)) {
      List<MutableText> listEntry = new ArrayList<>();
      listEntry.add(new LiteralText(" -").setStyle(Style.EMPTY.withColor(Formatting.WHITE)));

      ClickEvent clickEvent = new ClickEvent(Action.COPY_TO_CLIPBOARD, locations.get(targetUUID).get(l).getCoords());
      listEntry.add(new LiteralText(l).setStyle(Style.EMPTY.withColor(Formatting.GREEN).withClickEvent(clickEvent).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of("click to copy")))));
      listEntry.add(locations.get(targetUUID).get(l).toMutableText());

      listMessage.add(joinMutable(listEntry, " "));
    }

    MutableText resMessage = joinMutable(listMessage, "\n");
    sendPlayerMessage(sourceUUID, resMessage, world, false, false);
  }

  private void setLoc(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
    UUID uuid = getUUID(ctx, true);
    String name = StringArgumentType.getString(ctx, "location name");
    BlockPos pos = BlockPosArgumentType.getBlockPos(ctx, "coords");
    boolean isPrivate = BoolArgumentType.getBool(ctx, "private");

    locations.computeIfAbsent(uuid, l -> new HashMap<>());

    Dimension dim;

    DimensionType end = ctx.getSource().getMinecraftServer().getRegistryManager().getDimensionTypes()
        .get(DimensionType.THE_END_ID);
    DimensionType overworld = ctx.getSource().getMinecraftServer().getRegistryManager().getDimensionTypes()
        .get(DimensionType.OVERWORLD_ID);
    DimensionType nether = ctx.getSource().getMinecraftServer().getRegistryManager().getDimensionTypes()
        .get(DimensionType.THE_NETHER_ID);
    DimensionType currentDim = ctx.getSource().getWorld().getDimension();

    if (currentDim.equals(end)) {
      dim = Dimension.END;
    } else if (currentDim.equals(overworld)) {
      dim = Dimension.OVERWOLRD;
    } else if (currentDim.equals(nether)) {
      dim = Dimension.NETHER;
    } else {
      dim = Dimension.NODIM;
    }

    locations.get(uuid).put(name, new Position(pos, dim));

    if (!isPrivate) {
      publicLocations.computeIfAbsent(uuid, loc -> new ArrayList<>());

      publicLocations.get(uuid).add(name);
    }

    sendPlayerMessage(uuid, successfullStore, ctx.getSource().getWorld(), false);
  }

  private void getLocPlayer(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
    ServerWorld world = ctx.getSource().getWorld();
    UUID sourceUUID = getUUID(ctx, true);
    UUID targetUUID = getUUID(ctx, false);

    if (privatePlayers.contains(targetUUID) && targetUUID != sourceUUID) {
      sendPlayerMessage(sourceUUID, playerLocIsPrivate, world, false);
      return;
    }

    sendPlayerMessage(sourceUUID, blockPosToString(world.getPlayerByUuid(targetUUID).getBlockPos()), world, false);
  }

  private void getLoc(CommandContext<ServerCommandSource> ctx, boolean self) throws CommandSyntaxException {
    UUID sourceUUID = getUUID(ctx, true);
    UUID targetUUID = getUUID(ctx, self);
    String locName = StringArgumentType.getString(ctx, "location name");
    ServerWorld world = ctx.getSource().getWorld();

    if (!locations.containsKey(targetUUID)) {
      sendPlayerMessage(sourceUUID, playerHasNoLocations, world, false);
      return;
    }

    if (!locations.get(targetUUID).containsKey(locName)) {
      sendPlayerMessage(sourceUUID, noSuchLocation, world, false);
      return;
    }

    if (!(publicLocations.containsKey(targetUUID) && publicLocations.get(targetUUID).contains(locName)) && !self) {
      sendPlayerMessage(sourceUUID, locationIsPrivate, world, false);
      return;
    }

    MutableText message = locations.get(targetUUID).get(locName).toMutableText();
    message.setStyle(Style.EMPTY.withColor(Formatting.WHITE));

    sendPlayerMessage(sourceUUID, message, world, false);
  }

  private void sendPlayerMessage(UUID uuid, MutableText message, ServerWorld world, boolean toolBar,
      boolean hasPrefix) {
    LiteralText prefix = new LiteralText("[LocationsMod] ");
    prefix.setStyle(Style.EMPTY.withColor(Formatting.AQUA));

    if (!hasPrefix) {
      world.getPlayerByUuid(uuid).sendMessage(message, toolBar);
      return;
    }

    world.getPlayerByUuid(uuid).sendMessage(prefix.append(message), toolBar);
  }

  private void sendPlayerMessage(UUID uuid, String message, ServerWorld world, boolean toolBar, boolean hasPrefix) {
    MutableText literalMessage = new LiteralText(message);
    literalMessage.setStyle(Style.EMPTY.withColor(Formatting.WHITE));

    sendPlayerMessage(uuid, literalMessage, world, toolBar, hasPrefix);
  }

  private void sendPlayerMessage(UUID uuid, String message, ServerWorld world, boolean toolBar) {
    sendPlayerMessage(uuid, message, world, toolBar, true);
  }

  private void sendPlayerMessage(UUID uuid, MutableText message, ServerWorld world, boolean toolBar) {
    sendPlayerMessage(uuid, message, world, toolBar, true);
  }

  private UUID getUUID(CommandContext<ServerCommandSource> ctx, boolean self) throws CommandSyntaxException {
    if (self) {
      return ctx.getSource().getPlayer().getUuid();
    }
    return EntityArgumentType.getPlayer(ctx, "player").getUuid();
  }

  private MutableText joinMutable(List<MutableText> mutables, String delim) {
    MutableText delimMutable = new LiteralText(delim);
    MutableText res = new LiteralText("");

    for (MutableText mt : mutables) {
      res = res.append(delimMutable).append(mt);
    }

    return res;
  }

  private String blockPosToString(BlockPos pos) {
    return "" + pos.getX() + " " + pos.getY() + " " + pos.getZ();
  }

  public static void log(Level level, String message) {
    LOGGER.log(level, "[" + MOD_NAME + "] " + message);
  }

}
