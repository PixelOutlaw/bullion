/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.mint.listeners;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import info.faceland.mint.util.MintUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.nunnerycode.mint.MintPlugin;

public class ItemSpawnListener implements Listener {

  private final MintPlugin plugin;
  private final Random random = new Random();

  public ItemSpawnListener(MintPlugin mintPlugin) {
    this.plugin = mintPlugin;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onBitSpawn(ItemSpawnEvent event) {

    if (!MintUtil.isCashDrop(event.getEntity().getItemStack())) {
      return;
    }

    ItemStack nuggetStack = event.getEntity().getItemStack();
    String s = nuggetStack.getLore().get(0);
    String stripped = ChatColor.stripColor(s);

    double amount = NumberUtils.toDouble(stripped);
    if (amount <= 0.00D) {
      event.setCancelled(true);
      return;
    }

    ItemStackExtensionsKt.setDisplayName(nuggetStack, MintUtil.CASH_STRING);

    int amountId = (int) Math.floor(amount);
    if (amountId < 2) {
      ItemStackExtensionsKt.setCustomModelData(nuggetStack, 1000000 + random.nextInt(999999));
    } else if (amountId == 2) {
      ItemStackExtensionsKt.setCustomModelData(nuggetStack, 2000000 + random.nextInt(999999));
    } else if (amountId == 3) {
      ItemStackExtensionsKt.setCustomModelData(nuggetStack, 3000000 + random.nextInt(999999));
    } else if (amountId < 12) {
      ItemStackExtensionsKt.setCustomModelData(nuggetStack, 4000000 + random.nextInt(999999));
    } else if (amountId < 80) {
      ItemStackExtensionsKt.setCustomModelData(nuggetStack, 5000000 + random.nextInt(999999));
    } else if (amountId < 250) {
      ItemStackExtensionsKt.setCustomModelData(nuggetStack, 6000000 + random.nextInt(999999));
    } else if (amountId < 999) {
      ItemStackExtensionsKt.setCustomModelData(nuggetStack, 7000000 + random.nextInt(999999));
    } else {
      ItemStackExtensionsKt.setCustomModelData(nuggetStack, 8000000 + random.nextInt(999999));
    }

    event.getEntity().setItemStack(nuggetStack);
    event.getEntity().setCustomName(ChatColor.YELLOW + plugin.getEconomy().format(Math.floor(amount)));
    event.getEntity().setCustomNameVisible(true);
  }
}
