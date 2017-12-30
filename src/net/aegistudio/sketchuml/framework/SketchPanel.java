package net.aegistudio.sketchuml.framework;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.util.Enumeration;
import java.util.Vector;import javax.swing.JComponent;

import de.dubs.dollarn.PointR;
import net.aegistudio.sketchuml.Configuration;
import net.aegistudio.sketchuml.EntityEntry;
import net.aegistudio.sketchuml.SketchView;

public class SketchPanel extends JComponent implements 
	MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
	
	private static final long serialVersionUID = 1L;
	private final SketchModel model;
	
	public SketchPanel(SketchModel model) {
		this.model = model;
		model.connect(this, this::repaint);
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
	}
	
	// Current stroke painting points.
	private Vector<PointR> points = new Vector<PointR>();
	
	// Current pass of stroke paintings.
	private Vector<Vector<PointR>> strokes = new Vector<Vector<PointR>>();

	private boolean leftMouseDown(MouseEvent e) {
		return 0 != (e.getModifiersEx() & 
				MouseEvent.BUTTON1_DOWN_MASK);
	}
	
	private boolean rightMouseDown(MouseEvent e) {
		return 0 != (e.getModifiersEx() & 
				MouseEvent.BUTTON3_DOWN_MASK);
	}
	
	private int initMouseX, initMouseY;
	private float zoomMultiplier = 1.0f;
	
	@Override
	public void mouseDragged(MouseEvent arg0) {
		SketchEntityComponent selected;
		// Left mouse button down, then regard it as stroke input.
		if(leftMouseDown(arg0)) {
			// Clear previous candidates.
			candidate = null; candidates = null;
			updateCandidateObject();
			model.selectComponent(null, null);
			
			Point point = arg0.getPoint();
			points.add(new PointR(point.x, point.y));
			repaint();
		}
		
		// Right mouse button down.
		if(rightMouseDown(arg0) && (selected = model.getSelected()) != null) {
			SketchEntityComponent init = model.getSelectedOriginal();
			selected.x = init.x + (arg0.getX() - initMouseX);
			selected.y = init.y + (arg0.getY() - initMouseY);
			model.notifySelectedChanged(null);
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		
	}

	private void focusSelected() {
		zoomMultiplier = 1.0f;
	}
	
	@Override
	public void mousePressed(MouseEvent arg0) {
		this.requestFocusInWindow();
		
		// Initial editing parameters when right clicked.
		if(arg0.getButton() == MouseEvent.BUTTON3) {
			model.selectComponent(null, null);
			SketchEntityComponent toSelect = 
					model.componentAt(arg0.getX(), arg0.getY());
			
			model.selectComponent(null, toSelect);
			if(toSelect == null) return;
			focusSelected();
			initMouseX = arg0.getX(); initMouseY = arg0.getY();
		}
	}

	public EntityEntry[] candidates = null;
	public int candidateIndex = 0;
	public Runnable candidateNotifier = null;
	private int boxX, boxY, boxW, boxH;
	private SketchEntityComponent candidate = null;
	
	public void selectCandidate(int index) {
		candidateIndex = index;
		updateCandidateObject();
		repaint();
	}

	private void resetInputState() {
		candidate = null;
		candidates = null;
		points.clear();
		strokes.clear();
		
		if(candidateNotifier != null)
			candidateNotifier.run(); 
	}
	
	private void updateCandidateObject() {
		if(candidates != null) {
			candidate = new SketchEntityComponent(
					candidates[candidateIndex], 
					candidates[candidateIndex].factory.create());
			candidate.x = boxX;	candidate.y = boxY;
			candidate.w = boxW;	candidate.h = boxH;
		}
		
		if(candidateNotifier != null)
			candidateNotifier.run(); 
	}
	
	private void performRecognition() {
		Vector<PointR> allPoints = new Vector<PointR>();
		Enumeration<Vector<PointR>> en = strokes.elements();
		while (en.hasMoreElements()) {
			Vector<PointR> pts = en.nextElement();
			allPoints.addAll(pts);
		}
		
		// Recognize input stroke.
		candidates = model.getRecognizer().recognize(allPoints, strokes.size());
		if(candidates == null) return;
		if(candidates.length == 0) { candidates = null; return; }
		
		// Find the boundary of points.
		int minX = (int)allPoints.get(0).X; 
		int minY = (int)allPoints.get(0).Y;
		int maxX = minX, maxY = minY;
		for(int i = 1; i < allPoints.size(); ++ i) {
			PointR current = allPoints.get(i);
			int x = (int)current.X; int y = (int)current.Y;
			minX = Math.min(x, minX); maxX = Math.max(x, maxX);
			minY = Math.min(y, minY); maxY = Math.max(y, maxY);
		}
		
		boxX = minX;		boxY = minY;
		boxW = maxX - minX;	boxH = maxY - minY;
		
		// Render the first candidate.
		candidateIndex = 0;
		updateCandidateObject();
	}
	
	@Override
	public void mouseReleased(MouseEvent arg0) {
		SketchEntityComponent selected = model.getSelected();
		// Left button for stroke drawing.
		if(arg0.getButton() == MouseEvent.BUTTON1) {
			// Clear previous candidates.
			model.selectComponent(null, null);
			candidate = null; candidates = null;
			
			// Transport the points to the troke.
			if (points.size() > 1) 
				strokes.add(new Vector<PointR>(points));
			points.clear();
			
			// Judge whether to perform soon recognizing.
			if(strokes.size() > 0) {
				if(Configuration.getInstance().INSTANT_RECOGNIZE)
					performRecognition();
			}
			
			repaint();
		}
		
		else if(arg0.getButton() == MouseEvent.BUTTON3) {
			
			// Right mouse for stroke recognizing.
			if(candidates == null && strokes.size() > 0) {
				if(!Configuration.getInstance().INSTANT_RECOGNIZE)
					performRecognition();
				repaint();
			}
			
			// Right mouse for result confirmation.
			else if(candidate != null) {
				model.create(null, candidate);
				model.selectComponent(null, candidate);
				focusSelected();
				resetInputState();
				repaint();
			}
			
			// Right mouse for location confirmation.
			else if(selected != null) {
				// Partial confirmation.
				model.selectComponent(null, null);
				model.selectComponent(null, selected);
				focusSelected();
				repaint();
			}
		}
		
		// Middle mouse button for reset.
		else if(arg0.getButton() == MouseEvent.BUTTON2) {
			resetInputState();
			repaint();
		}
	}
	
	private void drawStroke(Graphics2D g2d, Vector<PointR> pts) {
		for (int i = 0; i < (pts.size() - 1); ++i) {
			g2d.setColor(Color.BLACK);
			g2d.drawLine((int) pts.elementAt(i).X,
					(int) pts.elementAt(i).Y, 
					(int) pts.elementAt(i + 1).X,
					(int) pts.elementAt(i + 1).Y);
		}
	}
	
	private void paintSketchComponent(Graphics g, 
			SketchEntityComponent current, boolean preview) {
		
		// The concrete part of the rendering object.
		Graphics currentGraphics = g.create(
				current.x, current.y, current.w, current.h);
		current.entry.sketchView.renderEntity(
				currentGraphics, current.entity, preview);
		
		// The overlaying part of the rendering object.
		g.setColor(preview? Color.GRAY : Color.BLACK);
		Rectangle2D boundObject = new Rectangle2D.Double(
				current.x, current.y, current.w, current.h);
		for(SketchView.OverlayDirection direction 
				: SketchView.OverlayDirection.values()) {
			// Ensure there's overlaying text.
			String overlayText = current.entry.sketchView
					.overlayEntity(current.entity, direction);
			if(overlayText == null) continue;
			
			// Calculate bound and draw location.
			Rectangle2D boundText = g.getFontMetrics()
					.getStringBounds(overlayText, g);
			Point position = direction.getLocation(
					boundText, boundObject);
			
			// Render the overlay text.
			g.drawString(overlayText, position.x, position.y);
		}
	}
	
	@Override
	public void paint(Graphics g) {
		SketchEntityComponent selected = model.getSelected();
		
		g.setFont(Configuration.getInstance().HANDWRITING_FONT);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		Graphics2D g2d = (Graphics2D) g;
		
		// Render the objects in order.
		for(int i = model.numComponents() - 1; i >= 0; -- i) {
			SketchEntityComponent current = model.get(i);
			
			// Selection box if the object is selected.
			if(current == selected) {
				g.setColor(Color.LIGHT_GRAY);
				g.fillRect(selected.x, selected.y, 
						selected.w, selected.h);
			}
			
			// Default object component.
			paintSketchComponent(g, current, false);
		}
		
		// Render the candidate object.
		if(candidate != null) paintSketchComponent(g, candidate, true);
		
		// Render the newly painting stroke.
		g2d.setStroke(new BasicStroke(2));
		g2d.setColor(Color.BLACK);
		Enumeration<Vector<PointR>> en = strokes.elements();
		while (en.hasMoreElements()) {
			Vector<PointR> pts = en.nextElement();
			drawStroke(g2d, pts);
		}
		if(!(points.size() < 2)) drawStroke(g2d, points);
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		SketchEntityComponent selected = model.getSelected();
		if(candidates != null && candidates.length > 0) {
			model.selectComponent(null, null);
			int base =  Math.min(Configuration.getInstance()
					.MAX_CANDIDATE, candidates.length);
			// Modulus rotation of mouse wheel.
			candidateIndex = (candidateIndex + 
					(arg0.getWheelRotation() > 0? 1 : -1)) % base;
			if(candidateIndex < 0) candidateIndex += base;
			
			// Generate new preview object.
			updateCandidateObject();
			repaint();
		}
		else if(selected != null){
			SketchEntityComponent init = model.getSelectedOriginal();
			zoomMultiplier += arg0.getWheelRotation() > 0? -0.1 : +0.1;
			if(zoomMultiplier < 0) zoomMultiplier = 
					(float)Math.max(1.0 / init.w, 1.0 / init.h);
			
			selected.w = (int)(zoomMultiplier * init.w);
			selected.h = (int)(zoomMultiplier * init.h);
			selected.x = (int)(init.x + init.w / 2 - zoomMultiplier / 2 * init.w);
			selected.y = (int)(init.y + init.h / 2 - zoomMultiplier / 2 * init.h);
			model.notifySelectedChanged(null);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		SketchEntityComponent selected = model.getSelected();
		// Select the candidate via keyboard input.
		if(candidates != null && candidates.length > 0) {
			int keyIndex = e.getKeyCode() - KeyEvent.VK_1;
			if(keyIndex >= 0 && keyIndex < candidates.length) {
				candidateIndex = keyIndex;
				updateCandidateObject();
				repaint();
			}
		}
		
		// Remove the selected object if any.
		else if(selected != null && e.getKeyCode() == KeyEvent.VK_DELETE)
			model.destroy(null, selected);
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		
	}
}
