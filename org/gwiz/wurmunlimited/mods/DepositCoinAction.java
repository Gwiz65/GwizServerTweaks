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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.gotti.wurmunlimited.modsupport.actions.ActionEntryBuilder;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.economy.Economy;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import com.wurmonline.shared.util.MaterialUtilities;

public class DepositCoinAction implements ActionPerformer, BehaviourProvider, ModAction {

	private final short actionId;
	private final ActionEntry actionEntry;

	public DepositCoinAction() {
		actionId = (short) ModActions.getNextActionId();
		actionEntry = new ActionEntryBuilder(actionId, "Deposit coin", "depositing").build();
		ModActions.registerAction(actionEntry);
	}

	@Override
	public boolean action(Action action, Creature performer, Item target, short num, float counter) {
		if (target.isCoin()) {
			final Player player = (Player) performer;
			try {
				target.getParent().dropItem(target.getWurmId(), false);
				Economy.getEconomy().returnCoin(target, "Banked");
				player.addMoney(target.getValue());
				player.getCommunicator().sendNormalServerMessage(
						"You deposit a " + MaterialUtilities.getMaterialString(target.getMaterial()) + " "
								+ target.getName() + " into your bank account.");
				player.getCommunicator().sendNormalServerMessage(
						"New balance: " + (Economy.getEconomy().getChangeFor(player.getMoney())).getChangeString());
			} catch (IOException | NoSuchItemException e) {
				if (target.isBanked()) {
					Item newCoin = Economy.getEconomy().getCoinsFor(target.getValue())[0];
					player.getInventory().insertItem(newCoin);
				}
				player.getCommunicator().sendNormalServerMessage(
						"Unable to deposit a " + MaterialUtilities.getMaterialString(target.getMaterial()) + " "
								+ target.getName() + " into your bank account.");
			}
		}
		return true;
	}

	@Override
	public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {
		if (performer instanceof Player && target != null && target.isCoin()) {
			// performer.getCommunicator().sendUpdateInventoryItem(target);
			for (Item item : performer.getAllItems()) {
				if (item.getWurmId() == target.getWurmId()) {
					return Arrays.asList(actionEntry);
				}
			}
		}
		return null;
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