package com.cobblemon.mod.common.mixin.compatibility;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.pokemon.Species;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Compatibility shim for CobGyms 1.6 (compiled against Cobblemon 1.6) running on Cobblemon 1.7.
 * <p>
 * CobGyms invokes {@code PokemonSpecies#getByIdentifier(ResourceLocation)} as an instance method, but in
 * Cobblemon 1.7 that method is exposed statically. The JVM therefore raises an {@link IncompatibleClassChangeError}
 * during the call site resolution. This mixin intercepts the call and redirects it to the static method to
 * restore compatibility without altering Cobblemon's public API.
 */
@Pseudo
@Mixin(targets = "net.gensir.cobgyms.util.PokemonUtils", remap = false)
public abstract class CobgymsPokemonUtilsMixin {

    @Unique
    private static boolean cobblemon$cobgymsLogged;

    @Redirect(
            method = "getEvolvedPokemonFromSpecies",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/cobblemon/mod/common/api/pokemon/PokemonSpecies;getByIdentifier(Lnet/minecraft/resources/ResourceLocation;)Lcom/cobblemon/mod/common/pokemon/Species;"
            )
    )
    private static Species cobblemon$redirectSpeciesLookup(PokemonSpecies ignored, ResourceLocation identifier) {
        Species species = PokemonSpecies.getByIdentifier(identifier);

        if (!cobblemon$cobgymsLogged) {
            Cobblemon.LOGGER.warn(
                    "Applied CobGyms compatibility shim for {}: redirected PokemonSpecies#getByIdentifier invoked from CobGyms."
                            + " Other mods calling the static method are unaffected.",
                    identifier
            );
            cobblemon$cobgymsLogged = true;
        }

        return species;
    }
}
