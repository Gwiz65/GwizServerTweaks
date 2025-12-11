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

package com.wurmonline.server.questions;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.wurmonline.server.LoginHandler;
import com.wurmonline.server.NoSuchPlayerException;
import com.wurmonline.server.Players;
import com.wurmonline.server.Server;
import com.wurmonline.server.creatures.Brand;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.Creatures;
import com.wurmonline.server.creatures.NoSuchCreatureException;
import com.wurmonline.server.creatures.Offspring;
import com.wurmonline.server.creatures.Traits;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.villages.NoSuchVillageException;
import com.wurmonline.server.villages.Village;
import com.wurmonline.server.villages.Villages;
import com.wurmonline.shared.util.StringUtilities;

public final class InspectQuestion extends Question {

	public Creature inspectTarget;

	public InspectQuestion(final Creature aResponder, final String aTitle, final String aQuestion, final long aTarget) {
		super(aResponder, aTitle, aQuestion, 870, aTarget);
	}

	@Override
	public void answer(final Properties answers) {
		this.setAnswer(answers);
		if (this.type == 0) {
			return;
		}
		for (final String key : this.getAnswer().stringPropertyNames()) {
			if (key.startsWith("sort")) {
				final InspectQuestion question = new InspectQuestion(this.getResponder(), this.getTitle(), "", -1L);
				question.inspectTarget = this.inspectTarget;
				question.sendQuestion();
				return;
			}
		}
	}

