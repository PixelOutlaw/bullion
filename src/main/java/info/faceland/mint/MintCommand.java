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

import static com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil.sendMessage;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.shade.acf.BaseCommand;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandAlias;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandCompletion;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandPermission;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Subcommand;
import com.tealcube.minecraft.bukkit.shade.acf.bukkit.contexts.OnlinePlayer;
import info.faceland.mint.util.MintUtil;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.nunnerycode.mint.MintPlugin;

@CommandAlias("mint|bullion")
public class MintCommand extends BaseCommand {

  private final MintPlugin plugin;

  public MintCommand(MintPlugin plugin) {
    this.plugin = plugin;
  }

  @Subcommand("reload")
  @CommandPermission("mint.reward")
  public void reload(CommandSender commandSender) {
    plugin.disable();
    plugin.enable();
    commandSender.sendMessage(PaletteUtil.color("&aMINT RELOADED!"));
  }

  @Subcommand("bank")
  public class BankCommand extends BaseCommand {

    @Subcommand("create")
    @CommandPermission("mint.bank.create")
    public void bankCreateSubcommand(CommandSender commandSender, OnlinePlayer player) {
      Player target = player.getPlayer();
      EconomyResponse response = plugin.getEconomy().bankBalance(target.getUniqueId().toString());
      if (response.transactionSuccess()) {
        commandSender.sendMessage(
            PaletteUtil.color(plugin.getSettings().getString("language.bank-create-failure2", "")));
        return;
      }
      response = plugin.getEconomy()
          .createBank(target.getUniqueId().toString(), target.getUniqueId().toString());
      if (response.transactionSuccess()) {
        commandSender.sendMessage(PaletteUtil.color(plugin.getSettings().getString(
            "language.bank-create-success", "").replaceAll("%player%", target.getDisplayName())));
        target.sendMessage(PaletteUtil.color(plugin.getSettings().getString(
            "language.bank-create-receiver", "")));
        return;
      }
      commandSender.sendMessage(
          PaletteUtil.color(plugin.getSettings().getString("language.bank-create-failure", "")));
    }

    @Subcommand("balance")
    @CommandPermission("mint.bank.balance")
    public void bankBalance(Player player) {
      EconomyResponse response = plugin.getEconomy().bankBalance(player.getUniqueId().toString());
      if (!response.transactionSuccess()) {
        player.sendMessage(
            PaletteUtil.color(plugin.getSettings().getString("language.bank-balance-failure", "")));
        return;
      }
      player.sendMessage(PaletteUtil.color(plugin.getSettings().getString(
          "language.bank-balance", "").replaceAll("%currency%", plugin.getEconomy().format(response.balance))));
    }

    @Subcommand("deposit")
    @CommandCompletion("@range:1-100")
    @CommandPermission("mint.bank.deposit")
    public void bankDeposit(Player player, double amount) {
      EconomyResponse response = plugin.getEconomy().bankBalance(player.getUniqueId().toString());
      if (!response.transactionSuccess()) {
        response = plugin.getEconomy()
            .createBank(player.getUniqueId().toString(), player.getUniqueId().toString());
        if (response.transactionSuccess()) {
          sendMessage(player, plugin.getSettings().getString(
              "language.bank-create-success", "").replaceAll("%player%", player.getName()));
          return;
        }
      }
      if (amount < 0) {
        if (plugin.getEconomy()
            .bankDeposit(player.getUniqueId().toString(), plugin.getEconomy().getBalance(
                player.getUniqueId().toString())).transactionSuccess()) {
          if (plugin.getEconomy()
              .withdrawPlayer(player.getUniqueId().toString(), plugin.getEconomy().getBalance(
                  player.getUniqueId().toString())).transactionSuccess()) {
            sendMessage(player, plugin.getSettings().getString(
                "language.bank-deposit-success", "").replaceAll("%currency%", "EVERYTHING"));
            sendMessage(player, plugin.getSettings().getString(
                "language.bank-balance", "").replaceAll("%currency%", plugin.getEconomy().format(plugin.getEconomy()
                .bankBalance(player.getUniqueId().toString()).balance)));
            return;
          }
        }
        sendMessage(player, plugin.getSettings().getString("language.bank-deposit-failure", ""));
        return;
      }
      if (!plugin.getEconomy().has(player.getUniqueId().toString(), amount)) {
        sendMessage(player, plugin.getSettings().getString("language.bank-deposit-failure", ""));
        return;
      }
      if (plugin.getEconomy().bankDeposit(player.getUniqueId().toString(), amount)
          .transactionSuccess()) {
        if (plugin.getEconomy().withdrawPlayer(player.getUniqueId().toString(), amount)
            .transactionSuccess()) {
          sendMessage(player, plugin.getSettings().getString(
              "language.bank-deposit-success", "").replaceAll("%currency%", plugin.getEconomy().format(amount)));
          sendMessage(player, plugin.getSettings().getString("language.bank-balance", "")
              .replaceAll("%currency%", plugin.getEconomy().format(plugin.getEconomy()
                  .bankBalance(player.getUniqueId().toString()).balance)));
          return;
        }
      }
      sendMessage(player, plugin.getSettings().getString("language.bank-deposit-failure", ""));
    }

