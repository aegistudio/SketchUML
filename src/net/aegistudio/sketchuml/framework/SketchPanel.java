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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;import javax.swing.JComponent;

import de.dubs.dollarn.PointR;
import net.aegistudio.sketchuml.Configuration;
import net.aegistudio.sketchuml.EntityEntry;
import net.aegistudio.sketchuml.LinkEntry;
import net.aegistudio.sketchuml.SketchView;
import net.aegistudio.sketchuml.path.PathManager;
import net.aegistudio.sketchuml.path.PathView;

public class SketchPanel<Path> extends JComponent implements 
	MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {
	
	private static final long serialVersionUID = 1L;
	private final SketchModel<Path> model;
	private final CandidatePanel candidatePanel;
	private final PathManager<Path> pathManager;
	private final PathView<Path> pathView;
	
	public SketchPanel(CandidatePanel candidatePanel, SketchModel<Path> model, 
			PathManager<Path> pathManager, PathView<Path> pathView) {
		
		this.model = model;
		this.candidatePanel = candidatePanel;
		this.pathManager = pathManager;
		this.pathView = pathView;
		
		model.registerEntityObserver(this, this::repaint);
		model.registerLinkObserver(this, this::repaint);
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
	}
	
	private class ComponentCandidate extends CandidatePanel.CandidateObject {
		private final SketchEntityComponent component;
		public ComponentCandidate(EntityEntry entry) {
			this.component = new SketchEntityComponent(
					entry, entry.factory.get());
			this.color = "yellow";
			this.text = entry.name;
			
			this.scrollAction = () -> {
				component.x = boxX;	component.y = boxY;
				component.w = boxW;	component.h = boxH;
				repaint();
			};
			
			this.confirmAction = () -> {
				model.create(null, component);
				model.selectEntity(null, component);
			};
		}
	}
	
	private class LinkCandidate extends CandidatePanel.CandidateObject {
		private final SketchLinkComponent<Path> component;
		public LinkCandidate(SketchEntityComponent source, 
				SketchEntityComponent destination, 
				Path path, LinkEntry entry) {
			
			this.component = new SketchLinkComponent<Path>(
					source, destination, path);
			this.color = "purple";
			this.text = "Link";
			
			this.component.entry = entry;
			this.component.link = entry.factory.get();
			
			this.scrollAction = () -> { repaint(); };
			this.confirmAction = () -> { model.link(null, component); };
		}
		
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
			candidatePanel.updateCandidates(null);
			model.selectEntity(null, null);
			
			Point point = arg0.getPoint();
			points.add(new PointR(point.x, point.y));
			repaint();
		}
		
		// Right mouse button down.
		if(rightMouseDown(arg0) && (selected = model.getSelectedEntity()) != null) {
			SketchEntityComponent init = model.getOriginalEntity();
			selected.x = init.x + (arg0.getX() - initMouseX);
			selected.y = init.y + (arg0.getY() - initMouseY);
			model.notifyEntityChanged(null);
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
		if(candidatePanel.numCandidates() > 0) return;
		
		// Initial editing parameters when right clicked.
		if(arg0.getButton() == MouseEvent.BUTTON3) {
			model.selectEntity(null, null);
			SketchEntityComponent entityToSelect = 
					model.entityAt(arg0.getX(), arg0.getY());
			
			// Judge whether an entity is already selected.
			if(entityToSelect != null) {
				model.selectEntity(null, entityToSelect);
				focusSelected();
				initMouseX = arg0.getX(); initMouseY = arg0.getY();
				return;
			}
			
			SketchLinkComponent<Path> linkToSelect =
					model.linkAt(arg0.getX(), arg0.getY());
			
			// Judge whether a link is already selected.
			if(linkToSelect != null) {
				model.selectLink(null, linkToSelect);
				return;
			}
			
			// Nothing is selected, the initial selection is enough.
		}
	}

	private int boxX, boxY, boxW, boxH;

	private void resetInputState() {
		points.clear();
		strokes.clear();
		candidatePanel.updateCandidates(null); 
	}
	
	private void performRecognition() {
		List<CandidatePanel.CandidateObject> candidates = new ArrayList<>();
		
		// Find the boundary of points.
		Vector<PointR> allPoints = new Vector<PointR>();
		Enumeration<Vector<PointR>> en = strokes.elements();
		while (en.hasMoreElements()) {
			Vector<PointR> pts = en.nextElement();
			allPoints.addAll(pts);
		}
		
		int minX = allPoints.get(0).intX(); 
		int minY = allPoints.get(0).intY();
		int maxX = minX, maxY = minY;
		for(int i = 1; i < allPoints.size(); ++ i) {
			PointR current = allPoints.get(i);
			minX = Math.min(current.intX(), minX);
			maxX = Math.max(current.intX(), maxX);
			minY = Math.min(current.intY(), minY);
			maxY = Math.max(current.intY(), maxY);
		}
		
		boxX = minX;		boxY = minY;
		boxW = maxX - minX;	boxH = maxY - minY;
		
		// Recognize input stroke.
		EntityEntry[] entityCandidates = model
				.getRecognizer().recognize(allPoints, strokes.size());
		if(entityCandidates != null && entityCandidates.length > 0) 
			Arrays.stream(entityCandidates)
				.map(ComponentCandidate::new)
				.forEach(candidates::add);
		
		// Judge whether it is the condition to add link to candidates.
		Vector<PointR> stroke0;
		if(strokes.size() == 1 && (stroke0 = strokes.get(0)).size() > 1) {
			PointR strokeBegin = stroke0.get(0);
			PointR strokeEnd = stroke0.get(stroke0.size() - 1);
			
			// The stroke should touch the components.
			SketchEntityComponent componentBegin = model
					.entityAt(strokeBegin.intX(), strokeBegin.intY());
			if(componentBegin == model.getSelectedEntity()) 
				componentBegin = model.getOriginalEntity();
			SketchEntityComponent componentEnd = model
					.entityAt(strokeEnd.intX(), strokeEnd.intY());
			if(componentEnd == model.getSelectedEntity())
				componentEnd = model.getOriginalEntity();
			if(componentBegin != null && componentEnd != null) {
				for(LinkEntry link : model.getTemplate().links()) 
					if(link.filter.test(componentBegin.entity, componentEnd.entity)) {
						// We've found a link that is applicable, so add it.
						LinkCandidate linkCandidate = new LinkCandidate(
								componentBegin, componentEnd, pathManager.quantize(stroke0, 
									componentBegin.getBoundRectangle(), 
									componentEnd.getBoundRectangle()), link);
						candidates.add(0, linkCandidate);
					}
			}
		}
		
		// Update the candidate list.
		candidatePanel.updateCandidates(candidates
				.toArray(new CandidatePanel.CandidateObject[0]));
	}
	
	@Override
	public void mouseReleased(MouseEvent arg0) {
		SketchEntityComponent selectedEntity = model.getSelectedEntity();
		// Left button for stroke drawing.
		if(arg0.getButton() == MouseEvent.BUTTON1) {
			// Clear previous candidates.
			candidatePanel.updateCandidates(null);
			model.selectEntity(null, null);
			
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
			if(candidatePanel.numCandidates() == 0 && strokes.size() > 0) {
				if(!Configuration.getInstance().INSTANT_RECOGNIZE)
					performRecognition();
				repaint();
			}
			
			// Right mouse for result confirmation.
			else if(candidatePanel.numCandidates() > 0) {
				candidatePanel.confirm();
				focusSelected();
				resetInputState();
				repaint();
			}
			
			// Right mouse for location confirmation.
			else if(selectedEntity != null) {
				// Partial confirmation.
				selectedEntity = model.getOriginalEntity();
				model.selectEntity(null, null);
				model.selectEntity(null, selectedEntity);
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
	
	private void paintSketchLink(Graphics g, 
			SketchLinkComponent<Path> current, boolean preview) {
		
		// Bound rectangle retrieval.
		Rectangle2D boundSource = current.source.getBoundRectangle();
		Rectangle2D boundDestination = current.destination.getBoundRectangle();
		
		// Judge whether the current object is selected, and replace with dynamic.
		if(current.source == model.getOriginalEntity())
			boundSource = model.getSelectedEntity().getBoundRectangle();
		if(current.destination == model.getOriginalEntity())
			boundDestination = model.getSelectedEntity().getBoundRectangle();
		
		// The concrete part of the rendering object.
		current.entry.linkView.render(current.source.entity, 
				current.destination.entity, current.link).paint(
						(Graphics2D)g, preview, current.pathObject, pathView, 
						boundSource, boundDestination);
	}
	
	@Override
	public void paint(Graphics g) {
		SketchEntityComponent selectedEntity = model.getSelectedEntity();
		SketchLinkComponent<Path> selectedLink = model.getSelectedLink();
		
		g.setFont(Configuration.getInstance().HANDWRITING_FONT);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		Graphics2D g2d = (Graphics2D) g;
		
		// Render the links in order.
		for(int i = 0; i < model.numLinks(); ++ i) {
			SketchLinkComponent<Path> current = model.getLink(i);
			if(current != selectedLink)
				paintSketchLink(g, current, false);
		}
		
		// Render the entities in order.
		for(int i = model.numEntities() - 1; i >= 0; -- i) {
			SketchEntityComponent current = model.getEntity(i);
			
			// Selection box if the object is selected.
			if(current == selectedEntity) {
				g.setColor(Color.LIGHT_GRAY);
				g.fillRect(selectedEntity.x, selectedEntity.y, 
						selectedEntity.w, selectedEntity.h);
			}
			
			// Default object component.
			paintSketchComponent(g, current, false);
		}
		
		// Render the candidate object.
		CandidatePanel.CandidateObject candidate = candidatePanel.current();
		if(candidate instanceof SketchPanel.ComponentCandidate) {
			// The current candidate object is a component.
			@SuppressWarnings("unchecked")
			SketchPanel<Path>.ComponentCandidate componentCandidate 
				= (SketchPanel<Path>.ComponentCandidate) candidate;
			paintSketchComponent(g, componentCandidate.component, true);
		}
		else if(candidate instanceof SketchPanel.LinkCandidate) {
			// The current candidate object is a link.
			@SuppressWarnings("unchecked")
			SketchPanel<Path>.LinkCandidate linkCandidate 
				= (SketchPanel<Path>.LinkCandidate) candidate;
			paintSketchLink(g, linkCandidate.component, true);
		}
		
		// Render the current selected link object.
		if(selectedLink != null)
			paintSketchLink(g, selectedLink, true);
		
		// Render the newly painting stroke.
		g2d.setStroke(new BasicStroke(2));
		g2d.setColor(Color.BLACK);
		Enumeration<Vector<PointR>> en = strokes.elements();
		while (en.hasMoreElements()) {
			Vector<PointR> pts = en.nextElement();
			RenderUtils.drawStroke(g2d, pts);
		}
		if(!(points.size() < 2)) RenderUtils.drawStroke(g2d, points);
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		SketchEntityComponent selected = model.getSelectedEntity();
		if(candidatePanel.numCandidates() > 0) {
			model.selectEntity(null, null);
			candidatePanel.scroll((arg0
					.getWheelRotation() > 0? 1 : -1));
			repaint();
		}
		else if(selected != null){
			SketchEntityComponent init = model.getOriginalEntity();
			zoomMultiplier += arg0.getWheelRotation() > 0? -0.1 : +0.1;
			if(zoomMultiplier < 0) zoomMultiplier = 
					(float)Math.max(1.0 / init.w, 1.0 / init.h);
			
			selected.w = (int)(zoomMultiplier * init.w);
			selected.h = (int)(zoomMultiplier * init.h);
			selected.x = (int)(init.x + init.w / 2 - zoomMultiplier / 2 * init.w);
			selected.y = (int)(init.y + init.h / 2 - zoomMultiplier / 2 * init.h);
			model.notifyEntityChanged(null);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// Select the candidate via keyboard input.
		int numCandidates = candidatePanel.numCandidates();
		if(numCandidates > 0) {
			int keyIndex = e.getKeyCode() - KeyEvent.VK_1;
			if(keyIndex >= 0 && keyIndex <= 9 && 
					keyIndex < numCandidates) {
				candidatePanel.select(keyIndex);
				return;
			}
		}
		
		// Operate on the current selected entity (if any).
		SketchEntityComponent selectedEntity = model.getSelectedEntity();
		if(selectedEntity != null) {
			if(e.getKeyCode() == KeyEvent.VK_DELETE)
				// Remove the selected object if any.
				model.destroy(null, selectedEntity);
			else if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
				model.selectEntity(null, null);
			else {
				// Perform object moving if direction is pressed.
				boolean moved = true;
				boolean shiftPressed = (e.getModifiersEx() 
						& KeyEvent.SHIFT_DOWN_MASK) != 0;
				switch(e.getKeyCode()) {
					case KeyEvent.VK_UP:
						if(shiftPressed) selectedEntity.h --;
						else selectedEntity.y --;
						break;
					case KeyEvent.VK_DOWN:
						if(shiftPressed) selectedEntity.h ++;
						else selectedEntity.y ++;
						break;
					case KeyEvent.VK_LEFT:
						if(shiftPressed) selectedEntity.w --;
						else selectedEntity.x --;
						break;
					case KeyEvent.VK_RIGHT:
						if(shiftPressed) selectedEntity.w ++;
						else selectedEntity.x ++;
						break;
					default:
						moved = false;
						break;
				}
				if(moved) model.notifyEntityChanged(null);
			}
		}
		
		// Operate on the current selected link (if any).
		SketchLinkComponent<Path> selectedLink = model.getSelectedLink();
		if(selectedLink != null) {
			if(e.getKeyCode() == KeyEvent.VK_DELETE)
				// Remove the selected link if any.
				model.unlink(null, selectedLink);
		}
		
		// For other cases, just clear all strokes and state and paint.
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			resetInputState();
			repaint();
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		
	}
}
