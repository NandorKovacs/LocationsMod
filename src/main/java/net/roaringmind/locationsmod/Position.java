package net.roaringmind.locationsmod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

public class Position extends PersistentState {
  private int x, y, z;
  private DimensionEnum dim;
  private boolean isPublic;

  public Position(String key) {
    super(key);
  }

  public MutableText toMutableText() {
    MutableText coords = new LiteralText(" " + x + " " + y + " " + z).setStyle(Style.EMPTY.withColor(Formatting.WHITE));
    return dim.toMutableText().append(coords);
  }

  public String getCoords() {
    return x + " " + y + " " + z;
  }

  public void setAll(BlockPos pos, DimensionEnum dim, boolean isPublic) {
    x = pos.getX();
    y = pos.getY();
    z = pos.getZ();
    this.dim = dim;
    this.isPublic = isPublic;
    markDirty();
  }

  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
    markDirty();
  }

  public boolean getPublic() {
    return isPublic;
  }

  @Override
  public void fromTag(CompoundTag tag) {
    x = tag.getInt("x");
    y = tag.getInt("y");
    z = tag.getInt("z");
    isPublic = tag.getBoolean("public");
    dim = DimensionEnum.fromInt(tag.getInt("dim"));
  }

  @Override
  public CompoundTag toTag(CompoundTag tag) {
    tag.putInt("dim", dim.toInt());
    tag.putInt("x", x);
    tag.putInt("y", y);
    tag.putInt("z", z);
    tag.putBoolean("public", isPublic);
    return tag;
  }
}