	@Override
	public void sendQuestion() {
		final StringBuilder questionString = new StringBuilder();
		questionString.append(getBmlHeaderNoQuestion());
		questionString.append(bmlBlank());
		questionString.append("label{text=\"" + this.title + "\";type=\"bold\";color=\"255,255,0\"}");
		String line1 = " ";
		String line2 = " ";
		final String examineText = inspectTarget.examine();
		if (examineText.length() > 90) {
			final int split = examineText.lastIndexOf(" ", 90);
			if (split > 0) {
				line1 = examineText.substring(0, split);
				line2 = examineText.substring(split + 1);
			}
		} else {
			line1 = examineText;
		}
		questionString.append("label{text=\"" + line1 + "\";type=\"italic\";color=\"255,255,125\"}");
		questionString.append("label{text=\"" + line2 + "\";type=\"italic\";color=\"255,255,125\"}");
		questionString.append("harray{varray{");
		questionString.append(bmlLabel(StringUtilities.raiseFirstLetter(inspectTarget.getStatus().getBodyType())));
		if (inspectTarget.hasTrait(63)) {
			questionString.append(bmlLabel("It has been breed in captivity."));
		} else {
			questionString.append(bmlLabel("This is a wild creature."));
		}
		final Brand brand = Creatures.getInstance().getBrand(inspectTarget.getWurmId());
		if (brand != null) {
			try {
				final Village village = Villages.getVillage((int) brand.getBrandId());
				questionString.append(bmlLabel("It has the brand of " + village.getName() + "."));
			} catch (NoSuchVillageException nsv) {
				brand.deleteBrand();
				questionString.append(bmlLabel("This horse is not currently branded."));
			}
		} else if (inspectTarget.isHorse()) {
			questionString.append(bmlLabel("This horse is not currently branded."));
		} else {
			questionString.append(bmlLabel("This aninimal cannot be branded."));
		}
		if (inspectTarget.isCaredFor()) {
			final long careTaker = inspectTarget.getCareTakerId();
			final PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(careTaker);
			if (info != null) {
				questionString.append(bmlLabel("It is being taken care of by " + info.getName() + "."));
			} else {
				if (System.currentTimeMillis()
						- Players.getInstance().getLastLogoutForPlayer(careTaker) > 1209600000L) {
					Creatures.getInstance().setCreatureProtected(inspectTarget, -10L, false);
				}
				questionString.append(bmlLabel("This animal is not currently being cared for."));
			}
		} else {
			questionString.append(bmlLabel("This animal is not currently being cared for."));
		}
		if (inspectTarget.isDominated()) {
			final float loyalty = inspectTarget.getLoyalty();
			String loyaltyString = "";
			if (loyalty < 10.0f) {
				loyaltyString = "This animal looks upset.";
			} else if (loyalty < 20.0f) {
				loyaltyString = "This animal acts nervously.";
			} else if (loyalty < 30.0f) {
				loyaltyString = "This animal looks submissive.";
			} else if (loyalty < 40.0f) {
				loyaltyString = "This animal looks calm.";
			} else if (loyalty < 50.0f) {
				loyaltyString = "This animal looks tame.";
			} else if (loyalty < 60.0f) {
				loyaltyString = "This animal acts loyal.";
			} else if (loyalty < 70.0f) {
				loyaltyString = "This animal looks trusting.";
			} else if (loyalty < 100.0f) {
				loyaltyString = "This animal looks extremely loyal.";
			}
			questionString.append(bmlLabel(loyaltyString));
		} else {
			questionString.append(bmlLabel("This animal is not tamed."));
		}
		if (inspectTarget.isPregnant()) {
			final Offspring offspring = inspectTarget.getOffspring();
			final int daysLeft = offspring.getDaysLeft();
			questionString.append(bmlLabel(LoginHandler.raiseFirstLetter(inspectTarget.getHeSheItString())
					+ " will deliver in " + daysLeft + ((daysLeft != 1) ? " days." : " day.")));
		} else {
			questionString.append(bmlLabel("This animal is not pregnant."));
		}
		questionString.append("};varray{label{text=\"  \"}};varray{");
		if (inspectTarget.isHorse()) {
			questionString.append(bmlLabel("Its colour is " + inspectTarget.getColourName() + "."));
		} else {
			questionString.append(bmlLabel("This animal has normal colouring."));
		}
		if (inspectTarget.getMother() != -10L) {
			Creature mother;
			try {
				mother = Server.getInstance().getCreature(inspectTarget.getMother());
				questionString.append(bmlLabel(StringUtilities.raiseFirstLetter(inspectTarget.getHisHerItsString())
						+ " mother is " + mother.getNameWithGenus() + "."));
			} catch (NoSuchPlayerException | NoSuchCreatureException e) {
			}
		} else {
			questionString.append(bmlLabel(
					StringUtilities.raiseFirstLetter(inspectTarget.getHisHerItsString()) + " mother is unknown."));
		}
		if (inspectTarget.getFather() != -10L) {
			Creature father;
			try {
				father = Server.getInstance().getCreature(inspectTarget.getFather());
				questionString.append(bmlLabel(StringUtilities.raiseFirstLetter(inspectTarget.getHisHerItsString())
						+ " father is " + father.getNameWithGenus() + "."));
			} catch (NoSuchPlayerException | NoSuchCreatureException e) {
			}
		} else {
			questionString.append(bmlLabel(
					StringUtilities.raiseFirstLetter(inspectTarget.getHisHerItsString()) + " father is unknown."));
		}
		if (inspectTarget.isDomestic()) {
			if (inspectTarget.canBeGroomed()) {
				questionString.append(bmlLabel("This animal could use some grooming."));
			} else {
				questionString.append(bmlLabel("This animal looks well groomed."));
			}
		} else {
			questionString.append(bmlLabel("This animal cannot be groomed."));
		}
		if (inspectTarget.isMilkable()) {
			if (inspectTarget.isMilked()) {
				questionString.append(bmlLabel("This aninimal has already been milked."));
			} else {
				questionString.append(bmlLabel("You can milk this animal."));
			}
		} else {
			questionString.append(bmlLabel("This aninimal cannot be milked."));
		}
		if (inspectTarget.isWoolProducer()) {
			if (inspectTarget.isSheared()) {
				questionString.append(bmlLabel("This aninimal has already been sheared."));
			} else {
				questionString.append(bmlLabel("You can shear this animal."));
			}
		} else {
			questionString.append(bmlLabel("This aninimal cannot be sheared."));
		}
		questionString.append("}}");
		questionString.append(bmlBlank());
		List<Integer> traitArrayList = new ArrayList<>();
		if (inspectTarget.hasTraits()) {
			for (int t = 0; t < 64; ++t) {
				if (inspectTarget.hasTrait(t) && !Traits.isTraitNegative(t) && t != 15 && t != 16 && t != 17 && t != 18
						&& t != 22 && t != 23 && t != 24 && t != 25 && t != 27 && t != 28 && t != 29 && t != 30
						&& t != 31 && t != 32 && t != 33 && t != 34 && t != 63) {
					traitArrayList.add(t);
				}
			}
			for (int t = 0; t < 64; ++t) {
				if (inspectTarget.hasTrait(t) && Traits.isTraitNegative(t) && t != 15 && t != 16 && t != 17 && t != 18
						&& t != 22 && t != 23 && t != 24 && t != 25 && t != 27 && t != 28 && t != 29 && t != 30
						&& t != 31 && t != 32 && t != 33 && t != 34 && t != 63) {
					traitArrayList.add(t);
				}
			}
		}
		questionString.append(bmlLabel(" Total number of traits: " + traitArrayList.size()));
		questionString.append("table{rows=\"1\";cols=\"4\";label{text=\"\"};" + this.colHeader("   Type   ", 1, 0)
				+ this.colHeader("                        Trait                        ", 2, 0)
				+ this.colHeader("                          Effect                         ", 3, 0));
		if (traitArrayList.size() > 0) {
			for (int id : traitArrayList) {
				questionString.append("label{text=\"\"};label{text=\"" + getPosNegString(id) + "\"};label{text=\""
						+ getTraitBySkill(id) + "\"};label{text=\"" + getEffectString(id) + "\"};");
			}
		}
		questionString.append("}");
		if (traitArrayList.size() < 8) {
			for (int x = 0; x < 8 - traitArrayList.size(); ++x) {
				questionString.append(bmlBlank());
			}
		}
		questionString.append(bmlBlank());
		questionString.append(
				"harray {label{text=\"                                                             \"};button{text="
						+ "\" Close \";id=\"submit\"}}}};null;null;}");
		this.getResponder().getCommunicator().sendBml(570, 388, true, true, questionString.toString(), 200, 200, 200,
				this.title);
	}

