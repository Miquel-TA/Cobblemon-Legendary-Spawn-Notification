/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.interaction

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.data.JsonDataRegistry
import com.cobblemon.mod.common.api.molang.ExpressionLike
import com.cobblemon.mod.common.api.pokemon.PokemonProperties
import com.cobblemon.mod.common.api.pokemon.requirement.OwnerQueryRequirement
import com.cobblemon.mod.common.api.pokemon.requirement.Requirement
import com.cobblemon.mod.common.api.reactive.SimpleObservable
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.adapters.CobblemonRequirementAdapter
import com.cobblemon.mod.common.pokemon.evolution.adapters.LegacyItemConditionWrapperAdapter
import com.cobblemon.mod.common.pokemon.requirements.PokemonPropertiesRequirement
import com.cobblemon.mod.common.util.adapters.*
import com.cobblemon.mod.common.util.cobblemonResource
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.minecraft.advancements.critereon.ItemPredicate
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.packs.PackType
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory

object PokemonInteractions : JsonDataRegistry<PokemonInteractionSet> {
    override val id = cobblemonResource("pokemon_interactions")
    override val type = PackType.SERVER_DATA
    override val observable = SimpleObservable<PokemonInteractions>()
    override val typeToken: TypeToken<PokemonInteractionSet> = TypeToken.get(PokemonInteractionSet::class.java)
    override val resourcePath = "pokemon_interactions"
    override val gson: Gson = GsonBuilder()
        .registerTypeAdapter(ResourceLocation::class.java, IdentifierAdapter)
        .registerTypeAdapter(PokemonProperties::class.java, pokemonPropertiesShortAdapter)
        .registerTypeAdapter(ItemPredicate::class.java, LegacyItemConditionWrapperAdapter)
        .registerTypeAdapter(Requirement::class.java, CobblemonRequirementAdapter)
        .registerTypeAdapter(InteractionEffect::class.java, InteractionEffectAdapter)
        .registerTypeAdapter(ExpressionLike::class.java, ExpressionLikeAdapter)
        .registerTypeAdapter(IntRange::class.java, IntRangeAdapter)
        .registerTypeAdapterFactory(LowerCaseEnumTypeAdapterFactory())
        .setPrettyPrinting()
        .create()

    val speciesInteractions = mutableListOf<PokemonInteractionSet>()
    val generalInteractions = mutableListOf<PokemonInteractionSet>()

    override fun sync(player: ServerPlayer) {}

    override fun reload(data: Map<ResourceLocation, PokemonInteractionSet>) {
        speciesInteractions.clear()
        generalInteractions.clear()
        val split = data.entries.partition { it.value.requirements.any { requirement -> requirement is PokemonPropertiesRequirement && requirement.target.species != null } }
        speciesInteractions.addAll(split.first.map {it.value})
        generalInteractions.addAll(split.second.map {it.value})
        Cobblemon.LOGGER.info("Loaded {} Pokémon interaction sets", data.size)
    }

    fun findInteraction(pokemon: PokemonEntity): PokemonInteraction? {
        val setCheck: (PokemonInteractionSet) -> Boolean = { it.requirements.all { req -> req.check(pokemon.pokemon) }}
        val interactionCheck: (PokemonInteraction) -> Boolean = { it.requirements.all { req -> req.check(pokemon.pokemon) } && !pokemon.pokemon.isOnInteractionCooldown(it.grouping)  }
        // species-specific interactions take priority
        val validInteractions = speciesInteractions
            .filter(setCheck)
            .flatMap { it.interactions }
            .filter(interactionCheck)
            .toMutableList()
        if (validInteractions.isEmpty()) { // if all species-specific interactions are absent/on cooldown, fallback to more generic ones
            validInteractions.addAll(generalInteractions.filter(setCheck).flatMap { it.interactions }
                .filter(interactionCheck)
            )
        }

        return validInteractions.randomOrNull()
    }
}