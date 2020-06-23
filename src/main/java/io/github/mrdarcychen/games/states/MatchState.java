/*
 * Copyright 2017-2020 The BlockyArena Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.mrdarcychen.games.states;

import io.github.mrdarcychen.arenas.Arena;
import io.github.mrdarcychen.games.Game;
import io.github.mrdarcychen.games.PlayerManager;
import io.github.mrdarcychen.games.TeamMode;
import io.github.mrdarcychen.utils.DamageData;
import io.github.mrdarcychen.utils.PlayerSnapshot;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.api.world.World;

import java.util.List;

public abstract class MatchState {

    protected final Game game;
    protected List<Player> players;
    protected TeamMode teamMode;

    public MatchState(Game game, List<Player> players) {
        this.game = game;
        this.players = players;
        teamMode = game.getTeamMode();
    }

    public void recruit(Player player) {
        players.add(player);
        game.addSnapshot(new PlayerSnapshot(player));
        PlayerManager.setGame(player.getUniqueId(), game);
        // TODO: set player status as active
        player.getInventory().clear();  // TODO: allow bringing personal kit
        player.offer(Keys.GAME_MODE, GameModes.SURVIVAL);
        double initialHealth = player.health().getMaxValue(); // TODO: to be customized
        player.offer(Keys.HEALTH, initialHealth);
        setSpectate(player, false);

        // send player to lobby
        Arena arena = game.getArena();
        player.sendMessage(Text.of("Sending you to " + game.getArena().getName() + " ..."));
        Transform<World> lobby = arena.getLobbySpawn().getTransform();
        player.setTransform(lobby);
    }

    public void dismiss(Player player) {
        game.restoreSnapshotOf(player);
        PlayerManager.clearGame(player.getUniqueId());
        setSpectate(player, false);
    }

    public void eliminate(Player player, Text cause) {
        broadcast(cause);
        setSpectate(player, true);
        player.setTransform(game.getArena().getSpectatorSpawn().getTransform());
        showEliminateScreen(player);
    }

    private void showEliminateScreen(Player player) {
        Text deathText = Text.builder("YOU DIED!")
                .color(TextColors.RED).build();
        Title deathTitle = Title.builder()
                .title(deathText).fadeOut(2).stay(16).build();
        player.sendTitle(deathTitle);
    }

    public void analyze(DamageEntityEvent event, DamageData damageData) {
        if (damageData.getDamageType().getName().equalsIgnoreCase("void")) {
            damageData.getVictim().setTransform(game.getArena().getLobbySpawn().getTransform());
        }
        event.setCancelled(true);
    }

    /**
     * Broadcasts the given message to all connected Gamers in this Game.
     *
     * @param msg the message to be delivered
     */
    public void broadcast(Text msg) {
        players.forEach(it -> it.sendMessage(msg));
    }

    /**
     * Toggles custom spectator mode on the player.
     *
     * @param i true to enable spectator mode, false to disable
     */
    void setSpectate(Player player, boolean i) {
        player.offer(Keys.HEALTH, player.health().getMaxValue());
        player.offer(Keys.FOOD_LEVEL, player.foodLevel().getMaxValue());
        player.offer(Keys.VANISH, i);
        player.offer(Keys.VANISH_IGNORES_COLLISION, i);
        player.offer(Keys.CAN_FLY, i);
        player.offer(Keys.IS_FLYING, i);
        player.offer(Keys.INVULNERABLE, i);
    }
}
