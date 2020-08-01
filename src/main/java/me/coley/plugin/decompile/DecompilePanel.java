package me.coley.plugin.decompile;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import me.coley.recaf.control.gui.GuiController;
import me.coley.recaf.decompile.Decompiler;
import me.coley.recaf.ui.controls.ExceptionAlert;
import me.coley.recaf.util.LangUtil;
import me.coley.recaf.util.Log;
import me.coley.recaf.util.UiUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.*;
import java.util.jar.JarEntry;
import java.util.zip.ZipOutputStream;

public class DecompilePanel extends BorderPane {
	private static final Color SUCCESS_COLOR = new Color(0.05, 0.45, 0.05, 1);
	private static final Color FAIL_COLOR = new Color(0.5, 0.05, 0.05, 1);
	private final int classCount;
	private final List<Long> decompileTimes = new CopyOnWriteArrayList<>();
	private final FileChooser saver = new FileChooser();
	private final Map<String, String> classSources = new ConcurrentHashMap<>();
	private final ListView<String> list = new ListView<>();
	private final Label lblEstimatedtime = new Label("Remaining time: Unknown");
	private final ProgressBar progressBar = new ProgressBar(0);
	private final GuiController controller;
	private final Stage window;

	public DecompilePanel(GuiController controller, TreeSet<String> strings) {
		this.controller = controller;
		this.classCount = strings.size();
		list.setItems(FXCollections.observableArrayList(strings));
		list.setCellFactory(e -> new DecompileListCell());
		FileChooser.ExtensionFilter filter = strings.size() > 1 ?
				new FileChooser.ExtensionFilter("Sources", "*.jar", "*.zip", "*.java") :
				new FileChooser.ExtensionFilter("Source", "*.java");
		saver.setTitle(LangUtil.translate("misc.save"));
		saver.getExtensionFilters().add(filter);
		saver.setSelectedExtensionFilter(filter);
		setCenter(list);
		BorderPane bottomInfo = new BorderPane();
		bottomInfo.setCenter(progressBar);
		bottomInfo.setBottom(lblEstimatedtime);
		setBottom(bottomInfo);
		progressBar.setMaxWidth(Double.MAX_VALUE);
		// Show window
		window = controller.windows().window("Decompile export", this);
		window.show();
		// Start decompile
		new Thread(() -> startDecompile(strings)).start();
	}

	private void startDecompile(TreeSet<String> strings) {
		Decompiler<?> decompiler = controller.config().decompile().decompiler.create(controller);
		int nThreads = Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors()));
		ExecutorService service = Executors.newFixedThreadPool(nThreads);
		for (String name : strings)
			service.submit(() -> {
				try {
					long start = System.currentTimeMillis();
					classSources.put(name, decompiler.decompile(name));
					decompileTimes.add(System.currentTimeMillis() - start);
					updateProgressUI();
				} catch (Exception ex) {
					Log.error(ex, "Class failed to decompiled: {}", name);
					classSources.put(name, "// Failed to decompile: " + name);
				}
			});
		service.shutdown();
		try {
			service.awaitTermination(1, TimeUnit.DAYS);
			Platform.runLater(() -> {
				saveSources();
			});
		} catch (InterruptedException e) {
			Log.error("Interrupted decompile service");
		}
	}

	private void updateProgressUI() {
		int done = decompileTimes.size();
		int remaining = classCount - done;
		decompileTimes.stream().mapToLong(Long::longValue)
				.average().ifPresent(avgMillis -> {
			long etaRemaining = (long) (avgMillis * remaining);
			String hms = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(etaRemaining),
					TimeUnit.MILLISECONDS.toMinutes(etaRemaining) % TimeUnit.HOURS.toMinutes(1),
					TimeUnit.MILLISECONDS.toSeconds(etaRemaining) % TimeUnit.MINUTES.toSeconds(1));
			Platform.runLater(() -> {
				lblEstimatedtime.setText("Remaining time: " + hms);
			});
		});
		Platform.runLater(() -> {
			list.refresh();
			progressBar.setProgress(classSources.size() / (double) classCount);
		});
	}

	private void saveSources() {
		saver.setInitialDirectory(controller.config().backend().getRecentSaveAppDir());
		File file = saver.showSaveDialog(null);
		if (file != null) {
			try {
				if (classCount == 1) {
					Files.write(file.toPath(), classSources.get(classSources.keySet().iterator().next()).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
				} else {
					try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {
						for (Map.Entry<String, String> e : classSources.entrySet()) {
							zos.putNextEntry(new JarEntry(e.getKey() + ".java"));
							zos.write(e.getValue().getBytes(StandardCharsets.UTF_8));
							zos.closeEntry();
						}
					}
				}
			} catch (Exception ex) {
				Log.error(ex, "Failed to save sources to file: {}", file.getName());
				ExceptionAlert.show(ex, "Failed to save application to file: " + file.getName());
			}
		}
		// Done, can close
		window.close();
	}

	private class DecompileListCell extends ListCell<String> {
		@Override
		protected void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				setText(item);
				setGraphic(UiUtil.createClassGraphic(controller.getWorkspace().getClassReader(item).getAccess()));
				if (classSources.containsKey(item)) {
					Color color = classSources.get(item) == null ?
							FAIL_COLOR : SUCCESS_COLOR;
					int r = (int) (color.getRed() * 255);
					int g = (int) (color.getGreen() * 255);
					int b = (int) (color.getBlue() * 255);
					setStyle("-fx-background: rgb(" + r + "," + g + "," + b + ")");
				}
			}
		}
	}

	public static double map(double value, double start, double stop, double targetStart, double targetStop) {
		return targetStart + (targetStop - targetStart) * ((value - start) / (stop - start));
	}
}