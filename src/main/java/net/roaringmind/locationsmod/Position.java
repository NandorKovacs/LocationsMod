package net.roaringmind.locationsmod;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.ClickEvent.Action;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class Position {
  private int x, y, z;
  private DimensionEnum dim;
  private boolean isPublic;

  public Position(BlockPos pos, DimensionEnum dim, boolean isPublic) {
    x = pos.getX();
    y = pos.getY();
    z = pos.getZ();
    this.dim = dim;
    this.isPublic = isPublic;
  }

  public MutableText toMutableText() {
    MutableText coords = new LiteralText(" " + x + " " + y + " " + z).setStyle(Style.EMPTY.withColor(Formatting.WHITE));
    return dim.toMutableText().append(coords);
  }

  public MutableText toMutableText(String name) {
    MutableText mutableName = new LiteralText(name + ":").setStyle(Style.EMPTY.withColor(Formatting.AQUA)
        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
            new LiteralText("Click to copy").setStyle(Style.EMPTY.withColor(Formatting.GRAY))))
        .withClickEvent(
            new ClickEvent(Action.COPY_TO_CLIPBOARD, LocationsMod.blockPosToString(new BlockPos(x, y, z)))));

    List<MutableText> res = new ArrayList<>();
    res.add(mutableName);
    res.add(toMutableText());

    return LocationsMod.joinMutable(res, " ");
  }

  public String getCoords() {
    return x + " " + y + " " + z;
  }

  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
  }

  public boolean getPublic() {
    return isPublic;
  }

  public static Position fromTag(CompoundTag tag) {
    int x = tag.getInt("x");
    int y = tag.getInt("y");
    int z = tag.getInt("z");
    boolean isPublic = tag.getBoolean("public");
    DimensionEnum dim = DimensionEnum.fromInt(tag.getInt("dim"));

    return new Position(new BlockPos(x, y, z), dim, isPublic);
  }

  public CompoundTag toTag(CompoundTag tag) {
    tag.putInt("dim", dim.toInt());
    tag.putInt("x", x);
    tag.putInt("y", y);
    tag.putInt("z", z);
    tag.putBoolean("public", isPublic);
    return tag;
  }
}
