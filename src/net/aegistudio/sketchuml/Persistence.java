package net.aegistudio.sketchuml;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * The object that is capable of loading and saving
 * objects. Could be used in the scenario of loading
 * or saving something.
 * 
 * @author Haoran Luo
 */
public class Persistence<Model> {
	protected JFileChooser initializeFileChooser(Model model) {
		JFileChooser fileChooser = new JFileChooser();
		FileFilter[] fileFilters = getFileFilters();
		for(FileFilter fileFilter : fileFilters)
			fileChooser.addChoosableFileFilter(fileFilter);
		
		// Choose file filter.
		FileFilter previousFilter = getPreviousFormat();
		if(previousFilter != null) fileChooser
			.setFileFilter(previousFilter);
		else if(fileFilters.length > 0)
			fileChooser.setFileFilter(fileFilters[0]);
		
		// Choose previously selected file.
		File root = getPreviousFile();
		if(root != null) {
			if(root.isDirectory())
				fileChooser.setCurrentDirectory(root);
			else {
				fileChooser.setCurrentDirectory(root.getParentFile());
				fileChooser.setSelectedFile(root);
			}
		}
		
		// Update the file chooser font.
		return fileChooser;
	}
	
	/**
	 * @param parent the component to go modal while saving.
	 * @param model the model of the persisting object.
	 * @return true if there's no problem while loading 
	 * object, and false if nothing is opened.
	 */
	public boolean openModelPanel(Component parent, Model model) {
		if(model == null) return false;
		
		// Initialize the file chooser.
		JFileChooser fileChooser = initializeFileChooser(model);
		
		// Pop-up the file open dialog panel. The logic could be
		// much simpler as there's no need to confirm whether to
		// replace a existing file.
		int openStatus = fileChooser.showOpenDialog(parent);
		if(openStatus != JFileChooser.APPROVE_OPTION) return false;
		
		// Validate the file status.
		File chosenFile = fileChooser.getSelectedFile();
		if(chosenFile == null) return false;
		
		// Attempt to perform open process.
		try {
			open(model, chosenFile, fileChooser.getFileFilter());
			setPreviousStatus(chosenFile, fileChooser.getFileFilter());
		}
		catch(IOException e) {
			e.printStackTrace();
			JOptionPane.showConfirmDialog(fileChooser, 
					"Cannot open file " + chosenFile.getAbsolutePath() 
					+ ":\n" + e.getMessage(), "Error opening file",
					JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		return true;
	}
	
	public boolean saveModelPanel(Component parent, Model model) {
		if(model == null) return false;

		// Initialize the file chooser.
		JFileChooser fileChooser = initializeFileChooser(model);
		
		// Loop the process of popping up save panel.
		File chosenFile = null;
		while(true) {
			// Pop-up the saving panel.
			int saveStatus = fileChooser.showSaveDialog(parent);
			if(saveStatus != JFileChooser.APPROVE_OPTION) return false;
			chosenFile = fileChooser.getSelectedFile();
			
			// Check whether the file already exists can will be 
			// overwritten (user option).
			if(chosenFile.exists()) {
				int overwriteStatus = JOptionPane.showConfirmDialog(fileChooser, 
						"The file " + chosenFile.getAbsolutePath() + 
						"\nalready exists, would you like to overwrite?",
						"Overwriting file", JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if(overwriteStatus == JOptionPane.YES_OPTION) break;
			}
			else break;
		}
		if(chosenFile == null) return false;
		
		// Attempt to perform save process.
		try {
			save(model, chosenFile, fileChooser.getFileFilter());
			setPreviousStatus(chosenFile, fileChooser.getFileFilter());
		}
		catch(IOException e) {
			e.printStackTrace();
			JOptionPane.showConfirmDialog(fileChooser, 
					"Cannot save file " + chosenFile.getAbsolutePath() 
					+ ":\n" + e.getMessage(), "Error saving file",
					JOptionPane.YES_OPTION, JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		return true;
	}
	
	public FileFilter[] getFileFilters() {
		return new FileFilter[0];
	}
	
	protected void setPreviousStatus(File parentPath, FileFilter fileFilter) {
		
	}
	
	protected File getPreviousFile() {
		return null;
	}
	
	protected FileFilter getPreviousFormat() {
		return null;
	}
	
	public void open(Model model, File modelFile, 
			FileFilter modelFormat) throws IOException {
		throw new IOException("Not yet implemented!");
	}
	
	public void save(Model model, File modelFile,
			FileFilter modelFormat) throws IOException {
		throw new IOException("Not yet implemented!");
	}
}
