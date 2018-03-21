package net.aegistudio.sketchuml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.filechooser.FileFilter;

import JP.co.esm.caddies.golf.geom2D.Pnt2d;
import JP.co.esm.caddies.jomt.jmodel.BinaryRelationPresentation;
import JP.co.esm.caddies.jomt.jmodel.JomtPresentation;
import JP.co.esm.caddies.jomt.jmodel.RectPresentation;
import net.aegistudio.sketchuml.astaxpt.AstahExportable;
import net.aegistudio.sketchuml.astaxpt.AstahFileFormat;
import net.aegistudio.sketchuml.astaxpt.AstahUuidGenerator;
import net.aegistudio.sketchuml.astaxpt.DefaultUuidGenerator;
import net.aegistudio.sketchuml.framework.SketchEntityComponent;
import net.aegistudio.sketchuml.framework.SketchLinkComponent;
import net.aegistudio.sketchuml.framework.SketchModel;
import net.aegistudio.sketchuml.path.PathManager;

public class AstahPersistence<Path> extends 
	Persistence<AstahPersistence.AstahTransaction<Path>> {
	
	public static class AstahTransaction<T> {
		public SketchModel<T> sketchModel;
		
		public PathManager<T> pathManager;
		
		public String projectName;
	}
	
	FileFilter astaFileFilter = new FileFilter() {
		@Override
		public boolean accept(File arg0) {
			if(arg0.isDirectory()) return true;
			if(arg0.getName().endsWith(".asta")) return true;
			return arg0.getName().endsWith("*.jude");
		}

		@Override
		public String getDescription() {
			return "Astah Project (*.asta; *.jude)";
		}
	};
	
	@Override
	public FileFilter[] getFileFilters() {
		return new FileFilter[] { astaFileFilter };
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void save(AstahPersistence.AstahTransaction<Path> model, 
			File modelFile, FileFilter modelFormat) throws IOException {
		
		Template template = model.sketchModel.getTemplate();
		AstahUuidGenerator uuid = new DefaultUuidGenerator();
		
		// Record the entities.
		int numEntities = model.sketchModel.numEntities();
		Entity[] entities = new Entity[numEntities];
		EntityEntry[] entityTypes = new EntityEntry[numEntities];
		for(int i = 0; i < numEntities; ++ i) {
			entities[i] = model.sketchModel.getEntity(i).entity;
			entityTypes[i] = model.sketchModel.getEntity(i).entry;
		}
		
		// Record the links.
		int numLinks = model.sketchModel.numLinks();
		Entity[] links = new Entity[numLinks];
		LinkEntry[] linkTypes = new LinkEntry[numLinks];
		Entity[] sources = new Entity[numLinks];
		Entity[] destinations = new Entity[numLinks];
		for(int i = 0; i < numLinks; ++ i) {
			links[i] = model.sketchModel.getLink(i).link;
			linkTypes[i] = model.sketchModel.getLink(i).entry;
			sources[i] = model.sketchModel.getLink(i).source.entity;
			destinations[i] = model.sketchModel.getLink(i).destination.entity;
		}
		
		// Retrieve the Astah project model.
		Map<Entity, RectPresentation> entitiesView = new HashMap<>();
		Map<Entity, BinaryRelationPresentation> linksView = new HashMap<>();
		AstahExportable astah = template.prepareAstahProject(uuid, 
				model.projectName, entityTypes, entities, entitiesView, 
				sources, destinations, linkTypes, links, linksView);
		
		// Initialize frame regulate information.
		double minX = Double.MAX_VALUE; 
		double minY = Double.MAX_VALUE;
		double maxX = - Double.MAX_VALUE; 
		double maxY = - Double.MAX_VALUE;
		
		// Allocate position for those entities and links, and update the frame size.
		if(numEntities > 0) {
			// Visit every element of the entities.
			for(int i = 0; i < numEntities; ++ i) {
				SketchEntityComponent entity = model.sketchModel.getEntity(i);
				RectPresentation astahEntity = entitiesView.get(entity.entity);
				if(astahEntity == null) continue;
				astahEntity.location.x = entity.x;
				astahEntity.location.y = entity.y;
				astahEntity.width = entity.w;
				astahEntity.height = entity.h;
				applyTransform(astahEntity);
				
				// Update the gross entity size.
				minX = Math.min(minX, astahEntity.location.x);
				minY = Math.min(minY, astahEntity.location.y);
				maxX = Math.max(maxX, astahEntity.location.x 
						+ astahEntity.width);
				maxY = Math.max(maxY, astahEntity.location.y 
						+ astahEntity.height);
			}
			
			// As link lives on entities, we would only consider links
			// when there's entity presents.
			for(int i = 0; i < numLinks; ++ i) {
				SketchLinkComponent<Path> link = model.sketchModel.getLink(i);
				BinaryRelationPresentation astahLink = linksView.get(link.link);
				if(astahLink == null) continue;
				
				// Retrieve render hints.
				PathManager.AstahPathHint pathHint = model.pathManager
						.getAstahPathHint(link.pathObject, 
						link.source.getBoundRectangle(), 
						link.destination.getBoundRectangle());
				astahLink.sourceEndX = pathHint.sourceX;
				astahLink.sourceEndY = pathHint.sourceY;
				astahLink.targetEndX = pathHint.targetX;
				astahLink.targetEndY = pathHint.targetY;
				astahLink.allPoints = pathHint.innerPoints;
				astahLink.points = pathHint.controlPoints;
				astahLink.outerPoints = pathHint.outerPoints;
				astahLink.styleMap.put("line.shape", pathHint.lineStyle);
				
				// Apply view transformation.
				applyTransform(astahLink);
			}
		}
		
		// Update the frame size.
		double topPadding = 60;		double bottomPadding = 40;
		double leftPadding = 40;	double rightPadding = 40;
		astah.getFrame().location.x = minX - leftPadding;
		astah.getFrame().location.y = minY - topPadding;
		astah.getFrame().width = maxX - minX + leftPadding + rightPadding;
		astah.getFrame().height = maxY - minY + topPadding + bottomPadding;
		
		// Collect the elements in order, so that they could be allocated later.
		Map<SketchEntityComponent, List<SketchLinkComponent<Path>>> 
				linkAllocation = new HashMap<>();
		for(int i = 0; i < numEntities; ++ i) linkAllocation.put(
				model.sketchModel.getEntity(i), new ArrayList<>());
		for(int i = 0; i < numLinks; ++ i) {
			SketchLinkComponent<Path> link = model.sketchModel.getLink(i);
			int sourceOrder = model.sketchModel.entityIndexOf(link.source);
			int destinationOrder = model.sketchModel.entityIndexOf(link.destination);
			if(sourceOrder > destinationOrder) linkAllocation.get(link.source).add(link);
			else linkAllocation.get(link.destination).add(link);
		}
		
		// Collect the order information.
		List<JomtPresentation> viewOrder = new ArrayList<>();
		viewOrder.add(astah.getFrame());
		for(int i = 0; i < numEntities; ++ i) {
			// Place the entity itself.
			SketchEntityComponent entity = model.sketchModel.getEntity(i);
			if(entitiesView.containsKey(entity.entity)) 
				viewOrder.add(entitiesView.get(entity.entity));
			
			// Place the links.
			List<SketchLinkComponent<Path>> allocatedLinkViews 
				= linkAllocation.get(entity);
			for(SketchLinkComponent<Path> link : allocatedLinkViews) 
				if(linksView.containsKey(link.link)) 
					viewOrder.add(linksView.get(link.link));
		}
		
		// Perform allocation.
		for(int i = 0; i < viewOrder.size(); ++ i)
			viewOrder.get(i).depth = Integer.MAX_VALUE - i;
		
		// Finally export to Astah file format.
		AstahFileFormat exportFileFormat = new AstahFileFormat();
		exportFileFormat.mmi = astah.getMMI();
		exportFileFormat.root = astah.getRoot();
		exportFileFormat.write(modelFile);
	}
	
	double scaleFactor = 0.55;
	
	private void applyTransform(RectPresentation rectView) {
		rectView.location.x *= scaleFactor;
		rectView.location.y *= scaleFactor;
		rectView.width *= scaleFactor;
		rectView.height *= scaleFactor;
	}
	
	private void applyTransform(BinaryRelationPresentation pathView) {
		// Relocate the edit-displayed points.
		for(Pnt2d pnt : pathView.allPoints) {
			pnt.x *= scaleFactor;
			pnt.y *= scaleFactor;
		}
		
		// Relocate the displayed points.
		for(Pnt2d pnt : pathView.outerPoints) {
			pnt.x *= scaleFactor;
			pnt.y *= scaleFactor;
		}
		
		// Relocate the control points.
		for(Pnt2d pnt : pathView.points) {
			pnt.x *= scaleFactor;
			pnt.y *= scaleFactor;
		}
	}
}
