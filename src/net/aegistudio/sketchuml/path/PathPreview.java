package net.aegistudio.sketchuml.path;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;

import de.dubs.dollarn.PointR;

public class PathPreview extends JComponent {
	private static final long serialVersionUID = 1L;
	
	private final DefaultPathManager manager = new DefaultPathManager();
	private final DefaultPathView view = new DefaultPathView();
	private DefaultPath path;
	
	private final Vector<PointR> points = new Vector<>();
	
	private Rectangle2D boundBegin = new Rectangle2D.Double(165, 100, 50, 70);
	private Rectangle2D boundEnd = new Rectangle2D.Double(365, 100, 50, 70);
	
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
	};
	
	public PathPreview() {
		addMouseListener(adapter);
		addMouseMotionListener(adapter);
	}
	
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
				false, path, PathView.LineStyle.DASHDOT, 
				boundBegin, PathView.ArrowStyle.CIRCLE_EMPTY, 
				boundEnd, PathView.ArrowStyle.CIRCLE_FILLED);
	}
	
	public static void main(String[] arguments) {
		JFrame frame = new JFrame("Path preview");
		frame.setSize(600, 480);
		frame.add(new PathPreview());
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
