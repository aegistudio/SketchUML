package net.aegistudio.sketchuml.abstraction;

import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;

/**
 * Indicates how will an object or a link be rendered in the 
 * sketch and in the exported PNG/SVG, etc.
 * 
 * @author Haoran Luo
 */
public class SketchRenderHint {
	/**
	 * The rendering hints defined in the AWT.
	 */
	public RenderingHints awtRenderingHints;
	
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
	
	/**
	 * More verbose colors besides the given usually used color.
	 * The fall-back color will be the commonly used color 
	 * according to which function is invoked.
	 */
	public final Map<String, Color> colorMap = new HashMap<>();
	
	/**
	 * Works just the same as the color map, however the fallback
	 * will first be the normal color map, then the usually used
	 * color.
	 */
	public final Map<String, Color> selectedColorMap = new HashMap<>();
	
	private Color getMapColor(String colorKey, boolean selected) {
		if(selected) {
			if(selectedColorMap.containsKey(colorKey))
				return selectedColorMap.get(colorKey);
		}
		return colorMap.get(colorKey);
	}
	
	public Color getFillColor(String colorKey, boolean selected) {
		Color fillColor = getMapColor(colorKey, selected);
		return fillColor != null? fillColor : 
			(selected? fillColorSelected : fillColorNormal);
	}
	
	public Color getLineColor(String colorKey, boolean selected) {
		Color lineColor = getMapColor(colorKey, selected);
		return lineColor != null? lineColor : 
			(selected? lineColorSelected : lineColorNormal);
	}
	
	/**
	 * The color of object label or text that is outside the
	 * object. You could append.
	 */
	public static final String outerLabelColor = "color.outerLabel";
	
	/**
	 * The color of text or label that is inside the object.
	 */
	public static final String innerLabelColor = "color.innerLabel";
	
	/**
	 * For those who wants to bypass the current path's line color, set 
	 * this to selected color or default color.
	 */
	public static final String pathColor = "color.pathColor";
	
	/**
	 * For those who wants to bypass the current arrow's fill color, set 
	 * this to selected color or default color.
	 */
	public static final String arrowFillcolor = "color.arrowFill";
}
