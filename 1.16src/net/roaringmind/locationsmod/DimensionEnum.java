package net.roaringmind.locationsmod;

import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

public enum DimensionEnum {
  OVERWOLRD, NETHER, END, NODIM;

  public MutableText toMutableText() {
    switch (this) {
    case END:
      return new LiteralText("<End>").setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE));
    case NETHER:
      return new LiteralText("<Nether>").setStyle(Style.EMPTY.withColor(Formatting.DARK_RED));
    case OVERWOLRD:
      return new LiteralText("<Overworld>").setStyle(Style.EMPTY.withColor(Formatting.DARK_GREEN));
    default:
      return new LiteralText("<Bad Dimension>").setStyle(Style.EMPTY.withColor(Formatting.DARK_PURPLE).withFormatting(Formatting.OBFUSCATED));
    }
  }

  public int toInt() {
    switch (this) {
      case END:
        return 0;
      case NETHER:
        return 1;
      case OVERWOLRD:
        return 2;
      default:
        return 3;
      }
  }

  public static DimensionEnum fromInt(int n) {
    if (n == 0) {
      return END;
    }
    if (n == 1) {
      return NETHER;
    }
    if (n == 2) {
      return OVERWOLRD;
    }
    return NODIM;
  }
}
