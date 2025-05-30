/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.mint;

import static com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil.color;
import static com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil.sendMessage;

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

  private final String msg_withdrawSuccess, msg_withdrawFail, msg_depositSuccess, msg_depositFail,
      msg_bankBalance, msg_bankCreate;

  public MintCommand(MintPlugin plugin) {
    this.plugin = plugin;
    msg_withdrawSuccess = color(plugin.getSettings().getString("language.bank-withdraw-success", ""));
    msg_withdrawFail = color(plugin.getSettings().getString("language.bank-withdraw-failure", ""));
    msg_depositSuccess = color(plugin.getSettings().getString("language.bank-deposit-success", ""));
    msg_depositFail = color(plugin.getSettings().getString("language.bank-deposit-failure", ""));
    msg_bankBalance = color(plugin.getSettings().getString("language.bank-balance", ""));
    msg_bankCreate = plugin.getSettings().getString("language.bank-create-success", "");
  }

  @Subcommand("reload")
  @CommandPermission("mint.reward")
  public void reload(CommandSender commandSender) {
    plugin.disable();
    plugin.enable();
    commandSender.sendMessage(color("&aMINT RELOADED!"));
  }

  @Subcommand("bank")
  public class BankCommand extends BaseCommand {

    @Subcommand("create")
    @CommandPermission("mint.bank.create")
    public void bankCreateSubcommand(CommandSender commandSender, OnlinePlayer player) {
      Player target = player.getPlayer();
      EconomyResponse response = plugin.getEconomy().bankBalance(target.getUniqueId().toString());
      if (response.transactionSuccess()) {
        commandSender.sendMessage(color(plugin.getSettings().getString("language.bank-create-failure2", "")));
        return;
      }
      response = plugin.getEconomy()
          .createBank(target.getUniqueId().toString(), target.getUniqueId().toString());
      if (response.transactionSuccess()) {
        commandSender.sendMessage(color(msg_bankCreate.replaceAll("%player%", target.getDisplayName())));
        target.sendMessage(color(plugin.getSettings().getString("language.bank-create-receiver", "")));
        return;
      }
      commandSender.sendMessage(
          color(plugin.getSettings().getString("language.bank-create-failure", "")));
    }

    @Subcommand("balance")
    @CommandPermission("mint.bank.balance")
    public void bankBalance(Player player) {
      EconomyResponse response = plugin.getEconomy().bankBalance(player.getUniqueId().toString());
      if (!response.transactionSuccess()) {
        player.sendMessage(color(plugin.getSettings().getString("language.bank-balance-failure", "")));
        return;
      }
      player.sendMessage(msg_bankBalance.replaceAll("%currency%", plugin.getEconomy().format(response.balance)));
    }

    @Subcommand("force-deposit")
    @CommandCompletion("@players @range:1-100")
    @CommandPermission("mint.bank.deposit")
    public void bankDeposit(CommandSender sender, OnlinePlayer target, double amount) {
      Player player = target.getPlayer();
      var playerId = player.getUniqueId().toString();
      var econ = plugin.getEconomy();
      EconomyResponse response = econ.bankBalance(playerId);
      if (!response.transactionSuccess()) {
        response = econ.createBank(playerId, playerId);
        if (response.transactionSuccess()) {
          sendMessage(player, msg_bankCreate.replaceAll("%player%", player.getName()));
          return;
        }
      }
      if (!econ.has(playerId, 1)) {
        sendMessage(player, msg_depositFail);
        return;
      }
      if (amount == -0.5) {
        amount = 0.5 * econ.getBalance(playerId);
      } else if (amount < 0) {
        amount = econ.getBalance(playerId);
      }
      if (amount < 1) {
        sendMessage(player, msg_depositFail);
        return;
      }
      if (!econ.has(playerId, amount)) {
        sendMessage(player, msg_depositFail);
        return;
      }
      if (econ.verifiedTransferDeposit(playerId, amount)) {
        sendMessage(player, msg_depositSuccess.replaceAll("%currency%", econ.format(amount)));
        sendMessage(player, msg_bankBalance.replaceAll("%currency%", econ.format(econ.bankBalance(playerId).balance)));
        return;
      }
      sendMessage(player, msg_depositFail);
    }

    @Subcommand("force-withdraw")
    @CommandCompletion("@players @range:1-100")
    @CommandPermission("mint.bank.withdraw")
    public void bankWithdraw(CommandSender sender, OnlinePlayer target, double amount) {
      Player player = target.getPlayer();
      var playerId = player.getUniqueId().toString();
      var econ = plugin.getEconomy();
      EconomyResponse response = econ.bankBalance(playerId);
      if (!response.transactionSuccess()) {
        sendMessage(player, plugin.getSettings().getString("language.bank-no-account", ""));
        return;
      }
      if (!econ.bankHas(playerId, 1).transactionSuccess()) {
        sendMessage(player, msg_withdrawFail);
        return;
      }
      if (amount == -0.5) {
        amount = 0.5 * econ.bankBalance(playerId).amount;
      } else if (amount < 0) {
        amount = econ.bankBalance(playerId).amount;
      }
      if (amount < 1) {
        sendMessage(player, msg_withdrawFail);
        return;
      }
      if (!econ.bankHas(playerId, amount).transactionSuccess()) {
        sendMessage(player, msg_withdrawFail);
        return;
      }
      if (econ.verifiedTransferWithdraw(playerId, amount)) {
        sendMessage(player, msg_withdrawSuccess.replaceAll("%currency%", econ.format(amount)));
        sendMessage(player, msg_bankBalance.replaceAll("%currency%", econ.format(econ.bankBalance(playerId).balance)));
        return;
      }
      sendMessage(player, msg_withdrawFail);
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
