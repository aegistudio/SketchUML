package net.aegistudio.sketchuml;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

/**
 * The view that enable users to see the object from the sketch.
 * 
 * @author Haoran Luo
 */
public interface SketchView {
	/**
	 * @param hint the sketch render hint.
	 * @param g the graphic object to render object.
	 * @param entity the entities' data model.
	 * @param preview is object previewing.
	 */
	public void renderEntity(SketchRenderHint hint,
			Graphics g, Entity entity, boolean preview);
	
	/**
	 * Indicate where to show the overlaying text.
	 */
	public enum OverlayDirection {
		UP {
			@Override
			public Point getLocation(Rectangle2D boundText, Rectangle2D boundObject) {
				return getLocationMatrix(boundText, boundObject, -0.5, 0.5, 0, 0);
			}
		}, DOWN {
			@Override
			public Point getLocation(Rectangle2D boundText, Rectangle2D boundObject) {
				return getLocationMatrix(boundText, boundObject, -0.5, 0.5, 0.5, 1);
			}
		}, LEFT {
			@Override
			public Point getLocation(Rectangle2D boundText, Rectangle2D boundObject) {
				return getLocationMatrix(boundText, boundObject, -1, 0, 0.25, 0.5);
			}
		}, RIGHT {
			@Override
			public Point getLocation(Rectangle2D boundText, Rectangle2D boundObject) {
				return getLocationMatrix(boundText, boundObject, 0, 1, 0.25, 0.5);
			}
		}, CENTER {
			@Override
			public Point getLocation(Rectangle2D boundText, Rectangle2D boundObject) {
				return getLocationMatrix(boundText, boundObject, -0.5, 0.5, 0.25, 0.5);
			}
		};
		
		public abstract Point getLocation(Rectangle2D boundText, Rectangle2D boundObject);
		
		protected Point getLocationMatrix(Rectangle2D boundText, Rectangle2D boundObject, 
				double widthText, double widthObject, 
				double heightText, double heightObject) {
			int pointX = (int)(widthText * boundText.getWidth() + 
					boundObject.getX() + widthObject * boundObject.getWidth());
			int pointY = (int)(heightText * boundText.getHeight() + 
					boundObject.getY() + heightObject * boundObject.getHeight());
			return new Point(pointX, pointY);
		}
	}
	
	/**
	 * Retrieve the overlay at specific direction.
	 * @param entity the entity to render.
	 * @param old the direction to overlay at.
	 * @return the overlaying text, null for no overlay.
	 */
	public String overlayEntity(Entity entity, OverlayDirection old);
}