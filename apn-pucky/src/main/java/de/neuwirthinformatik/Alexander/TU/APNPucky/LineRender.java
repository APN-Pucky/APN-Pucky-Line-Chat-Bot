package de.neuwirthinformatik.Alexander.TU.APNPucky;

import java.awt.FontFormatException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

import de.neuwirthinformatik.Alexander.TU.Render.Render;

public class LineRender extends Render {
	public LineRender() throws FontFormatException, IOException {
		super();
		// TODO Auto-generated constructor stub
	}

	public static DownloadedContent createTempFile(String ext) {
		String fileName = LocalDateTime.now().toString() + '-' + UUID.randomUUID().toString() + '.' + ext;
		Path tempFile = KitchenSinkApplication.downloadedContentDir.resolve(fileName);
		tempFile.toFile().deleteOnExit();
		return new DownloadedContent(tempFile, KitchenSinkController.createUri("/downloaded/" + tempFile.getFileName()));
	}
}