	final private String bmlBlank() {
		return "label{text=\" \"};";
	}

	final private String bmlLabel(final String text) {
		return "label{text=\"" + text + "\"};";
	}

	final private String getTraitBySkill(int t) {
		try {
			if (this.getResponder().getSkills().getSkill(10085).getKnowledge(0.0) - 20.0 > t) {
				return Traits.getTraitString(t);
			}
		} catch (NoSuchSkillException e) {
		}
		return "                          ???";
	}

	final private String getPosNegString(int t) {
		String retString = "Positive\";color=\"0,255,0";
		if (Traits.isTraitNegative(t)) {
			retString = "Negative\";color=\"255,0,0";
		}
		return retString;
	}

	final private String getEffectString(int t) {
		try {
			if (this.getResponder().getSkills().getSkill(10085).getKnowledge(0.0) - 20.0 > t) {
				switch (t) {
				case 0:
					return "Higher fighting skill.";
				case 1:
					return "Minor speed boost.";
				case 2:
					return "Withstands more damage.";
				case 3:
					return "Bonus to mounted weight limit.";
				case 4:
					return "Major speed boost.";
				case 5:
					return "Major bonus to mounted weight limit.";
				case 6:
					return "Minor speed boost & weight limit bonus.";
				case 7:
					return "Able to sense when on a water tile.";
				case 8:
					return "Minor speed penalty.";
				case 9:
					return "Major speed penalty.";
				case 10:
					return "Random chance to bite.";
				case 11:
					return "Major penalty to mounted weight limit.";
				case 12:
					return "Will stop being led at random.";
				case 13:
					return "Slow body strength reduction.";
				case 14:
					return "Becomes hungry twice as fast as normal.";
				case 19:
					return "Prone to catching a disease.";
				case 20:
					return "Has a higher resistance to disease.";
				case 21:
					return "Lives 50% longer than normal.";
				}
			}
		} catch (NoSuchSkillException e) {
		}
		return "                             ???";
	}
}