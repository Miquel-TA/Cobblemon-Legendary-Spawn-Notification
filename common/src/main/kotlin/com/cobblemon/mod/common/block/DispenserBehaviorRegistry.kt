/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.block

import net.minecraft.world.item.Item
import net.minecraft.core.dispenser.DispenseItemBehavior
import net.minecraft.world.item.Items
import net.minecraft.world.item.alchemy.PotionContents
import net.minecraft.world.item.alchemy.Potions
import net.minecraft.world.level.block.DispenserBlock

object DispenserBehaviorRegistry {
    private val behaviors = mutableMapOf<Item, DispenseItemBehavior>()

    fun register(items: List<Item>, behaviorProvider: (Item) -> DispenseItemBehavior) {
        for (item in items) {
            behaviors[item] = behaviorProvider(item)
        }
    }

    fun applyBehaviors() {
        behaviors.forEach { (item, behavior) ->
            DispenserBlock.registerBehavior(item, behavior)
        }
    }

    // TODO: Seems like there can only 1 behavior per item, so registering the leaves with the honey bottle overrode the honey bottle behavior for the log.
    //  Registering a new behavior with `DispenserBlock.registerBehavior` will override any existing ones for that item, possibly even ones from Minecraft and other mods
    //  Will probably need to use an alternate way of implementing this to avoid conflicts.
    fun registerDispenserBehaviors() {
        // register(listOf(PotionContents.createItemStack(Items.POTION, Potions.WATER).item)) { SaccharineLogSlatheredBlock.createBehavior() }
        // register(listOf(Items.HONEY_BOTTLE)) { SaccharineLogBlock.createBehavior() }
        // register(listOf(Items.HONEY_BOTTLE, Items.GLASS_BOTTLE)) { item -> SaccharineLeafBlock.createBehavior(item)}
        applyBehaviors()
    }
}