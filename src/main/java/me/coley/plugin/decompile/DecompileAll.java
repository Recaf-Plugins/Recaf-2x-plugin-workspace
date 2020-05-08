package me.coley.plugin.decompile;

import javafx.scene.control.ContextMenu;
import me.coley.recaf.control.Controller;
import me.coley.recaf.control.gui.GuiController;
import me.coley.recaf.decompile.Decompiler;
import me.coley.recaf.ui.ContextBuilder;
import me.coley.recaf.ui.controls.ActionMenuItem;
import me.coley.recaf.workspace.JavaResource;
import org.plugface.core.annotations.Plugin;
import me.coley.recaf.plugin.api.*;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Plugin(name = "Decompile All")
public class DecompileAll implements StartupPlugin, ContextMenuInjectorPlugin {
	private Controller controller;

	@Override
	public String getVersion() {
		return "1.0.0";
	}

	@Override
	public String getDescription() {
		return "Provides an decompile export function on selected classes/packages/resources";
	}

	@Override
	public void onStart(Controller controller) {
		this.controller =controller;
	}

	@Override
	public void forPackage(ContextBuilder builder, ContextMenu menu, String name) {
		menu.getItems().add(new ActionMenuItem("Decompile package",
				() -> decompile(Pattern.quote(name) + "/.*", builder.getResource())));
	}

	@Override
	public void forClass(ContextBuilder builder, ContextMenu menu, String name) {
		menu.getItems().add(new ActionMenuItem("Decompile class",
				() -> decompile(Pattern.quote(name), builder.getResource())));
	}

	@Override
	public void forResourceRoot(ContextBuilder builder, ContextMenu menu, JavaResource resource) {
		menu.getItems().add(new ActionMenuItem("Decompile all",
				() -> decompile(".*", resource)));
	}

	private void decompile(String namePattern, JavaResource resource) {
		Set<String> matchedNames = resource.getClasses().keySet().stream()
				.filter(name -> name.matches(namePattern))
				.collect(Collectors.toSet());
		new DecompilePanel((GuiController) controller, new TreeSet<>(matchedNames));
	}
}