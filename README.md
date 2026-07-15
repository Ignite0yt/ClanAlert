# ClanAlert (Minecraft Fabric 1.21.1)

ClanAlert is a lightweight, client-side utility mod built for Minecraft 1.21.1 using the Fabric loader. It allows competitive faction, clan, and anarchy players to send instant emergency panic pings directly to a specified Discord channel via an in-game command.

## Features
* **Instant Dynamic Alerts:** Type `/sos <message>` in-game to instantly ping your team.
* **Asynchronous Threading:** Designed from scratch to fire webhooks instantly without causing any in-game frame drops or lag spikes.
* **100% Client-Side:** No server installation needed. It works flawlessly on any server you join.
* **Simple Local Config:** Safely reads your team's Discord Webhook URL from a local `config/clanalert.txt` file.

## Installation & Configuration
1. Download the latest `.jar` release from **Modrinth**.
2. Drop the file into your `.minecraft/mods` folder alongside **Fabric API**.
3. Launch Minecraft 1.21.1 once to automatically generate the config file.
4. Navigate to `.minecraft/config/clanalert.txt` and paste your Discord Webhook URL inside it.
5. In-game, use the command: `/sos <your message here>`

## License
This project is licensed under the MIT License - see the LICENSE file for details.
