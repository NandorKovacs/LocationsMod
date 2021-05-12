package net.roaringmind.locationsmod;

import java.text.Format;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class Position {
  int x, y, z;
  Dimension dim;
  public Position(BlockPos pos, Dimension dim) {
    this.x = pos.getX();
    this.y = pos.getY();
    this.z = pos.getZ();
    this.dim = dim;
  }

  public MutableText toMutableText() {
    LiteralText dimText;
    if (dim == Dimension.OVERWOLRD) {
      dimText = new LiteralText("<Overworld> ");
      dimText.setStyle(Style.EMPTY.withColor(Formatting.DARK_GREEN));
    } else if (dim == Dimension.NETHER) {
      dimText = new LiteralText("<Nether> ");
      dimText.setStyle(Style.EMPTY.withColor(Formatting.DARK_RED));
    } else if (dim == Dimension.END) {
      dimText = new LiteralText("<End> ");
      dimText.setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE));
    } else {
      dimText = new LiteralText("<out of this world> ");
      dimText.setStyle(Style.EMPTY.withFormatting(Formatting.OBFUSCATED));
    }
    MutableText coords = new LiteralText(x + " " + y + " " + z).setStyle(Style.EMPTY.withColor(Formatting.WHITE));
    return dimText.append(coords);
  }

  public String getCoords() {
    return x + " " + y + " " + z;
  }
}
