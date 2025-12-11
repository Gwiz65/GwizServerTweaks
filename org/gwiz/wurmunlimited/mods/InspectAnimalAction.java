/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org/>
*/

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
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
import com.wurmonline.shared.util.StringUtilities;

public class InspectAnimalAction implements ActionPerformer, BehaviourProvider, ModAction {

	private final short actionId;
	private final ActionEntry actionEntry;

	public InspectAnimalAction() {
		actionId = (short) ModActions.getNextActionId();
		actionEntry = new ActionEntryBuilder(actionId, "Inspect animal", "inspecting").build();
		ModActions.registerAction(actionEntry);
	}

	@Override
	public boolean action(Action action, Creature performer, Creature target, short num, float counter) {
		performer.getCommunicator()
				.sendNormalServerMessage("You take a closer look at " + target.getNameWithGenus() + ".");
		final InspectQuestion question = new InspectQuestion(performer,
				StringUtilities.raiseFirstLetterOnly(target.getName()), "", -1L);
		question.inspectTarget = target;
		question.sendQuestion();
		return true;
	}

	@Override
	public boolean action(Action action, Creature performer, Item source, Creature target, short num, float counter) {
		return this.action(action, performer, target, num, counter);
	}

	@Override
	public List<ActionEntry> getBehavioursFor(Creature performer, Creature target) {
		if (performer instanceof Player && target != null && target.isAnimal()) {
			try {
				final Skill breeding = performer.getSkills().getSkill(10085);
				final double knowledge = breeding.getKnowledge(0.0);
				if (knowledge > 20.0) {
					return Arrays.asList(actionEntry);
				}
			} catch (NoSuchSkillException e) {
			}
		}
		return null;
	}

	@Override
	public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Creature target) {
		return this.getBehavioursFor(performer, target);
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
}