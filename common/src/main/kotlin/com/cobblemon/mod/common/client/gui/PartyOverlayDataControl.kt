/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui

import com.cobblemon.mod.common.CobblemonSounds
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.sounds.SoundEvent
import java.util.*

object PartyOverlayDataControl {
    const val BAR_UPDATE_BEFORE_TIME = 15
    const val BAR_UPDATE_AFTER_TIME = 15
    const val BAR_FLASH_TIME = 2
    val POPUP_TIME = FadeTimings(3, 40, 3)
    const val LEVEL_UP_PORTRAIT_TIME = 10

    val BAR_UPDATE_NO_LEVEL_TIME = BAR_UPDATE_BEFORE_TIME + BAR_UPDATE_AFTER_TIME + BAR_FLASH_TIME

    private enum class RenderTarget {
        NONE,
        EXP_GAINED,
        EVOLUTION,
        MOVES
    }

    private val overlayData = mutableMapOf<UUID, OverlayData>()

    private var barFillupSound: SoundInstance? = null
    private var levelUpJingleSound: SoundInstance? = null

    fun pokemonGainedExp(pokemonUuid : UUID, oldLevel: Int?, expGained: Int, countOfMovesLearned: Int, countOfEvosUnlocked: Int = 0) {
        var time = BAR_UPDATE_NO_LEVEL_TIME
        if (countOfMovesLearned > 0 || countOfEvosUnlocked > 0) {
            time = BAR_UPDATE_BEFORE_TIME + BAR_FLASH_TIME + POPUP_TIME.total()
        }

        val data = overlayData[pokemonUuid]
        val newData = ExpGainedData(oldLevel, expGained, countOfMovesLearned, countOfEvosUnlocked).also { it.ticksMax = time }
        if (data != null) {
            if (data.movesGainedData != null) {
                newData.countOfMovesLearned += data.movesGainedData!!.movesCount
            }
            if (data.evoGainedData != null) {
                newData.countOfEvosUnlocked += data.evoGainedData!!.evolutionCount
            }

            val expData = data.expGainedData
            if (expData != null) {
                newData.countOfMovesLearned += expData.countOfMovesLearned
                newData.countOfEvosUnlocked += expData.countOfEvosUnlocked
                if (expData.oldLevel != null) {
                    newData.oldLevel = expData.oldLevel
                }
                newData.expGained += expData.expGained
            }
        }
        overlayData[pokemonUuid] = OverlayData(expGainedData = newData)

        stopSound(barFillupSound)
        barFillupSound = playSound(CobblemonSounds.LEVELUP_START)
    }

    fun pokemonGainedEvo(pokemonUuid : UUID, evoCount: Int) {
        val data = overlayData[pokemonUuid]
        val newData = EvolutionGainedData(evoCount).also { it.ticksMax = POPUP_TIME.total() }
        if (data != null) {
            if (data.expGainedData != null && data.expGainedData!!.ticks < BAR_UPDATE_BEFORE_TIME + BAR_FLASH_TIME) {
                data.expGainedData!!.ticksMax = BAR_UPDATE_BEFORE_TIME + BAR_FLASH_TIME + POPUP_TIME.total()
                data.expGainedData!!.countOfEvosUnlocked += evoCount
                return
            }

            if (data.evoGainedData != null) {
                data.evoGainedData!!.evolutionCount += evoCount
                data.evoGainedData!!.ticks = 0
            } else {
                data.evoGainedData = newData
            }
        } else {
            overlayData[pokemonUuid] = OverlayData(evoGainedData = newData)
        }
    }

    fun pokemonGainedMoves(pokemonUuid : UUID, movesCount: Int) { //Unused for now I think, until TMs work, as I don't think you can get any other way than level up
        val data = overlayData[pokemonUuid]
        val newData = MovesGainedData(movesCount).also { it.ticksMax = POPUP_TIME.total() }
        if (data != null) {
            if (data.expGainedData != null && data.expGainedData!!.ticks < BAR_UPDATE_BEFORE_TIME + BAR_FLASH_TIME) {
                data.expGainedData!!.ticksMax = BAR_UPDATE_BEFORE_TIME + BAR_FLASH_TIME + POPUP_TIME.total()
                data.expGainedData!!.countOfMovesLearned += movesCount
                return
            }

            if (data.movesGainedData != null) {
                data.movesGainedData!!.movesCount += movesCount
                data.evoGainedData!!.ticks = 0
            } else {
                data.movesGainedData = newData
            }
        } else {
            overlayData[pokemonUuid] = OverlayData(movesGainedData = newData)
        }
    }

