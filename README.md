# Recaf plugin workspace

This is a sample maven workspace for creating plugins for Recaf `2.X`.

## Plugin documentation

Official documentation is on hold until Recaf `2.0.0` is fully released.

In the meantime, check this blogpost on using the plugin API: [Creating custom plugins](https://coley.software/recaf-creating-custom-plugins/)

## Building & modification

Once you've downloaded or cloned the repository, you can compile with `mvn clean package`. 
This will generate the file `target/plugin-{VERSION}.jar`. To add your plugin to Recaf:

1. Navigate to the `plugins` folder.
    - Windows: `%HOMEPATH%/Recaf/plugins`
	- Linux: `$HOME/Recaf/plugins`
2. Copy your plugin jar into this folder
3. Run Recaf to verify your plugin loads.