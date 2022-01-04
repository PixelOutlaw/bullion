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
package info.faceland.mint;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import com.tealcube.minecraft.bukkit.shade.acf.BaseCommand;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandAlias;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandCompletion;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandPermission;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Default;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Subcommand;
import com.tealcube.minecraft.bukkit.shade.acf.bukkit.contexts.OnlinePlayer;
import info.faceland.mint.util.MintUtil;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.nunnerycode.mint.MintPlugin;

@CommandAlias("pay")
public class PayCommand extends BaseCommand {

  private final MintPlugin plugin;

  public PayCommand(MintPlugin plugin) {
    this.plugin = plugin;
  }

  @Subcommand("pay")
  @CommandCompletion("@players @range:1-100")
  public void payCommand(Player sender, OnlinePlayer recipient, double amount,
      @Default("false") boolean overrideRange) {
    Player target = recipient.getPlayer();
    if (amount < 1D) {
      sendMessage(sender, plugin.getSettings().getString("language.pay-negative-money"));
      return;
    }
    if (!overrideRange || !sender.hasPermission("mint.epay")) {
      if (!sender.getLocation().getWorld().equals(target.getLocation().getWorld()) ||
          sender.getLocation().distanceSquared(target.getLocation()) > plugin.getSettings()
              .getDouble("config.pay-distance-max", 25)) {
        sendMessage(sender, plugin.getSettings().getString("language.pay-range", ""));
        return;
      }
    }
    if (!plugin.getEconomy().has(sender.getUniqueId().toString(), amount)) {
      sendMessage(sender, plugin.getSettings().getString("language.pay-failure", ""));
      return;
    }
    if (plugin.getEconomy().withdrawPlayer(sender.getUniqueId().toString(), amount)
        .transactionSuccess() && plugin.getEconomy().depositPlayer(target.getUniqueId()
            .toString(), amount).transactionSuccess()) {
      sendMessage(sender, plugin.getSettings().getString("language.pay-success", "")
          .replaceAll("%player%", target.getDisplayName())
          .replaceAll("%currency%", plugin.getEconomy().format(Math.abs(amount))));
      sendMessage(sender, plugin.getSettings().getString("language.gain-money", "")
          .replaceAll("%amount%", String.valueOf(amount))
          .replaceAll("%money%", amount == 1D ? plugin.getEconomy().currencyNameSingular()
              : plugin.getEconomy().currencyNamePlural())
          .replaceAll("%currency%", plugin.getEconomy().format(amount)));
      return;
    }
    sendMessage(sender, plugin.getSettings().getString("language.pay-failure", ""));
  }
}