    private fun getCurrentRenderTarget(uuid: UUID): RenderTarget {
        val data = overlayData[uuid] ?: return RenderTarget.NONE
        if (data.expGainedData != null) return RenderTarget.EXP_GAINED
        if (data.evoGainedData != null) return RenderTarget.EVOLUTION
        if (data.movesGainedData != null) return RenderTarget.MOVES

        return RenderTarget.NONE
    }

    fun getExpGainedData(uuid: UUID): ExpGainedData? {
        if (getCurrentRenderTarget(uuid) != RenderTarget.EXP_GAINED) return null
        return overlayData[uuid]?.expGainedData
    }

    fun getEvolutionData(uuid: UUID): EvolutionGainedData? {
        if (getCurrentRenderTarget(uuid) != RenderTarget.EVOLUTION) return null
        return overlayData[uuid]?.evoGainedData
    }

    fun getMovesData(uuid: UUID): MovesGainedData? {
        if (getCurrentRenderTarget(uuid) != RenderTarget.MOVES) return null
        return overlayData[uuid]?.movesGainedData
    }

    fun tick(paused: Boolean) {
        val deletion = mutableListOf<UUID>()
        if (paused) return
        overlayData.forEach { (id, data) ->
            if (data.expGainedData != null) { // We update this in order and render these in order, and as such we don't tick others
                val expGainedData = data.expGainedData!!
                expGainedData.ticks += 1
                if (expGainedData.ticks == BAR_UPDATE_BEFORE_TIME && expGainedData.oldLevel != null) {
                    stopSound(levelUpJingleSound)
                    levelUpJingleSound = playSound(CobblemonSounds.LEVELUP)
                }
                if (expGainedData.ticks >= expGainedData.ticksMax) {
                    data.expGainedData = null
                }
            } else {
                if (data.evoGainedData != null) {
                    val evoGainedData = data.evoGainedData!!
                    evoGainedData.ticks += 1
                    if (evoGainedData.ticks >= evoGainedData.ticksMax) {
                        data.evoGainedData = null
                    }
                }
                if (data.movesGainedData != null) {
                    val movesGainedData = data.movesGainedData!!
                    movesGainedData.ticks += 1
                    if (movesGainedData.ticks >= movesGainedData.ticksMax) {
                        data.movesGainedData = null
                    }
                }
            }
            if (getCurrentRenderTarget(id) == RenderTarget.NONE) deletion.add(id)
        }
        deletion.forEach {
            overlayData.remove(it)
        }
    }

    fun clear() {
        overlayData.clear()
        stopSound(barFillupSound)
        stopSound(levelUpJingleSound)
    }

    open class TickCounter(
        var ticksMax: Int,
        var ticks: Int = 0
    )

    class ExpGainedData (
        var oldLevel: Int?, //If Null we never leveled up
        var expGained: Int,
        var countOfMovesLearned: Int, //We don't care what moves we learned, just how many
        var countOfEvosUnlocked: Int
    ) : TickCounter(0)

    class EvolutionGainedData (
        var evolutionCount: Int
    ) : TickCounter(0)

    class MovesGainedData (
        var movesCount: Int
    ) : TickCounter(0)

    data class OverlayData(
        var expGainedData: ExpGainedData? = null,
        var evoGainedData: EvolutionGainedData? = null,
        var movesGainedData: MovesGainedData? = null
    )

    data class FadeTimings(
        val fadeIn: Int,
        val hold: Int,
        val fadeOut: Int
    ) {
        fun total(): Int {
            return fadeIn + hold + fadeOut
        }
    }

    //SOUNDS
    private fun playSound(soundEvent: SoundEvent): SoundInstance? {
        if (!PartyOverlay.canRender()) return null
        val soundInstance = SimpleSoundInstance.forUI(soundEvent, 1F)
        Minecraft.getInstance().soundManager.play(soundInstance)
        return soundInstance
    }

    private fun stopSound(soundInstance: SoundInstance?) {
        if (soundInstance != null) Minecraft.getInstance().soundManager.stop(soundInstance)
    }
}