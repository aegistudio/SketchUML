package net.aegistudio.sketchuml;

import java.awt.Color;
import java.awt.Font;

/**
 * Indicates how will an object or a link be rendered in the 
 * sketch and in the exported PNG/SVG, etc.
 * 
 * @author Haoran Luo
 */
public class SketchRenderHint {
	/**
	 * The user given or system given name of this render style.
	 */
	public String name;
	
	/**
	 * The color of an object outline or a link while it is
	 * not selected by the user. The label over the link or
	 * text around the object should also be such color.
	 */
	public Color lineColorNormal;
	
	/**
	 * The color of an object's background while it is not
	 * selected by the user.
	 */
	public Color fillColorNormal;
	
	/**
	 * The color of an object outline or a link while it is
	 * marked selected. The label over the link or the text
	 * around the object should also be such color while 
	 * selected. So as the control points.
	 */
	public Color lineColorSelected;
	
	/**
	 * The color of an object's background while it is
	 * selected by the user.
	 */
	public Color fillColorSelected;
	
	/**
	 * The color of an arrow's background while it is
	 * not selected by the user.
	 */
	public Color arrowColorNormal;
	
	/**
	 * The color of an arrow's background while it is 
	 * selected by the user.
	 */
	public Color arrowColorSelected;
	
	/**
	 * The color of the user input stroke.
	 */
	public Color userColor;
	
	/**
	 * The width of an object outline or a link.
	 */
	public float outlineWidth;

	/**
	 * The width of a path when it is selected. Not applicable
	 * to the object and label.
	 */
	public float lineWidthSelected;
	
	/**
	 * The width of a line inside the object.
	 */
	public float inlineWidth;
	
	/**
	 * The width of the user input stroke.
	 */
	public float userWidth;
	
	/**
	 * The font of object label, link label and text around 
	 * the object.
	 */
	public Font labelFont;
}
