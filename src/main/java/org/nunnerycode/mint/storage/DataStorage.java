/*
 * This file is part of Mint, licensed under the ISC License.
 *
 * Copyright (c) 2014 Richard Harrah
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted,
 * provided that the above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */
package org.nunnerycode.mint.storage;

import com.tealcube.minecraft.bukkit.kern.shade.google.common.base.Optional;
import org.nunnerycode.mint.accounts.BankAccount;
import org.nunnerycode.mint.accounts.PlayerAccount;

import java.util.Set;
import java.util.UUID;

public interface DataStorage {

    void initialize();

    void shutdown();

    Optional<PlayerAccount> loadPlayerAccount(UUID uuid);

    Optional<BankAccount> loadBankAccount(UUID uuid);

    boolean savePlayerAccount(PlayerAccount account);

    boolean saveBankAccount(BankAccount account);

    Set<PlayerAccount> loadPlayerAccounts();

    Set<BankAccount> loadBankAccounts();

    boolean savePlayerAccounts(Set<PlayerAccount> accounts);

    boolean saveBankAccounts(Set<BankAccount> accounts);

}