    @Subcommand("force-deposit")
    @CommandCompletion("@players @range:1-100")
    @CommandPermission("mint.bank.deposit")
    public void bankDeposit(CommandSender sender, OnlinePlayer target, double amount) {
      Player player = target.getPlayer();
      EconomyResponse response = plugin.getEconomy().bankBalance(player.getUniqueId().toString());
      if (!response.transactionSuccess()) {
        response = plugin.getEconomy()
            .createBank(player.getUniqueId().toString(), player.getUniqueId().toString());
        if (response.transactionSuccess()) {
          sendMessage(player, plugin.getSettings().getString(
              "language.bank-create-success", "").replaceAll("%player%", player.getName()));
          return;
        }
      }
      if (amount < 0) {
        if (plugin.getEconomy()
            .bankDeposit(player.getUniqueId().toString(), plugin.getEconomy().getBalance(
                player.getUniqueId().toString())).transactionSuccess()) {
          if (plugin.getEconomy()
              .withdrawPlayer(player.getUniqueId().toString(), plugin.getEconomy().getBalance(
                  player.getUniqueId().toString())).transactionSuccess()) {
            sendMessage(player, plugin.getSettings().getString(
                "language.bank-deposit-success", "").replaceAll("%currency%", "EVERYTHING"));
            sendMessage(player, plugin.getSettings().getString(
                "language.bank-balance", "").replaceAll("%currency%", plugin.getEconomy().format(plugin.getEconomy()
                .bankBalance(player.getUniqueId().toString()).balance)));
            return;
          }
        }
        sendMessage(player, plugin.getSettings().getString("language.bank-deposit-failure", ""));
        return;
      }
      if (!plugin.getEconomy().has(player.getUniqueId().toString(), amount)) {
        sendMessage(player, plugin.getSettings().getString("language.bank-deposit-failure", ""));
        return;
      }
      if (plugin.getEconomy().bankDeposit(player.getUniqueId().toString(), amount)
          .transactionSuccess()) {
        if (plugin.getEconomy().withdrawPlayer(player.getUniqueId().toString(), amount)
            .transactionSuccess()) {
          sendMessage(player, plugin.getSettings().getString(
              "language.bank-deposit-success", "").replaceAll("%currency%", plugin.getEconomy().format(amount)));
          sendMessage(player, plugin.getSettings().getString("language.bank-balance", "")
              .replaceAll("%currency%", plugin.getEconomy().format(plugin.getEconomy()
                  .bankBalance(player.getUniqueId().toString()).balance)));
          return;
        }
      }
      sendMessage(player, plugin.getSettings().getString("language.bank-deposit-failure", ""));
    }

    @Subcommand("withdraw")
    @CommandCompletion("@range:1-100")
    @CommandPermission("mint.bank.withdraw")
    public void bankWithdraw(Player p, double amount) {
      Player player = p.getPlayer();
      EconomyResponse response = plugin.getEconomy().bankBalance(player.getUniqueId().toString());
      if (!response.transactionSuccess()) {
        sendMessage(player, plugin.getSettings().getString("language.bank-no-account", ""));
        return;
      }
      if (amount < 0) {
        if (plugin.getEconomy()
            .depositPlayer(player.getUniqueId().toString(), plugin.getEconomy().bankBalance(
                player.getUniqueId().toString()).balance).transactionSuccess()) {
          if (plugin.getEconomy()
              .bankWithdraw(player.getUniqueId().toString(), plugin.getEconomy().bankBalance(
                  player.getUniqueId().toString()).balance).transactionSuccess()) {
            sendMessage(player, plugin.getSettings().getString("language.bank-withdraw-success", "")
                .replaceAll("%currency%", "EVERYTHING"));
            sendMessage(player, plugin.getSettings().getString("language.bank-balance", "")
                .replaceAll("%currency%", plugin.getEconomy().format(plugin.getEconomy()
                    .bankBalance(player.getUniqueId().toString()).balance)));
            return;
          }
        }
        sendMessage(player, plugin.getSettings().getString("language.bank-withdraw-failure", ""));
        return;
      }
      if (!plugin.getEconomy().bankHas(player.getUniqueId().toString(), amount)
          .transactionSuccess()) {
        sendMessage(player, plugin.getSettings().getString("language.bank-withdraw-failure", ""));
        return;
      }
      if (plugin.getEconomy().bankWithdraw(player.getUniqueId().toString(), amount)
          .transactionSuccess() && plugin
          .getEconomy().depositPlayer(player.getUniqueId().toString(), amount).transactionSuccess()) {
        sendMessage(player, plugin.getSettings().getString("language.bank-withdraw-success", "")
            .replaceAll("%currency%", plugin.getEconomy().format(amount)));
        sendMessage(player, plugin.getSettings().getString("language.bank-balance", "")
            .replaceAll("%currency%", plugin.getEconomy().format(plugin.getEconomy()
                .bankBalance(player.getUniqueId().toString()).balance)));
        return;
      }
      sendMessage(player, plugin.getSettings().getString("language.bank-withdraw-failure", ""));
    }

