package net.aegistudio.sketchuml.astaxpt;

import JP.co.esm.caddies.golf.model.EntityRoot;
import JP.co.esm.caddies.jomt.jmodel.FramePresentation;
import JP.co.esm.caddies.jomt.jmodel.ModelManageInfo;

/**
 * Could be used in the Astah persistence class to export an
 * Astah project constructed from this interface.
 * 
 * @author Haoran Luo
 */
public interface AstahExportable {
	/**
	 * @return the model manage information for this project.
	 */
	public ModelManageInfo getMMI();
	
	/**
	 * @return the entity root for this project.
	 */
	public EntityRoot getRoot();
	
	/**
	 * @return the frame for this project.
	 */
	public FramePresentation getFrame();
}
