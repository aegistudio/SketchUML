package net.aegistudio.sketchuml.path;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;

import de.dubs.dollarn.PointR;

public class PathPreview extends JComponent {
	private static final long serialVersionUID = 1L;
	
	private final DefaultPathManager manager = new DefaultPathManager();
	private final DefaultPathView view = new DefaultPathView();
	private DefaultPath path;
	
	private final Vector<PointR> points = new Vector<>();
	
	private Rectangle2D boundBegin = new Rectangle2D.Double(165, 100, 50, 70);
	private Rectangle2D boundEnd = new Rectangle2D.Double(365, 100, 50, 70);
	
	private final JLabel statusBar = new JLabel();
	
	private final MouseAdapter adapter = new MouseAdapter() {
		@Override
		public void mousePressed(MouseEvent me) {
			points.clear();
			PathPreview.this.repaint();
		}
		
		@Override
		public void mouseDragged(MouseEvent me) {
			points.add(new PointR(me.getX(), me.getY()));
			PathPreview.this.repaint();
		}
		
		@Override
		public void mouseReleased(MouseEvent me) {
			path = manager.quantize(points, boundBegin, boundEnd);
			PathPreview.this.repaint();
		}
		
		@Override
		public void mouseMoved(MouseEvent me) {
			if(path != null) {
				PointR position = new PointR(me.getX(), me.getY());
				double distance = view.distance(path,
						position, boundBegin, boundEnd);
				statusBar.setText(view.totalLength(
						path, boundBegin, boundEnd) + ", " + distance);
			}
		}
	};
	
	public JComponent component = new JComponent() {
		private static final long serialVersionUID = 1L;

		public void paint(Graphics g) {
			g.setColor(Color.BLACK);
			for(int i = 0; i < points.size() - 1; ++ i) {
				PointR start = points.get(i);
				PointR end = points.get(i + 1);
				g.drawLine((int)start.X, (int)start.Y, 
						(int)end.X, (int)end.Y);
			}
			
			g.fillRect((int)boundBegin.getX(), 
					(int)boundBegin.getY(),
					(int)boundBegin.getWidth(), 
					(int)boundBegin.getHeight());
			
			g.fillRect((int)boundEnd.getX(), 
					(int)boundEnd.getY(),
					(int)boundEnd.getWidth(), 
					(int)boundEnd.getHeight());
			
			if(path != null) view.render((Graphics2D)g, 
					true, path, PathView.LineStyle.DASHDOT, 
					boundBegin, PathView.ArrowStyle.DIAMOND_FILLED, 
					boundEnd, PathView.ArrowStyle.DIAMOND_EMPTY,
					"Start", "Center", "End");
		}
	};
	
	public PathPreview() {
		setLayout(new BorderLayout());
		
		add(component, BorderLayout.CENTER);
		component.addMouseListener(adapter);
		component.addMouseMotionListener(adapter);
		
		add(statusBar, BorderLayout.SOUTH);
	}
	
	public static void main(String[] arguments) {
		JFrame frame = new JFrame("Path preview");
		frame.setSize(600, 480);
		frame.add(new PathPreview());
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
