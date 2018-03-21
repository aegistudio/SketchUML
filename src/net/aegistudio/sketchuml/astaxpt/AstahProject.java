package net.aegistudio.sketchuml.astaxpt;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

import JP.co.esm.caddies.golf.model.EntityRoot;
import JP.co.esm.caddies.jomt.jmodel.ModelManageInfo;
import JP.co.esm.caddies.uml.Foundation.ExtensionMechanisms.UTaggedValueImp;
import JP.co.esm.caddies.uml.ModelManagement.UModelImp;

/**
 * Represents a common astah project. We could develop projects
 * of different diagrams upon it.
 * 
 * @author Haoran Luo
 */
public class AstahProject {
	public final AstahUuidGenerator uuid;
	public final ModelManageInfo mmi;
	public final EntityRoot root;
	public final UModelImp model;
	
	@SuppressWarnings("unchecked")
	public AstahProject(AstahUuidGenerator uuid) throws IOException {
		this.uuid = uuid;
		this.mmi = new ModelManageInfo();
		this.root = new EntityRoot();
		this.model = new UModelImp();
		
		// Mock-up the model manage information of astah community.
		mmi.currentModelProducer = "A.C";
		mmi.currentModelVersion = 37;
		mmi.lastModifiedTime = System.currentTimeMillis();
		mmi.maxModelVersion = 37;
		mmi.sortedVersionHistory.add(
				new String[] { "Community 7.2.0", "37"});
		
		// Mock-up the entity root of astah community.
		root.entry = new HashMap<>();
		root.entry.put("ProjectModel", model);
		root.store = new HashSet<>();
		root.store.add(model);
		model.id = uuid.nextUuid();
		
		// Load the serialized.model.styleMap. 
		{
			Properties styleMap = new Properties();
			styleMap.load(getClass().getResourceAsStream(
					"/assets/model/styleMap"));
			styleMap.forEach(model.styleMap::put);
		}
		
		// Load the serialized.model.judeProfiles.
		{
			UTaggedValueImp judeProfiles = new UTaggedValueImp();
			judeProfiles.invTaggedValue = model;
			model.taggedValue.add(judeProfiles);
			root.store.add(judeProfiles);
			judeProfiles.tag.body = "jude.profiles";
			InputStream judeProfilesStream = 
					getClass().getResourceAsStream(
							"/assets/model/judeProfiles");
			byte[] judeProfilesData = new byte[judeProfilesStream.available()];
			judeProfilesStream.read(judeProfilesData);
			judeProfiles.value.body = new String(judeProfilesData);
			judeProfiles.id = uuid.nextUuid();
		}
		
		// Load the serialized.model.judeUserIcons.
		{
			UTaggedValueImp judeUserIcons = new UTaggedValueImp();
			judeUserIcons.invTaggedValue = model;
			model.taggedValue.add(judeUserIcons);
			root.store.add(judeUserIcons);
			judeUserIcons.tag.body = "jude.usericons";
			InputStream judeUserIconsStream =
					getClass().getResourceAsStream(
							"/assets/model/judeUserIcons");
			byte[] judeUserIconsData = new byte[judeUserIconsStream.available()];
			judeUserIconsStream.read(judeUserIconsData);
			judeUserIcons.value.body = new String(judeUserIconsData);
			judeUserIcons.id = uuid.nextUuid();
		}
	}
}
