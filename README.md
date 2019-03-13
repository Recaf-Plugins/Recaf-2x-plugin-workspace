# Recaf plugin workspace

This is a sample maven workspace for creating plugins for Recaf. For documentation on creating plugins, check the [plugin development page](https://col-e.github.io/Recaf/plugins.html).

## Building & modification

Once you've downloaded or cloned the repository, you can compile with `mvn package`. This will generate the file `target/plugin-{VERSION}.jar`. To add your plugin to Recaf:

1. Navigate to the `plugins` folder.
2. Copy your jar into this folder
3. Run Recaf to verify your plugin loads.

### Examples

See the [releases](https://github.com/Col-E/Recaf-plugin-workspace/releases) page for examples based off of this workspace. Outdated examples will be be marked as unsupported in the release description.