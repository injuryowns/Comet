# Comet (Fabric, Minecraft 1.21.11)

## Build the jar
1. Install JDK 21 (Temurin/Adoptium is fine) and make sure `java -version` says 21.
2. In this folder run:
   - Windows:  gradlew.bat build
   - Mac/Linux: ./gradlew build
   (first run downloads Gradle, Minecraft and mappings, so it takes a few minutes)
3. Your jar is in build/libs/Comet-1.0.0.jar  (ignore the -sources jar)

## Install
- Install Fabric Loader 0.19.x for 1.21.11 (fabricmc.net/use).
- Put Comet-1.0.0.jar AND Fabric API 0.141.6+1.21.11 in .minecraft/mods.
- In game: Right Shift opens the GUI.

## Test without installing
gradlew runClient
