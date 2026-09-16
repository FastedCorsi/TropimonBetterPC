$ErrorActionPreference = 'Stop'
$pcJar = Join-Path $env:APPDATA '.tropimon/mods/Cobblemon-fabric-1.7.2+1.21.1.jar'
& javap -c -classpath $pcJar com.cobblemon.mod.common.client.net.storage.pc.ClosePCHandler com.cobblemon.mod.common.client.net.storage.party.SetPartyReferenceHandler
& javap -classpath $pcJar com.cobblemon.mod.common.net.messages.client.storage.pc.ClosePCPacket com.cobblemon.mod.common.api.pokemon.stats.Stats com.cobblemon.mod.common.client.gui.PokemonGuiUtilsKt
