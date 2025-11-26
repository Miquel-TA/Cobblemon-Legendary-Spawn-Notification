/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.events

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.Priority
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.entity.SpawnEvent
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity.RemovalReason

/**
 * Broadcasts nearby shiny or legendary spawns and despawns to the server so players are aware of them.
 */
object SpawnNotificationHandler : EventHandler {

    override fun registerListeners() {
        CobblemonEvents.POKEMON_ENTITY_SPAWN.subscribe(Priority.NORMAL, ::onPokemonSpawn)
    }

    private fun onPokemonSpawn(event: SpawnEvent<PokemonEntity>) {
        val entity = event.entity
        val serverLevel = entity.level() as? ServerLevel ?: return
        val pokemon = entity.pokemon

        if (!pokemon.isWild() || !shouldAnnounce(pokemon) || !isNearAnyPlayer(entity, serverLevel)) {
            return
        }

        val spawnMessage = buildAnnouncement("spawn", entity, pokemon)
        broadcast(serverLevel, spawnMessage)
        logNotice("spawned", pokemon, entity, serverLevel)

        val subscription = entity.removalObservable.subscribe { reason ->
            if (reason == RemovalReason.DISCARDED && shouldAnnounce(pokemon) && isNearAnyPlayer(entity, serverLevel)) {
                val despawnMessage = buildAnnouncement("despawn", entity, pokemon)
                broadcast(serverLevel, despawnMessage)
                logNotice("despawned", pokemon, entity, serverLevel)
            }
        }

        entity.subscriptions.add(subscription)
    }

    private fun buildAnnouncement(action: String, entity: PokemonEntity, pokemon: Pokemon): MutableComponent {
        val rarity = buildRarityLabel(pokemon)
        val displayName = pokemon.getDisplayName(true).withStyle(ChatFormatting.AQUA)

        val base = Component.translatable("cobblemon.spawn_notification.$action", rarity, displayName)
            .withStyle(if (action == "spawn") ChatFormatting.GOLD else ChatFormatting.RED)

        if (Cobblemon.config.announcePokemonCoordinates) {
            val position = entity.blockPosition()
            val dimension = entity.level().dimension().location()
            base.append(
                Component.translatable(
                    "cobblemon.spawn_notification.coordinates",
                    position.x,
                    position.y,
                    position.z,
                    dimension
                ).withStyle(ChatFormatting.GRAY)
            )
        }

        return base
    }

    private fun buildRarityLabel(pokemon: Pokemon): MutableComponent {
        val key = when {
            pokemon.shiny && pokemon.isLegendary() -> "cobblemon.spawn_notification.rarity.shiny_legendary"
            pokemon.shiny -> "cobblemon.spawn_notification.rarity.shiny"
            pokemon.isLegendary() -> "cobblemon.spawn_notification.rarity.legendary"
            else -> "cobblemon.spawn_notification.rarity.standard"
        }

        val formatting = when {
            pokemon.shiny -> ChatFormatting.AQUA
            pokemon.isLegendary() -> ChatFormatting.LIGHT_PURPLE
            else -> ChatFormatting.WHITE
        }

        return Component.translatable(key).withStyle(formatting)
    }

    private fun broadcast(level: ServerLevel, message: Component) {
        level.server.playerList.broadcastSystemMessage(message, false)
    }

    private fun logNotice(action: String, pokemon: Pokemon, entity: PokemonEntity, level: ServerLevel) {
        val position = entity.blockPosition()
        Cobblemon.LOGGER.info(
            "Pokemon {} notice: {} {} at ({}, {}, {}) in {}",
            action,
            rarityLabelText(pokemon),
            pokemon.getDisplayName(true).string,
            position.x,
            position.y,
            position.z,
            level.dimension().location()
        )
    }

    private fun shouldAnnounce(pokemon: Pokemon): Boolean {
        return Cobblemon.config.announceAllPokemonSpawns || pokemon.isLegendary() || pokemon.shiny
    }

    private fun isNearAnyPlayer(entity: PokemonEntity, level: ServerLevel): Boolean {
        val range = Cobblemon.config.despawnerNearDistance.toDouble()
        return level.players().any { it.distanceTo(entity) <= range }
    }

    private fun rarityLabelText(pokemon: Pokemon): String = when {
        pokemon.shiny && pokemon.isLegendary() -> "Shiny Legendary"
        pokemon.shiny -> "Shiny"
        pokemon.isLegendary() -> "Legendary"
        else -> "Standard"
    }
}
