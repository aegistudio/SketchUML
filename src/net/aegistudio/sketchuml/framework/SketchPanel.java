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
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.function.Supplier;

import javax.swing.JComponent;

import de.dubs.dollarn.PointR;
import net.aegistudio.sketchuml.Background;
import net.aegistudio.sketchuml.Command;
import net.aegistudio.sketchuml.Configuration;
import net.aegistudio.sketchuml.EntityEntry;
import net.aegistudio.sketchuml.History;
import net.aegistudio.sketchuml.LinkEntry;
import net.aegistudio.sketchuml.SketchRenderHint;
import net.aegistudio.sketchuml.SketchView;
import net.aegistudio.sketchuml.path.PathManager;
import net.aegistudio.sketchuml.path.PathView;
import net.aegistudio.sketchuml.stroke.SketchRecognizer;

public class SketchPanel<Path> extends JComponent implements 
	MouseListener, MouseMotionListener, MouseWheelListener, KeyListener,
	SketchModel.Observer<Path>, SketchSelectionModel.Observer<Path> {
	
	private static final long serialVersionUID = 1L;
	private final Object keyPaintObject = new Object();
	private final Object keyDeleteObject = new Object();
	
	private final SketchModel<Path> model;
	private final SketchSelectionModel<Path> selectionModel;
	private final SketchRecognizer recognizer;
	private final CandidatePanel candidatePanel;
	private final PathManager<Path> pathManager;
	private final PathView<Path> pathView;
	
	private final CheatSheetGraphics cheatSheet;
	private final History history;
	private final Supplier<SketchRenderHint> renderHint;
	private final Supplier<Background> background;
	
	public SketchPanel(CandidatePanel candidatePanel, 
			History history, SketchSelectionModel<Path> selectionModel, 
			SketchModel<Path> model, SketchRecognizer recognizer, 
			PathManager<Path> pathManager, PathView<Path> pathView,
			CheatSheetGraphics cheatsheet, Supplier<SketchRenderHint> renderHint,
			Supplier<Background> background) {
		
		this.model = model;
		this.selectionModel = selectionModel;
		this.history = history;
		this.candidatePanel = candidatePanel;
		this.recognizer = recognizer;
		this.pathManager = pathManager;
		this.pathView = pathView;
		this.cheatSheet = cheatsheet;
		this.renderHint = renderHint;
		this.background = background;
		
		model.subscribe(this);
		selectionModel.subscribe(this);
		
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
				// Initialize the command of inserting entity.
				Command entityCommand = new Command() {
					@Override
					public void execute() {
						model.create(component);
						selectionModel.requestSelectEntity(component);
					}

					@Override
					public void undo() {
						if(selectionModel.selectedEntity() == component)
							selectionModel.requestUnselect();
						model.destroy(component);
					}

					@Override
					public String name() {
						return "Insert Object " 
								+ component.entry.name;
					}
				};
				
				// Conclude the painting process with the entity
				// insertion.
				history.finishLocal(keyPaintObject, 
						entityCommand, true);
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
			this.confirmAction = () -> {
				// Initialize the command of inserting links.
				Command linkCommand = new Command() {

					@Override
					public void execute() {
						model.link(component);
						selectionModel.requestSelectLink(component);
					}

					@Override
					public void undo() {
						if(selectionModel.selectedLink() == component)
							selectionModel.requestUnselect();
						model.unlink(component);
					}

					@Override
					public String name() {
						return "Link Objects";
					}
				};
				
				// Conclude the painting process with the link
				// insertion. Notice the link will not be selected
				// because we need to distinguish the line on
				// candidate demonstration and the line on selection.
				history.finishLocal(keyPaintObject, 
						linkCommand, false);
				model.link(component);
			};
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
	public boolean displayUsage = false;
	
	@Override
	public void mouseDragged(MouseEvent arg0) {
		SketchEntityComponent selected;
		// Left mouse button down, then regard it as stroke input.
		if(leftMouseDown(arg0)) {
			// Clear previous candidates.
			candidatePanel.updateCandidates(null);
			selectionModel.requestUnselect();
			
			Point point = arg0.getPoint();
			points.add(new PointR(point.x, point.y));
			repaint();
		}
		
		// Right mouse button down.
		if(rightMouseDown(arg0) && (selected = 
				selectionModel.selectedEntity()) != null) {
			
			// Initialize parameters for updating.
			selected.x = init.x; selected.y = init.y;
			selected.w = init.w; selected.h = init.h;
			int dx = (arg0.getX() - initMouseX);
			int dy = (arg0.getY() - initMouseY);
			
			// Resize or move according to the shift key state.
			if(arg0.isShiftDown()) {
				// Resize the object if shift is down.
				selected.w = Math.max(init.w + dx,
						Configuration.getInstance().MIN_ENTITYWIDTH);
				selected.h = Math.max(init.h + dy,
						Configuration.getInstance().MIN_ENTITYHEIGHT);
			}
			else {
				// Move the object if not.
				selected.x = init.x + dx;
				selected.y = init.y + dy;
			}
			model.notifyEntityMoved(selected);
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
		displayUsage = false; // Respond to mouse input.
		history.setHistoryProtection(true);
		
		this.requestFocusInWindow();
		if(candidatePanel.numCandidates() > 0) return;

		// Initial editing parameters when right clicked.
		if(arg0.getButton() == MouseEvent.BUTTON3) {
			selectionModel.requestUnselect();
			SketchEntityComponent entityToSelect = null;
			SketchLinkComponent<Path> linkToSelect = null;
			
			if(!Configuration.getInstance().LINK_INTERLEAVED_RENDER) {
				// When the link component is not interleaved, we just need
				// to judge whether there's entity at the front.
				entityToSelect = model.entityAt(arg0.getX(), arg0.getY());
				
				// Judge whether an entity is already selected.
				if(entityToSelect == null) 
					linkToSelect = model.linkAt(arg0.getX(), arg0.getY());
				
				// Nothing is selected, the initial selection is enough.
			}
			else {
				// A more complicated case, both link and entity is required to
				// be judged.
				entityToSelect = model.entityAt(arg0.getX(), arg0.getY());
				linkToSelect = model.linkAt(arg0.getX(), arg0.getY());
				
				// Judge whether the link overlays the entity.
				if(entityToSelect != null && linkToSelect != null) {
					int entityIndex = model.entityIndexOf(entityToSelect);
					int sourceIndex = model.entityIndexOf(linkToSelect.source);
					int destinationIndex = model.entityIndexOf(linkToSelect.destination);
					if(entityIndex < sourceIndex || entityIndex < destinationIndex)
						// Entity is selected, as link is overlaid.
						linkToSelect = null;
					else // Link is selected, as entity does not overlay it.
						entityToSelect = null;
				}
			}
			
			// Perform actual entity selection if any.
			if(entityToSelect != null) {
				selectionModel.requestSelectEntity(entityToSelect);
				focusSelected();
				initMouseX = arg0.getX(); initMouseY = arg0.getY();
				return;
			}
			
			// Perform actual link selection if any.
			if(linkToSelect != null) {
				selectionModel.requestSelectLink(linkToSelect);
				return;
			}
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
		EntityEntry[] entityCandidates = recognizer
				.recognize(allPoints, strokes.size());
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
			SketchEntityComponent componentEnd = model
					.entityAt(strokeEnd.intX(), strokeEnd.intY());
			
			// Begin to find some stroke.
			if(componentBegin != null && componentEnd != null) {
				// Ensure the stroke is not intrinsic, or all stroke contained in
				// a single shape in other word.
				boolean isIntrinsic = false;
				if(componentBegin == componentEnd) {
					Rectangle2D monoBound = componentBegin
							.getBoundRectangle();
					isIntrinsic = stroke0.stream().allMatch(p -> 
						monoBound.contains(p.X, p.Y));
				}
				
				if(!isIntrinsic) for(LinkEntry link : model.getTemplate().links()) 
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
		history.setHistoryProtection(false);
		
		SketchEntityComponent selectedEntity = selectionModel.selectedEntity();
		// Left button for stroke drawing.
		if(arg0.getButton() == MouseEvent.BUTTON1) {
			// Clear previous candidates.
			candidatePanel.updateCandidates(null);
			selectionModel.requestUnselect();
			
			// Transport the points to the stroke.
			if(points.size() > 1) {
				// Clone the points and create stroke command.
				Vector<PointR> newPoints = new Vector<>(points);
				Command strokeCommand = new Command() {
					@Override
					public void execute() {
						strokes.add(newPoints);
						points.clear();
						updateStrokes();
					}
	
					@Override
					public void undo() {
						strokes.remove(newPoints);
						points.clear();
						updateStrokes();
					}
					
					private void updateStrokes() {
						// Judge whether to perform soon recognizing.
						if(strokes.size() > 0) {
							if(Configuration.getInstance().INSTANT_RECOGNIZE)
								performRecognition();
						}
						repaint();
					}

					@Override
					public String name() {
						return "Draw Stroke";
					}
				};
				
				// See the number of strokes inputed.
				if(strokes.size() == 0) {
					// Create a local history when there's only one stroke 
					// inputed. The first stroke can not be undone. 
					strokeCommand.execute();
					history.startLocal(keyPaintObject);
				}
				
				// Add proceeding strokes and they can be undone in the local
				// history.
				else history.perform(keyPaintObject, strokeCommand, true);
			}
			else {
				points.clear();
				
				// Judge whether to perform soon recognizing.
				if(strokes.size() > 0) {
					if(Configuration.getInstance().INSTANT_RECOGNIZE)
						performRecognition();
				}

				repaint();
			}
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
				selectedEntity = selectionModel.selectedEntity();
				selectionModel.requestUnselect();
				selectionModel.requestSelectEntity(selectedEntity);
				focusSelected();
				repaint();
			}
		}
		
		// Middle mouse button for reset.
		else if(arg0.getButton() == MouseEvent.BUTTON2) {
			if(strokes.size() > 0)
				history.finishLocal(keyPaintObject, null, false);
			resetInputState();
			repaint();
		}
	}
	
	private void paintSketchComponent(SketchRenderHint hint, 
		Graphics g, SketchEntityComponent current, boolean preview) {
		
		// The concrete part of the rendering object.
		Graphics currentGraphics = g.create(
				current.x, current.y, current.w, current.h);
		current.entry.sketchView.renderEntity(hint,
				currentGraphics, current.entity, preview);
		
		// The overlaying part of the rendering object.
		g.setColor(hint.getLineColor(SketchRenderHint.outerLabelColor, preview));
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
	
	private void paintSketchLink(SketchRenderHint hint, Graphics g, 
			SketchLinkComponent<Path> current, boolean preview) {
		
		// Bound rectangle retrieval.
		Rectangle2D boundSource = current.source.getBoundRectangle();
		Rectangle2D boundDestination = current.destination.getBoundRectangle();
		
		// The concrete part of the rendering object.
		current.entry.linkView.render(current.source.entity, 
				current.destination.entity, current.link).paint(hint,
						(Graphics2D)g, preview, current.pathObject, pathView, 
						boundSource, boundDestination);
	}
	
	private interface SketchPaintInterface<Path> {
		public void paint(SketchRenderHint hint, Graphics2D g2d,
			SketchEntityComponent selectedEntity,
			SketchLinkComponent<Path> selectedLink);
	}
	
	private boolean dirty = false;
	@Override
	public void repaint() {
		if(dirty) return;
		dirty = true;
		super.repaint();
	}
	
	@Override
	public void paint(Graphics g) {
		// Draw other entities on the canvas.
		SketchEntityComponent selectedEntity = selectionModel.selectedEntity();
		SketchLinkComponent<Path> selectedLink = selectionModel.selectedLink();
		Graphics2D g2d = (Graphics2D) g;
		SketchRenderHint hint = this.renderHint.get();
		
		// Update the rendering hint.
		g.setFont(hint.labelFont);
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		g2d.setRenderingHints(hint.awtRenderingHints);
		
		// Retrieve the background.
		Background background = this.background.get();
		background.renderBackground(g2d, hint, 
				0, 0, getWidth(), getHeight());
		
		// Render common objects.
		SketchPaintInterface<Path> paintInterface;
		if(Configuration.getInstance().LINK_INTERLEAVED_RENDER)
			// Render the objects with links-first-entities-last manner.
			paintInterface = this::paintObjectsInterleaved;
		else // Render links alongside with its entities.
			paintInterface = this::paintObjectsSeparated;
		paintInterface.paint(hint, g2d, selectedEntity, selectedLink);
		
		// Render the candidate object.
		CandidatePanel.CandidateObject candidate = candidatePanel.current();
		if(candidate instanceof SketchPanel.ComponentCandidate) {
			// The current candidate object is a component.
			@SuppressWarnings("unchecked")
			SketchPanel<Path>.ComponentCandidate componentCandidate 
				= (SketchPanel<Path>.ComponentCandidate) candidate;
			paintSketchComponent(hint, g, componentCandidate.component, true);
		}
		else if(candidate instanceof SketchPanel.LinkCandidate) {
			// The current candidate object is a link.
			@SuppressWarnings("unchecked")
			SketchPanel<Path>.LinkCandidate linkCandidate 
				= (SketchPanel<Path>.LinkCandidate) candidate;
			paintSketchLink(hint, g, linkCandidate.component, true);
		}
		
		// Render the newly painting stroke.
		g2d.setStroke(new BasicStroke(hint.userWidth));
		g2d.setColor(hint.userColor);
		Enumeration<Vector<PointR>> en = strokes.elements();
		while (en.hasMoreElements()) {
			Vector<PointR> pts = en.nextElement();
			RenderUtils.drawStroke(g2d, pts);
		}
		if(!(points.size() < 2)) RenderUtils.drawStroke(g2d, points);
		
		// Draw the cheat sheet if available.
		if(displayUsage && cheatSheet != null) {
			g.drawImage(cheatSheet.image, 
					(getWidth() - cheatSheet.imageWidth) / 2, 
					(getHeight() - cheatSheet.imageHeight) / 2, null);
		}
		
		// Clear the painting bits.
		dirty = false;
	}
	
	private void paintObjectsSeparated(SketchRenderHint hint, 
			Graphics2D g2d, SketchEntityComponent selectedEntity,
			SketchLinkComponent<Path> selectedLink) {
		// Render the links in order.
		for(int i = 0; i < model.numLinks(); ++ i) {
			SketchLinkComponent<Path> current = model.getLink(i);
			if(current != selectedLink)
				paintSketchLink(hint, g2d, current, false);
		}
		
		// Render the entities in order.
		for(int i = model.numEntities() - 1; i >= 0; -- i) {
			SketchEntityComponent current = model.getEntity(i);
			
			// Selection box if the object is selected.
			if(current == selectedEntity) {
				g2d.setColor(Color.LIGHT_GRAY);
				g2d.fillRect(selectedEntity.x, selectedEntity.y, 
						selectedEntity.w, selectedEntity.h);
			}
			
			// Default object component.
			paintSketchComponent(hint, g2d, current, false);
		}
		
		// Render the current selected link object.
		if(selectedLink != null)
			paintSketchLink(hint, g2d, selectedLink, true);
	}
	
	private void paintObjectsInterleaved(
			SketchRenderHint hint, Graphics2D g2d,
			SketchEntityComponent selectedEntity,
			SketchLinkComponent<Path> selectedLink) {
		
		// Retrieve all paths first.
		List<SketchLinkComponent<Path>> paths = new ArrayList<>();
		for(int i = 0; i < model.numLinks(); ++ i)
			paths.add(model.getLink(i));
		
		// Sort the links with correct order.
		for(int i = model.numEntities() - 1; i >= 0; -- i) {
			SketchEntityComponent current = model.getEntity(i);
			
			// Selection box if the object is selected.
			if(current == selectedEntity) {
				g2d.setColor(Color.LIGHT_GRAY);
				g2d.fillRect(selectedEntity.x, selectedEntity.y, 
						selectedEntity.w, selectedEntity.h);
			}
			
			// Iterate for every paths in the model.
			Iterator<SketchLinkComponent<Path>> iterator = paths.iterator();
			while(iterator.hasNext()) {
				SketchLinkComponent<Path> currentLink = iterator.next();
				SketchEntityComponent relation = current;
				if(currentLink.relatedTo(relation)) {
					if(selectedLink != currentLink)
						paintSketchLink(hint, g2d, currentLink, false);
					iterator.remove();
				}
			}
			
			// Default object component.
			paintSketchComponent(hint, g2d, current, false);
		}
		
		// Render the current selected link object.
		if(selectedLink != null)
			paintSketchLink(hint, g2d, selectedLink, true);
	}
	
	@Override
	public void mouseMoved(MouseEvent arg0) {
		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		SketchEntityComponent selected = selectionModel.selectedEntity();
		if(candidatePanel.numCandidates() > 0) {
			selectionModel.requestUnselect();
			candidatePanel.scroll((arg0
					.getWheelRotation() > 0? 1 : -1));
			repaint();
		}
		else if(selected != null && hasFocus()) {
			zoomMultiplier += arg0.getWheelRotation() > 0? -0.1 : +0.1;
			zoomMultiplier = Math.max(zoomMultiplier, (float)Math.max(
					1.0f * Configuration.getInstance().MIN_ENTITYWIDTH / init.w, 
					1.0f * Configuration.getInstance().MIN_ENTITYHEIGHT / init.h));
			
			selected.w = (int)(zoomMultiplier * init.w);
			selected.h = (int)(zoomMultiplier * init.h);
			selected.x = (int)(init.x + init.w / 2 - zoomMultiplier / 2 * init.w);
			selected.y = (int)(init.y + init.h / 2 - zoomMultiplier / 2 * init.h);
			model.notifyEntityMoved(selected);
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
		SketchEntityComponent selectedEntity = selectionModel.selectedEntity();
		if(selectedEntity != null) {
			if(e.getKeyCode() == KeyEvent.VK_DELETE) {
				// Remove the selected object if any.
				Command entityCommand = new CommandDeleteEntity<Path>(
						model, selectionModel, selectedEntity);
				history.perform(keyDeleteObject, entityCommand, true);
			}
			else if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
				selectionModel.requestUnselect();
			else {
				// Perform object moving if direction is pressed.
				boolean moved = true;
				boolean shiftPressed = (e.getModifiersEx() 
						& KeyEvent.SHIFT_DOWN_MASK) != 0;
				switch(e.getKeyCode()) {
					case KeyEvent.VK_UP:
						if(shiftPressed) 
							selectedEntity.h = Math.max(selectedEntity.h - 1, 
									Configuration.getInstance().MIN_ENTITYHEIGHT);
						else selectedEntity.y --;
						break;
					case KeyEvent.VK_DOWN:
						if(shiftPressed) selectedEntity.h ++;
						else selectedEntity.y ++;
						break;
					case KeyEvent.VK_LEFT:
						if(shiftPressed) 
							selectedEntity.w = Math.max(selectedEntity.w - 1, 
									Configuration.getInstance().MIN_ENTITYWIDTH);
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
				if(moved) model.notifyEntityUpdated(selectedEntity);
			}
		}
		
		// Operate on the current selected link (if any).
		SketchLinkComponent<Path> selectedLink = selectionModel.selectedLink();
		if(selectedLink != null) {
			if(e.getKeyCode() == KeyEvent.VK_DELETE) {
				// Remove the selected link if any.
				Command linkCommand = new CommandDeleteLink<Path>(
						model, selectionModel, selectedLink);
				history.perform(keyDeleteObject, linkCommand, true);
			}
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

	@Override
	public void entityCreated(SketchEntityComponent entity) {
		repaint();
	}

	@Override
	public void entityDestroyed(SketchEntityComponent entity) {
		repaint();
	}

	@Override
	public void entityUpdated(SketchEntityComponent entity) {
		repaint();
	}

	@Override
	public void entityMoved(SketchEntityComponent entity) {
		repaint();
	}

	@Override
	public void entityReordered(SketchEntityComponent entity) {
		repaint();
	}

	@Override
	public void linkCreated(SketchLinkComponent<Path> link) {
		repaint();
	}

	@Override
	public void linkDestroyed(SketchLinkComponent<Path> link) {
		repaint();
	}

	@Override
	public void linkUpdated(SketchLinkComponent<Path> link) {
		repaint();
	}

	@Override
	public void linkStyleChanged(SketchLinkComponent<Path> link) {
		repaint();
	}

	SketchEntityComponent init;
	
	@Override
	public void selectEntity(SketchEntityComponent entity) {
		init = new SketchEntityComponent(null, null);
		init.x = entity.x; init.y = entity.y;
		init.w = entity.w; init.h = entity.h;
		repaint();
	}

	@Override
	public void selectLink(SketchLinkComponent<Path> link) {
		init = null;
		repaint();
	}

	@Override
	public void unselect() {
		init = null;
		repaint();
	}
}
