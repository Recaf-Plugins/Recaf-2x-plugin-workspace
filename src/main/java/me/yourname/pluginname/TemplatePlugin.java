package me.yourname.pluginname;

import org.plugface.core.annotations.Plugin;
import me.coley.recaf.plugin.api.*;

@Plugin(name = "Template")
public class TemplatePlugin implements BasePlugin {
	@Override
	public String getVersion() {
		return "1.0";
	}

	@Override
	public String getDescription() {
		return "A template plugin to work off of";
	}
}