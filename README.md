# RefinedRedstone
Refined Redstone mod for Minecraft

# Download
Once a version will be released as jar-file, you will be able to find all releases [here](https://github.com/Okaghana/RefinedRedstone/releases)

# How to Configure your IDE for contributing
## Installation
- Download the Repository
- Open the folder and run 
    - `gradlew genIntellijRuns` for IntelliJ
    - `gradlew genEclipseRuns` for Eclipse
    - This should download and load all required files and configure the Project for your IDE
- If you should not be able to run the Project (which would open a new Minecraft windows with
  Forge and the mod installed), you might need to restart your Editor

## Exporting as a JAR
To export the mod as a JAR you need to run `gradlew build` in the root folder of the project.
The .jar can be found in /build/libs
