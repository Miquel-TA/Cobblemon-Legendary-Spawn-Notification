/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.cooking

import com.cobblemon.mod.common.api.riding.stats.RidingStat
import com.cobblemon.mod.common.util.math.geometry.toDegrees
import com.cobblemon.mod.common.util.math.geometry.toRadians
import net.minecraft.util.FastColor
import net.minecraft.world.item.ItemStack
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private val colourMap = mapOf(
    Flavour.SPICY to 0xFEB37D,
    Flavour.DRY to 0x8AE9FC,
    Flavour.SWEET to 0xFFBEED,
    Flavour.BITTER to 0x9EED8F,
    Flavour.SOUR to 0xFCF38A
)

private val bubbleColourMap = mapOf(
    Flavour.SPICY to 0xFFD9AD,
    Flavour.DRY to 0xBCF8FE,
    Flavour.SWEET to 0xFEE3F9,
    Flavour.BITTER to 0xC8F7BC,
    Flavour.SOUR to 0xFDFAB8
)

fun getColourMixFromSeasonings(seasonings: List<ItemStack>, forBubbles: Boolean = false): Int? {
    val flavors = seasonings
            .mapNotNull { Seasonings.getFlavoursFromItemStack(it) }
            .flatMap { it.entries }
            .groupingBy { it.key }
            .fold(0) { acc, entry -> acc + entry.value }

    if (flavors.isEmpty()) return null

    val maxFlavorValue = flavors.values.maxOrNull()
    val dominantFlavors = flavors.filter { it.value == maxFlavorValue }.map { it.key }

    return getColourMixFromFlavours(dominantFlavors, forBubbles)
}

fun getColourMixFromFlavours(dominantFlavours: List<Flavour>, forBubbles: Boolean = false): Int? {
    val colors =
        dominantFlavours.mapNotNull { if (forBubbles) bubbleColourMap[it] else colourMap[it] }
            .map { FastColor.ARGB32.opaque(it) }

    if (colors.isEmpty()) return null

    return getColourMixFromColors(colors)
}

fun getColourMixFromRideStatBoosts(dominantBoosts: Iterable<RidingStat>, forBubbles: Boolean = false): Int? {
    return getColourMixFromFlavours(dominantBoosts.map { it.flavour }, forBubbles)
}

// Mixing RGB colors directly often gives poor results
// Converting them to HSL and averaging hues circularly produces more natural blends
fun getColourMixFromColors(colors: List<Int>): Int? {
    if (colors.isEmpty()) return null
    if (colors.size == 1) return colors[0]

    val hslColors = colors.map {
        val red = FastColor.ARGB32.red(it)
        val green = FastColor.ARGB32.green(it)
        val blue = FastColor.ARGB32.blue(it)

        rgbToHsl(red, green, blue)
    }

    val hues = hslColors.map { it.h.toDouble() }
    val huesAverage = hueAverage(hues).toFloat()
    val saturations = hslColors.map { it.s }
    val saturationsAverage = average(saturations)
    val lightness = hslColors.map { it.l }
    val lightnessAverage = average(lightness)

    val rgbColor = hslToRgb(huesAverage, saturationsAverage, lightnessAverage)

    return FastColor.ARGB32.color(rgbColor.r, rgbColor.g, rgbColor.b)
}

// Based on https://iter.ca/post/hue-avg
fun hueAverage(hues: List<Double>): Float {
    val points = hues.map { hue ->
        val rad = hue.toRadians()
        Pair(cos(rad), sin(rad))
    }

    val avgX = points.sumOf { it.first.toDouble() } / points.size
    val avgY = points.sumOf { it.second.toDouble() } / points.size

    var degrees = atan2(avgY, avgX).toDegrees()
    if (degrees < 0) {
        degrees += 360f
    } else if (degrees > 360) {
        degrees -= 360f
    }

    return degrees
}

fun average(values: List<Float>): Float {
    if (values.isEmpty()) return 0f
    return values.sum() / values.size
}

data class HSL(val h: Float, val s: Float, val l: Float)

fun rgbToHsl(r: Int, g: Int, b: Int): HSL {
    val rf = r / 255f
    val gf = g / 255f
    val bf = b / 255f

    val max = maxOf(rf, gf, bf)
    val min = minOf(rf, gf, bf)
    val delta = max - min

    var h = 0f
    val l = (max + min) / 2f
    val s = if (delta == 0f) 0f else delta / (1f - kotlin.math.abs(2f * l - 1f))

    if (delta != 0f) {
        h = when (max) {
            rf -> ((gf - bf) / delta) % 6f
            gf -> ((bf - rf) / delta) + 2f
            else -> ((rf - gf) / delta) + 4f
        }
        h *= 60f
        if (h < 0) h += 360f
    }

    return HSL(h, s, l)
}

data class RGB(val r: Int, val g: Int, val b: Int)

fun hslToRgb(h: Float, s: Float, l: Float): RGB {
    val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
    val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
    val m = l - c / 2f

    val (rf, gf, bf) = when {
        h < 60f -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return RGB(
        ((rf + m) * 255).toInt().coerceIn(0, 255),
        ((gf + m) * 255).toInt().coerceIn(0, 255),
        ((bf + m) * 255).toInt().coerceIn(0, 255)
    )
}