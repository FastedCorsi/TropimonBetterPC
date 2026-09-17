package fr.tropimon.betterpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EmbeddedUpdaterInstallerTest {
  @TempDir Path temporaryDirectory;

  @Test
  void replacesTheJarAfterMinecraftStopsAndKeepsABackup() throws Exception {
    Assumptions.assumeTrue(System.getProperty("os.name", "").startsWith("Windows"));

    Path instance = temporaryDirectory.resolve("instance [literal]");
    Path mods = Files.createDirectories(instance.resolve("mods"));
    Path updateDirectory = Files.createDirectories(temporaryDirectory.resolve("updates"));
    Path target = mods.resolve("TropimonBetterPC-0.9.8+1.21.1.jar");
    Path staged = updateDirectory.resolve("TropimonBetterPC-0.9.9+1.21.1.jar");
    Files.writeString(target, "old", StandardCharsets.UTF_8);
    Files.writeString(staged, "new", StandardCharsets.UTF_8);

    Field installerField = TropimonSelfUpdater.class.getDeclaredField("WINDOWS_INSTALLER");
    installerField.setAccessible(true);
    Path installer = updateDirectory.resolve("install-after-minecraft.ps1");
    Files.writeString(installer, (String) installerField.get(null), StandardCharsets.UTF_8);

    Process process = new ProcessBuilder(
            "powershell.exe", "-NoProfile", "-NonInteractive", "-ExecutionPolicy", "Bypass",
            "-File", installer.toString(), "-ParentPid", "999999", "-Staged", staged.toString(),
            "-Target", target.toString(), "-ExpectedOldHash", sha256(target), "-NewHash",
            sha256(staged), "-ModId", "tropimon_better_pc")
        .redirectErrorStream(true)
        .redirectOutput(updateDirectory.resolve("install.log").toFile())
        .start();

    assertTrue(process.waitFor(10, TimeUnit.SECONDS), "the deferred installer must terminate");
    String installerOutput = new String(
        Files.readAllBytes(updateDirectory.resolve("install.log")), StandardCharsets.ISO_8859_1);
    assertEquals(0, process.exitValue(), installerOutput);
    assertEquals("new", Files.readString(target));
    assertFalse(Files.exists(staged));

    Path backupDirectory = instance.resolve("tropimon-updater-backups");
    try (var backups = Files.list(backupDirectory)) {
      Path backup = backups.findFirst().orElseThrow();
      assertEquals("old", Files.readString(backup));
    }
  }

  private static String sha256(Path file) throws Exception {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    digest.update(Files.readAllBytes(file));
    return HexFormat.of().formatHex(digest.digest());
  }
}
