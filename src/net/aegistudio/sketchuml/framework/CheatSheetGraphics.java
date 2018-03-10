package net.aegistudio.sketchuml.framework;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class CheatSheetGraphics {
	public final Image image;
	public final int imageWidth, imageHeight;
	
	/** How dark should the image grows. */
	public static final double GRAYSCALE_RATIO = 0.65;
	
	/** The scaled factor of the cheat sheet. */
	public static final double SCALED_RATIO = 1.4;
	
	public CheatSheetGraphics(String language) throws IOException {
		// Attempts to load cheat sheet of certain language.
		InputStream cheatsheetInputStream = getClass()
				.getResourceAsStream("/cheatsheet/" + language + ".png");
		if(cheatsheetInputStream == null) throw new IOException(
				"Cheat sheet resource is not found.");
		
		// Initialize the graphics of the cheat sheet.
		BufferedImage cheatsheetImage = ImageIO.read(cheatsheetInputStream);
		BufferedImage image = new BufferedImage(cheatsheetImage.getWidth(), 
				cheatsheetImage.getHeight(), 
				BufferedImage.TYPE_4BYTE_ABGR);
		WritableRaster raster = image.getRaster();
		WritableRaster sourceRaster = cheatsheetImage.getRaster();
		double[] sample = new double[4]; 
		for(int i = 0; i < image.getWidth(); ++ i)
			for(int j = 0; j < image.getHeight(); ++ j) {
				double value = sourceRaster.getSampleDouble(i, j, 0);
				sample[3] = value > 210.? 0 : Math.min(210, 255. - value);
				sample[0] = sample[1] = sample[2] 
						= 255. - sample[3] * GRAYSCALE_RATIO;
				raster.setPixel(i, j, sample);
			}
		
		// Scale the image and produce the result.
		this.imageWidth = (int)(image.getWidth() * SCALED_RATIO);
		this.imageHeight = (int)(image.getHeight() * SCALED_RATIO);
		this.image = image.getScaledInstance(
				imageWidth, imageHeight, Image.SCALE_SMOOTH);
	}
}
