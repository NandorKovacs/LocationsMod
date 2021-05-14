package net.roaringmind.locationsmod;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class Position {
  int x, y, z;
  DimensionEnum dim;
  boolean isPublic;

  public Position(BlockPos pos, DimensionEnum dim, boolean isPublic) {
    this.x = pos.getX();
    this.y = pos.getY();
    this.z = pos.getZ();
    this.dim = dim;
    this.isPublic = isPublic;
  }

  public MutableText toMutableText() {
    MutableText coords = new LiteralText(" " + x + " " + y + " " + z).setStyle(Style.EMPTY.withColor(Formatting.WHITE));
    return dim.toMutableText().append(coords);
  }

  public String getCoords() {
    return x + " " + y + " " + z;
  }
}
