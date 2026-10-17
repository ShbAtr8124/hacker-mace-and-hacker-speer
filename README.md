# Mafia Hacker Mod — Minecraft Java 26.3 (Fabric)

This is a starter source project, not a finished compiled `.jar`.

## Included
- Mod ID: `mafia_hacker_mod`
- Starter registrations for `hacker_mace` and `hacker_spear`
- Fabric 26.3 project configuration
- TODOs for recipes, special abilities, item textures/models, and `/opitems`

## Intended recipes
- Hacker Mace: diamond spear + golden apple + iron ingot
- Hacker Spear: netherrack + rotten flesh + diamond + 2 sticks

The exact 3x3 crafting layout still needs to be defined.

## Build
1. Install JDK 25 or newer.
2. Open this folder in IntelliJ IDEA as a Gradle project.
3. Let Gradle sync.
4. Run `gradlew.bat build` on Windows.
5. If successful, the JAR will be in `build/libs`.

## Important compatibility note
Minecraft 26.3 changed several modding APIs. This starter source has not been built or tested in this environment. If Gradle or compilation reports API/version errors, use the official Fabric 26.3 template as the baseline and port these names into it:
https://fabricmc.net/develop/template/

Official 26.3 Fabric notes:
https://fabricmc.net/2026/09/15/263.html
