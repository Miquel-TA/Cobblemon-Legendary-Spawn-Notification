/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.stats

import net.minecraft.resources.ResourceLocation

object CobblemonStats {
    val stats = mutableMapOf<String,  ResourceLocation>()

    var CAPTURED: ResourceLocation = ResourceLocation.fromNamespaceAndPath("cobblemon", "captured") //makeCustomStat("captured", StatFormatter.DEFAULT)
    val SHINIES_CAPTURED: ResourceLocation = ResourceLocation.fromNamespaceAndPath("cobblemon", "shinies_captured") //makeCustomStat("shinies_captured", StatFormatter.DEFAULT)
    val RELEASED: ResourceLocation = ResourceLocation.fromNamespaceAndPath("cobblemon", "released") //makeCustomStat("released", StatFormatter.DEFAULT)
    val EVOLVED: ResourceLocation = ResourceLocation.fromNamespaceAndPath("cobblemon", "evolved") //makeCustomStat("evolved", StatFormatter.DEFAULT)
    val LEVEL_UP: ResourceLocation = ResourceLocation.fromNamespaceAndPath("cobblemon", "level_up") //makeCustomStat("level_up", StatFormatter.DEFAULT)
    val BATTLES_WON: ResourceLocation = ResourceLocation.fromNamespaceAndPath("cobblemon", "battles_won") //makeCustomStat("battles_won", StatFormatter.DEFAULT)
    val BATTLES_LOST: ResourceLocation = ResourceLocation.fromNamespaceAndPath("cobblemon", "battles_lost") //makeCustomStat("battles_lost", StatFormatter.DEFAULT)
    val BATTLES_FLED: ResourceLocation = ResourceLocation.fromNamespaceAndPath("cobblemon", "battles_fled") //makeCustomStat("battles_fled", StatFormatter.DEFAULT)
    val BATTLES_TOTAL: ResourceLocation = ResourceLocation.fromNamespaceAndPath("cobblemon", "battles_total") //makeCustomStat("battles_total", StatFormatter.DEFAULT)
    val DEX_ENTRIES: ResourceLocation = ResourceLocation.fromNamespaceAndPath("cobblemon", "dex_entries") //makeCustomStat("dex_entries", StatFormatter.DEFAULT)
    val EGGS_COLLECTED: ResourceLocation = ResourceLocation.fromNamespaceAndPath("cobblemon", "eggs_collected") //makeCustomStat("eggs_collected", StatFormatter.DEFAULT)
    val EGGS_HATCHED: ResourceLocation = ResourceLocation.fromNamespaceAndPath("cobblemon", "eggs_hatched") //makeCustomStat("eggs_hatched", StatFormatter.DEFAULT)
    val TRADED: ResourceLocation = ResourceLocation.fromNamespaceAndPath("cobblemon", "traded") //makeCustomStat("traded", StatFormatter.DEFAULT)
    val FOSSILS_REVIVED: ResourceLocation = ResourceLocation.fromNamespaceAndPath("cobblemon", "fossils_revived") //makeCustomStat("fossils_revived", StatFormatter.DEFAULT)
    val TIMES_RIDDEN: ResourceLocation = ResourceLocation.fromNamespaceAndPath("cobblemon", "times_ridden")
    val ROD_CASTS: ResourceLocation = ResourceLocation.fromNamespaceAndPath("cobblemon", "rod_casts")
    val REEL_INS: ResourceLocation = ResourceLocation.fromNamespaceAndPath("cobblemon", "reel_ins")
    val POKEMON_INTERACTED_WITH: ResourceLocation = ResourceLocation.fromNamespaceAndPath("cobblemon", "pokemon_interacted_with") //makeCustomStat("pokemon_interacted_with", StatFormatter.DEFAULT)
    //TODO block, stats (interact, gimmi), riding (styles?)

    fun registerStats() {
        stats["captured"] = CAPTURED
        stats["shinies_captured"] = SHINIES_CAPTURED
        stats["released"] = RELEASED
        stats["evolved"] = EVOLVED
        stats["level_up"] = LEVEL_UP
        stats["battles_won"] = BATTLES_WON
        stats["battles_lost"] = BATTLES_LOST
        stats["battles_fled"] = BATTLES_FLED
        stats["battles_total"] = BATTLES_TOTAL
        stats["dex_entries"] = DEX_ENTRIES
        stats["eggs_collected"] = EGGS_COLLECTED
        stats["eggs_hatched"] = EGGS_HATCHED
        stats["traded"] = TRADED
        stats["fossils_revived"] = FOSSILS_REVIVED
        stats["times_ridden"] = TIMES_RIDDEN
        stats["rod_casts"] = ROD_CASTS
        stats["reel_ins"] = REEL_INS
        stats["pokemon_interacted_with"] = POKEMON_INTERACTED_WITH
    }
}
