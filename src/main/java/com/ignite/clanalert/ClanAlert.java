package com.ignite.clanalert;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClanAlert implements ModInitializer {
    public static final String MOD_ID = "clanalert";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static String webhookUrl = "";
    private static boolean isModMuted = false;

    @Override
    public void onInitialize() {
        LOGGER.info("ClanAlert initializing dynamic client commands...");
        loadOrCreateConfig();

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            
            // New Command Structure: /sos <realmName>
            // StringArgumentType.greedyString() allows multiple words (e.g., /sos Desert Outpost)
            dispatcher.register(ClientCommandManager.literal("sos")
                .then(ClientCommandManager.argument("realmName", StringArgumentType.greedyString())
                    .executes(context -> {
                        // Extract whatever text the player typed after /sos
                        String customRealm = StringArgumentType.getString(context, "realmName");
                        
                        sendWebhookAlert(customRealm);
                        context.getSource().sendFeedback(Component.literal("§7[ClanAlert] §cSent SOS alert for the §e" + customRealm + "§c Realm!"));
                        return 1;
                    })
                )
                // If they type just /sos without a name, give them a helpful reminder hint
                .executes(context -> {
                    context.getSource().sendFeedback(Component.literal("§7[ClanAlert] §6Usage: /sos <realm name> (Example: /sos Alpha)"));
                    return 0;
                })
            );

            // Command: /clantoggle
            dispatcher.register(ClientCommandManager.literal("clantoggle")
                .executes(context -> {
                    isModMuted = !isModMuted;
                    String status = isModMuted ? "§cMuted (Asleep)" : "§aActive (Awake)";
                    context.getSource().sendFeedback(Component.literal("§7[ClanAlert] §6Mod status: " + status));
                    return 1;
                })
            );
        });
    }

    private void loadOrCreateConfig() {
        try {
            Path configDirPath = FabricLoader.getInstance().getConfigDir();
            File configFile = configDirPath.resolve("clanalert.txt").toFile();

            if (!configFile.getParentFile().exists()) {
                configFile.getParentFile().mkdirs();
            }

            if (!configFile.exists()) {
                Files.writeString(configFile.toPath(), "PASTE_YOUR_DISCORD_WEBHOOK_URL_HERE");
                webhookUrl = "PASTE_YOUR_DISCORD_WEBHOOK_URL_HERE";
                LOGGER.info("Created configuration system file.");
            } else {
                webhookUrl = Files.readString(configFile.toPath()).trim();
                LOGGER.info("Configuration parameters fully linked.");
            }
        } catch (Exception e) {
            LOGGER.error("Failed managing configuration profiles!", e);
        }
    }

    private void sendWebhookAlert(String realm) {
        if (isModMuted) return;
        
        if (webhookUrl.isEmpty() || webhookUrl.contains("PASTE_YOUR_DISCORD_WEBHOOK_URL_HERE")) {
            LOGGER.warn("Webhook aborted: Discord link missing from config file!");
            return;
        }

        new Thread(() -> {
            try {
                URL url = new URL(webhookUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // This formats the dynamic realm name into your Discord message block
                String jsonPayload = "{\"content\": \"🚨 **CLAN WAR ALERT** 🚨\\n@everyone - Urgent reinforcement needed in the **" + realm + "** Realm! Log in now!\"}";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 204 || responseCode == 200) {
                    LOGGER.info("Successfully fired network payload data.");
                } else {
                    LOGGER.error("Discord rejected transmission. HTTP Code: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                LOGGER.error("Fatal network error connecting to Webhook channel backend!", e);
            }
        }).start();
    }
}
