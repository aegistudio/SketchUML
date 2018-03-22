package net.aegistudio.sketchuml.statechart;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import JP.co.esm.caddies.jomt.jmodel.CompositeStatePresentation;
import JP.co.esm.caddies.uml.BehavioralElements.CommonBehavior.UActionImp;
import JP.co.esm.caddies.uml.BehavioralElements.StateMachines.UCompositeStateImp;
import net.aegistudio.sketchuml.astaxpt.AstahUuidGenerator;

public class EntityStateObject implements StateEntity {
	public String name = "";
	
	public String actions = "";
	
	public boolean isBrief = false;

	@Override
	public void load(DataInputStream inputStream) throws IOException {
		name = inputStream.readUTF();
		actions = inputStream.readUTF();
		isBrief = inputStream.readBoolean();
	}

	@Override
	public void save(DataOutputStream outputStream) throws IOException {
		outputStream.writeUTF(name);
		outputStream.writeUTF(actions);
		outputStream.writeBoolean(isBrief);
	}

	@SuppressWarnings("unchecked")
	@Override
	public AstahStateObject toAstahState(AstahUuidGenerator uuid) {
		UCompositeStateImp stateModel = new UCompositeStateImp();
		CompositeStatePresentation stateView = new CompositeStatePresentation();
		stateModel.name.body = name;
		stateModel.definition.body = actions;
		stateView.allActionVisibility = !isBrief;
		
		// Attempt to parse the entry, do and exit action.
		// The token matched will be { "do", "entry", "exit" }.
		Map<String, String> actionMap = new HashMap<>();
		parseAction(actions, actionMap, "do", "entry", "exit");
		
		// Fill-in the do action.
		if(actionMap.containsKey("do")) {
			UActionImp doActionImp = new UActionImp();
			doActionImp.id = uuid.nextUuid();
			doActionImp.name.body = actionMap.get("do");
			stateModel.doActivity = doActionImp;
		}
		
		// Fill-in the entry action.
		if(actionMap.containsKey("entry")) {
			UActionImp entryActionImp = new UActionImp();
			entryActionImp.id = uuid.nextUuid();
			entryActionImp.name.body = actionMap.get("entry");
			stateModel.entry = entryActionImp;
		}
		
		// Fill-in the exit action.
		if(actionMap.containsKey("exit")) {
			UActionImp exitActionImp = new UActionImp();
			exitActionImp.id = uuid.nextUuid();
			exitActionImp.name.body = actionMap.get("exit");
			stateModel.exit = exitActionImp;
		}
		
		// Caution: the state's color is set to wheat yellow by default.
		stateView.styleMap.put("fill.color.alpha", "FF");
		stateView.styleMap.put("fill.color", "#FFFFCC");
		
		// Collect the created instance.
		AstahStateObject stateObject = new AstahStateObject();
		stateObject.stateModel = stateModel;
		stateObject.stateView = stateView;
		return stateObject;
	}
	
	private void parseAction(String parsingString,
			Map<String, String> actionMap, 
			String defaultAction, String... auxiliaryActions) {
		String previousAction = defaultAction;
		String previousString = parsingString.trim();
		
		// Retrieve tokens as list.
		List<String> tokens = new ArrayList<>();
		tokens.add(defaultAction);
		for(String auxiliaryAction : auxiliaryActions)
			tokens.add(auxiliaryAction);
		
		// Perform matches one by one.
		boolean hasMatch;
		do {
			hasMatch = false;
			for(String token : tokens) {
				int tokenIndex = previousString.indexOf(token);
				if(tokenIndex < 0) continue;
				
				// Attempt to remove the format.
				String afterString = previousString.substring(
						tokenIndex + token.length());
				afterString = afterString.trim();
				if(afterString.charAt(0) != '/') continue;
				afterString = afterString.substring(1);
				afterString = afterString.trim();
				
				// Retrieve the previous part of string.
				hasMatch = true;
				String previousPart = removeTrailingLineBreak(
						previousString.substring(0, tokenIndex));
				addActionToMap(actionMap, previousAction, previousPart);
				
				// Update loop status.
				previousAction = token;
				previousString = afterString;
			}
		} while(hasMatch);
		
		// Add the last string to the map.
		previousString = removeTrailingLineBreak(previousString);
		addActionToMap(actionMap, previousAction, previousString);
	}
	
	private static void addActionToMap(Map<String, String> actionMap,
			String action, String concatString) {
		if(concatString.length() > 0) {
			String previousMap = actionMap.getOrDefault(action, "");
			if(previousMap.length() > 0) previousMap = 
					removeTrailingLineBreak(previousMap) + "\n";
			actionMap.put(action, previousMap + concatString);
		}
	}
	
	private static String removeTrailingLineBreak(String line) {
		while(line.length() > 0 && (
				line.lastIndexOf('\n') == line.length() - 1
				|| line.lastIndexOf('\r') == line.length() - 1)) 
				line = line.substring(0, line.length() - 1);
		return line;
	}
}
