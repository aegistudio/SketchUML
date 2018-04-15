package net.aegistudio.sketchuml;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.filechooser.FileFilter;

import net.aegistudio.sketchuml.abstraction.Template;
import net.aegistudio.sketchuml.framework.SketchModel;

public class SketchPersistence<Path> extends 
		Persistence<SketchPersistence.SketchFile<Path>> {
	private final int currentFormatVersion;
	private final List<Template> templates;
	
	public SketchPersistence(int formatVersion, Template[] templates) {
		this.currentFormatVersion = formatVersion;
		this.templates = Arrays.asList(templates);
	}
	
	public static class SketchFile<Path> {
		public int formatVersion;
		
		public Template template;
		
		public SketchModel<Path> sketchModel;
	}
	
	protected SketchModel<Path> newSketchModel(Template template) {
		throw new AssertionError("The sketch model creation method " 
				+ "should be implemented.");
	}
	
	private final FileFilter sketchFileFilter = new FileFilter() {
		@Override
		public boolean accept(File arg0) {
			if(arg0.isDirectory()) return true;
			if(arg0.getName().endsWith(".suml")) return true;
			return false;
		}

		@Override
		public String getDescription() {
			return "SketchUML File (*.suml)";
		}
	};
	
	public FileFilter[] getFileFilters() {
		return new FileFilter[] {sketchFileFilter};
	}
	
	@Override
	public void open(SketchFile<Path> sketch, File file, FileFilter format) 
			throws IOException {
		
		try(FileInputStream fileInputStream = new FileInputStream(file);
			DataInputStream dataInputStream = new DataInputStream(fileInputStream)) {
			
			// Validation of the magic number.
			byte[] magicNumber = new byte[4];
			dataInputStream.read(magicNumber);
			if(	magicNumber[0] != 'S' || magicNumber[1] != 'U' 
			||	magicNumber[2] != 'M' || magicNumber[3] != 'L')
				throw new IOException("Not a SketchUML file.");
			
			// Read version information.
			sketch.formatVersion = dataInputStream.readInt();
			if(sketch.formatVersion != currentFormatVersion) 
				throw new IOException("The file to open is not supported " + 
						"int current version of SketchUML.");
			
			// Retrieve the template information.
			int templateIndex = dataInputStream.readShort();
			if(templateIndex < 0 || templateIndex >= templates.size())
				throw new IOException("The template is not supported.");
			sketch.template = templates.get(templateIndex);
			
			// Create the sketch model and load.
			GZIPInputStream gzipInputStream = new GZIPInputStream(dataInputStream);
			sketch.sketchModel = newSketchModel(sketch.template);
			sketch.sketchModel.loadModel(new DataInputStream(gzipInputStream));
		}
	}
	
	@Override
	public void save(SketchFile<Path> sketch, File file, FileFilter format) 
			throws IOException {
		
		try(FileOutputStream fileOutputStream = new FileOutputStream(file);
			DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream)) {
			
			// Validation of the magic number.
			byte[] magicNumber = new byte[] {'S', 'U', 'M', 'L'};
			dataOutputStream.write(magicNumber);
			
			// Write version information.
			dataOutputStream.writeInt(sketch.formatVersion);
			
			// Write the template index.
			int templateIndex = templates.indexOf(sketch.template);
			if(templateIndex < 0) throw new IOException(
					"The template is not supported.");
			dataOutputStream.writeShort(templateIndex);
			
			// Create the sketch model and load.
			GZIPOutputStream gzipOutputStream = new GZIPOutputStream(dataOutputStream);
			sketch.sketchModel.saveModel(new DataOutputStream(gzipOutputStream));
			gzipOutputStream.finish();
			gzipOutputStream.flush();
		}
	}
}