    @Subcommand("force-withdraw")
    @CommandCompletion("@players @range:1-100")
    @CommandPermission("mint.bank.withdraw")
    public void bankWithdraw(CommandSender sender, OnlinePlayer target, double amount) {
      Player player = target.getPlayer();
      EconomyResponse response = plugin.getEconomy().bankBalance(player.getUniqueId().toString());
      if (!response.transactionSuccess()) {
        sendMessage(player, plugin.getSettings().getString("language.bank-no-account", ""));
        return;
      }
      if (amount < 0) {
        if (plugin.getEconomy()
            .depositPlayer(player.getUniqueId().toString(), plugin.getEconomy().bankBalance(
                player.getUniqueId().toString()).balance).transactionSuccess()) {
          if (plugin.getEconomy()
              .bankWithdraw(player.getUniqueId().toString(), plugin.getEconomy().bankBalance(
                  player.getUniqueId().toString()).balance).transactionSuccess()) {
            sendMessage(player, plugin.getSettings().getString("language.bank-withdraw-success", "")
                .replaceAll("%currency%", "EVERYTHING"));
            sendMessage(player, plugin.getSettings().getString("language.bank-balance", "")
                .replaceAll("%currency%", plugin.getEconomy().format(plugin.getEconomy()
                    .bankBalance(player.getUniqueId().toString()).balance)));
            return;
          }
        }
        sendMessage(player, plugin.getSettings().getString("language.bank-withdraw-failure", ""));
        return;
      }
      if (!plugin.getEconomy().bankHas(player.getUniqueId().toString(), amount)
          .transactionSuccess()) {
        sendMessage(player, plugin.getSettings().getString("language.bank-withdraw-failure", ""));
        return;
      }
      if (plugin.getEconomy().bankWithdraw(player.getUniqueId().toString(), amount)
          .transactionSuccess() && plugin
          .getEconomy().depositPlayer(player.getUniqueId().toString(), amount).transactionSuccess()) {
        sendMessage(player, plugin.getSettings().getString("language.bank-withdraw-success", "")
            .replaceAll("%currency%", plugin.getEconomy().format(amount)));
        sendMessage(player, plugin.getSettings().getString("language.bank-balance", "")
            .replaceAll("%currency%", plugin.getEconomy().format(plugin.getEconomy()
                .bankBalance(player.getUniqueId().toString()).balance)));
        return;
      }
      sendMessage(player, plugin.getSettings().getString("language.bank-withdraw-failure", ""));
    }
  }

  @Subcommand("add|give")
  @CommandCompletion("@players @range:1-100")
  @CommandPermission("mint.add")
  public void addSubcommand(CommandSender sender, OnlinePlayer p, double amount) {
    Player target = p.getPlayer();
    if (plugin.getEconomy().depositPlayer(target.getUniqueId().toString(), amount)
        .transactionSuccess()) {
      sendMessage(sender, plugin.getSettings().getString("language.add-money", "")
          .replaceAll("%player%", target.getDisplayName()));
    }
  }

  @Subcommand("sub")
  @CommandCompletion("@players @range:1-100")
  @CommandPermission("mint.sub")
  public void subSubcommand(CommandSender sender, OnlinePlayer p, double amount) {
    Player target = p.getPlayer();
    if (plugin.getEconomy().withdrawPlayer(target.getUniqueId().toString(), amount)
        .transactionSuccess()) {
      sendMessage(sender, plugin.getSettings().getString("language.sub-money", "")
          .replaceAll("%player%", target.getDisplayName()));
    }
  }

  @Subcommand("spawn")
  @CommandCompletion("@range:1-100 world x y z")
  @CommandPermission("mint.spawn")
  public void spawnSubcommand(CommandSender sender, int amount, String worldName, int x, int y, int z) {
    World world = Bukkit.getWorld(worldName);
    MintUtil.spawnCashDrop(new Location(world, x, y, z), Math.round(Math.abs(amount)), 0);
    sendMessage(sender, plugin.getSettings().getString("language.spawn-success", "")
        .replaceAll("%currency%", plugin.getEconomy().format(Math.abs(amount))));
  }
}
