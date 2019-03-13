package me.yourname.pluginname;

import org.plugface.core.annotations.Plugin;
import me.coley.recaf.plugin.*;

@Plugin(name = "Example")
public class ExamplePlugin implements PluginBase {
    @Override
    public String name() {
        return "Example";
    }

    @Override
    public String version() {
        return "1.0";
    }
    
    @Override
    public void onLoad() {
        // Optional, but useful to show that it loads in the console output
        System.out.println("Hello");
    }
}