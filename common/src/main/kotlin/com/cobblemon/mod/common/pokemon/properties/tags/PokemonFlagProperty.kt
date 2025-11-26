/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.pokemon.properties.tags

import com.cobblemon.mod.common.api.properties.CustomPokemonPropertyType
import com.cobblemon.mod.common.pokemon.properties.StringProperty

object PokemonFlagProperty : CustomPokemonPropertyType<StringProperty> {

    private const val KEY = "tag"

    override val keys = setOf(KEY)
    override val needsKey = true

    override fun fromString(value: String?) = if (value == null) null else StringProperty(KEY, value, { _, _ -> }, { pokemon, underlyingValue -> pokemon.hasLabels(underlyingValue) })

    override fun examples() = emptySet<String>()
}