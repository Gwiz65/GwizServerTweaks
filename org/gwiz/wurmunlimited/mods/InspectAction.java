package org.gwiz.wurmunlimited.mods;

import java.util.Arrays;
import java.util.List;

import org.gotti.wurmunlimited.modsupport.actions.ActionEntryBuilder;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.questions.InspectQuestion;
import com.wurmonline.shared.util.StringUtilities;

public class InspectAction implements ActionPerformer, BehaviourProvider, ModAction {

	private final short actionId;
	private final ActionEntry actionEntry;

	public InspectAction() {
		actionId = (short) ModActions.getNextActionId();
		actionEntry = new ActionEntryBuilder(actionId, "Inspect animal", "inspecting", new int[] { 0, 25, 29, 37, 43 })
				.build();
		ModActions.registerAction(actionEntry);
	}

	@Override
	public boolean action(Action action, Creature performer, Creature target, short num, float counter) {
		performer.getCommunicator()
				.sendNormalServerMessage("You take a closer look at " + target.getNameWithGenus() + ".");
		final InspectQuestion question = new InspectQuestion((Player) performer,
				StringUtilities.raiseFirstLetter(target.getName()), "", -1L);
		question.inspectTarget = target;
		question.sendQuestion();
		return true;
	}

	@Override
	public boolean action(Action action, Creature performer, Item source, Creature target, short num, float counter) {
		return this.action(action, performer, target, num, counter);
	}

	@Override
	public short getActionId() {
		return actionId;
	}

	@Override
	public ActionPerformer getActionPerformer() {
		return this;
	}

	@Override
	public BehaviourProvider getBehaviourProvider() {
		return this;
	}

	@Override
	public List<ActionEntry> getBehavioursFor(Creature performer, Creature target) {
		if (performer instanceof Player && target != null && target.isAnimal()) {
			return Arrays.asList(actionEntry);
		} else {
			return null;
		}
	}

	@Override
	public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Creature target) {
		return this.getBehavioursFor(performer, target);
	}

}