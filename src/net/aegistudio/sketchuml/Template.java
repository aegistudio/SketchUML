package net.aegistudio.sketchuml;

import java.io.IOException;
import java.util.Map;

import JP.co.esm.caddies.jomt.jmodel.PathPresentation;
import JP.co.esm.caddies.jomt.jmodel.RectPresentation;
import net.aegistudio.sketchuml.astaxpt.AstahProject;
import net.aegistudio.sketchuml.astaxpt.AstahUuidGenerator;
import net.aegistudio.sketchuml.framework.SketchEntityComponent;
import net.aegistudio.sketchuml.framework.SketchLinkComponent;

/**
 * Represents a kind of diagram (template) that could be 
 * drawn in SketchUML, giving the internal entities and links.
 * 
 * @author Haoran Luo
 */
public interface Template {
	/**
	 * Retrieving enumeration of entity types in current template.
	 * 
	 * The entity entries hold information for an entity type, like the 
	 * path of recognizing data for converting user stroke into entities,
	 * the view to visualize the entity, and the editor to edit the entity 
	 * properties.
	 * 
	 * @return entities in this template.
	 */
	public EntityEntry[] entities();
	
	/**
	 * Retrieving enumeration of link types in current template.
	 * 
	 * The links need not to be recognized (there's separated engine for 
	 * quantizing them), however the view to visualize the link and the
	 * editor to edit the link properties should be provided. 
	 * 
	 * @return the links in SketchUML, the link will not be
	 * recognized (as the entities) but their 
	 */
	public LinkEntry[] links();
	
	/**
	 * @return could current format be associated with and exported as 
	 * an Astah project.
	 */
	public boolean canExportAstah();
	
	/**
	 * Prepare an Astah project model. This work includes initializing all
	 * entities and link inside the model, and add the entities and links
	 * to a map such that followed allocation could be performed.
	 * 
	 * @param uuid[in] the generator to provide next UUID.
	 * @param projectName[in] the name of current project.
	 * @param entities[in] the entities in the model.
	 * @param entitiesView[out] corresponding views of entities.
	 * @param links[in] the links in the model.
	 * @param linksView[out] corresponding links of entities.
	 * @return the prepared Astah project model.
	 */
	public <Path> AstahProject prepareAstahProject(
			AstahUuidGenerator uuid, String projectName, 
			SketchEntityComponent[] entities,
			Map<SketchEntityComponent, RectPresentation> entitiesView,
			SketchLinkComponent<Path>[] links,
			Map<SketchLinkComponent<Path>, PathPresentation> linksView) throws IOException;
}