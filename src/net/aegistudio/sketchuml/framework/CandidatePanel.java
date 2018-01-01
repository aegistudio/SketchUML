package net.aegistudio.sketchuml.framework;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.aegistudio.sketchuml.Configuration;

public class CandidatePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final JLabel[] selectionLabels;
	
	public static class CandidateObject {
		public Runnable scrollAction;
		
		public Runnable confirmAction;
		
		public String text;
		
		public String color;
	}
	private CandidateObject[] candidates = null;
	private int candidateIndex = 0;
	
	public CandidatePanel() {
		selectionLabels = new JLabel[Configuration.getInstance().MAX_CANDIDATE];
		for(int i = 0; i < selectionLabels.length; ++ i) {
			final int current = i;
			selectionLabels[i] = new JLabel();
			selectionLabels[i].setPreferredSize(new Dimension(180, 30));
			selectionLabels[i].setHorizontalAlignment(JLabel.CENTER);
			add(selectionLabels[i]);
			
			selectionLabels[i].setFont(Configuration
					.getInstance().HANDWRITING_FONT);
			selectionLabels[i].addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent me) {
					select(current);
				}
			});
		}
	}
	
	// Update selection panel upon condition.
	private void updateContent() {
		// Clear label text.
		for(JLabel label : selectionLabels)
			label.setText("");
		
		// Update text content.
		if(this.candidates != null) {
			for(int i = 0; i < candidates.length && 
					i < Configuration.getInstance().MAX_CANDIDATE; ++ i) {
				String nameText = "" + (i + 1) + ": " 
					+ candidates[i].text;
				selectionLabels[i].setText(
					(candidateIndex != i)? nameText : 
						"<html><b style=\"background:" 
						+ candidates[i].color + "\">" 
						+ nameText + "</b></html>");
			}
		}

		// Select current candidate.
		if(		candidates == null || 
				candidateIndex < 0 || 
				candidateIndex > candidates.length) return;
		candidates[candidateIndex].scrollAction.run();
		
		repaint();
	}
	
	public void updateCandidates(CandidateObject[] candidates) {
		this.candidates = candidates;
		this.candidateIndex = 0;
		
		updateContent();
	}
	
	public void select(int index) {
		this.candidateIndex = index;
		updateContent();
	}
	
	public int index() {
		return this.candidateIndex;
	}
	
	public void scroll(int offset) {
		candidateIndex += offset;
		int bound = numCandidates();
		candidateIndex %= bound;
		if(candidateIndex < 0) candidateIndex += bound;
		updateContent();
	}
	
	public int numCandidates() {
		return this.candidates != null? Math.min(candidates.length, 
				Configuration.getInstance().MAX_CANDIDATE) : 0;
	}
	
	public void confirm() {
		if(		candidates == null || 
				candidateIndex < 0 || 
				candidateIndex > candidates.length) return;
		candidates[candidateIndex].confirmAction.run();
	}
	
	public CandidateObject current() {
		if(numCandidates() == 0) return null;
		return candidates[candidateIndex];
	}
}
