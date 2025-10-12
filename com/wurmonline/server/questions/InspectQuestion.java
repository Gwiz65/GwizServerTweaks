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
import com.wurmonline.server.players.Player;
import com.wurmonline.server.players.PlayerInfo;
import com.wurmonline.server.players.PlayerInfoFactory;
import com.wurmonline.server.skills.NoSuchSkillException;
import com.wurmonline.server.skills.Skill;
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
		((Player) this.getResponder()).setQuestion(null);
	}

	@Override
	public void sendQuestion() {
		final StringBuilder questionString = new StringBuilder();
		questionString.append(this.getBmlHeaderNoQuestion());
		questionString.append("text{text=''}");
		questionString.append("text{text='" + inspectTarget.examine() + "'}");

		final Brand brand = Creatures.getInstance().getBrand(inspectTarget.getWurmId());
		if (brand != null) {
			try {
				final Village village = Villages.getVillage((int) brand.getBrandId());
				questionString.append("text{text='It has been branded by and belongs to the settlement of "
						+ village.getName() + ".'}");
			} catch (NoSuchVillageException nsv) {
				brand.deleteBrand();
			}
		} else if (inspectTarget.isHorse()) {
			questionString.append("text{text='This horse is not currently branded.'}");
		} else {
			questionString.append("text{text='This aninimal cannot be branded.'}");
		}

		if (inspectTarget.isCaredFor()) {
			final long careTaker = inspectTarget.getCareTakerId();
			final PlayerInfo info = PlayerInfoFactory.getPlayerInfoWithWurmId(careTaker);
			if (info != null) {
				questionString.append("text{text='It is being taken care of by " + info.getName() + ".'}");
			} else {
				if (System.currentTimeMillis()
						- Players.getInstance().getLastLogoutForPlayer(careTaker) > 1209600000L) {
					Creatures.getInstance().setCreatureProtected(inspectTarget, -10L, false);
				}

			}
		} else {
			questionString.append("text{text='This animal is not currently being cared for.'}");
		}

		questionString.append(
				"text{text='" + StringUtilities.raiseFirstLetter(inspectTarget.getStatus().getBodyType()) + "'}");

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
			questionString.append("text{text='" + loyaltyString + "'}");
		} else {
			questionString.append("text{text='This animal is not tamed.'}");
		}

		if (inspectTarget.isDomestic()) {
			if (System.currentTimeMillis() - inspectTarget.getLastGroomed() > 172800000L) {
				questionString.append("text{text='This animal could use some grooming.'}");
			} else {
				questionString.append("text{text='This animal looks well groomed.'}");
			}
		} else {
			questionString.append("text{text='This animal cannot be groomed.'}");
		}

		if (inspectTarget.hasTraits()) {
			try {
				final Skill breeding = this.getResponder().getSkills().getSkill(10085);
				final double knowledge = breeding.getKnowledge(0.0);
				if (knowledge > 20.0) {
					final StringBuilder traitString = new StringBuilder();
					for (int x = 0; x < 64; ++x) {
						if (inspectTarget.hasTrait(x) && knowledge - 20.0 > x) {
							final String trait = Traits.getTraitString(x);
							if (trait.length() > 0) {
								traitString.append(trait);
								traitString.append(' ');
							}
						}
					}
					if (traitString.toString().length() > 0) {
						questionString.append("text{text='" + traitString.toString() + "'}");
					}
				}
			} catch (NoSuchSkillException nse) {
			}
		} else {
			questionString.append("text{text='This animal has no traits.'}");
		}

		if (inspectTarget.isHorse()) {
			questionString.append("text{text='Its colour is " + inspectTarget.getColourName() + ".'}");
		} else {
			questionString.append("text{text='This animal has no color info.'}");
		}

		if (inspectTarget.isPregnant()) {
			final Offspring offspring = inspectTarget.getOffspring();
			final int daysLeft = offspring.getDaysLeft();
			questionString.append("text{text='" + LoginHandler.raiseFirstLetter(inspectTarget.getHeSheItString())
					+ " will deliver in about " + daysLeft + ((daysLeft != 1) ? " days.'}" : " day.'}"));
		} else {
			questionString.append("text{text='This animal is not pregnant.'}");
		}

		String motherfather = "";
		if (inspectTarget.getMother() != -10L) {
			Creature mother;
			try {
				mother = Server.getInstance().getCreature(inspectTarget.getMother());
				motherfather = motherfather + StringUtilities.raiseFirstLetter(inspectTarget.getHisHerItsString())
						+ " mother is " + mother.getNameWithGenus() + ". ";
			} catch (NoSuchPlayerException | NoSuchCreatureException e) {
			}
		}
		if (inspectTarget.getFather() != -10L) {
			Creature father;
			try {
				father = Server.getInstance().getCreature(inspectTarget.getFather());
				motherfather = motherfather + StringUtilities.raiseFirstLetter(inspectTarget.getHisHerItsString())
						+ " father is " + father.getNameWithGenus() + ". ";
			} catch (NoSuchPlayerException | NoSuchCreatureException e) {
			}
		}
		if (motherfather.length() > 0) {
			questionString.append("text{text='" + motherfather + "'}");
		} else {
			questionString.append("text{text='No family information for this animal'}");
		}

		if (inspectTarget.hasTraits()) {
			List<Integer> traitArrayList = new ArrayList<>();
			for (int x = 0; x < 64; ++x) {
				if (inspectTarget.hasTrait(x) && this.isActualTrait(x)) {
					traitArrayList.add(x);
				}
			}

			questionString.append("text{text='" + traitArrayList.toString() + "'}");
			for (int id : traitArrayList) {
				questionString.append(
						"text{text='" + id + "  " + this.getPosNegString(id) + "  " + Traits.getTraitString(id) + "'}");
			}
		}

		questionString.append("text{text=''}");
		questionString.append(this.createAnswerButton2("Close"));
		this.getResponder().getCommunicator().sendBml(600, 400, true, true, questionString.toString(), 200, 200, 200,
				this.title);
	}

	private boolean isActualTrait(int t) {
		// these are colors so ignore
		// 15 16 17 18 23 24 25 30 31 32 33 34
		// 63 is captivity trait
		if (t != 15 && t != 16 && t != 17 && t != 18 && t != 23 && t != 24 && t != 15 && t != 25 && t != 30 && t != 31
				&& t != 32 && t != 33 && t != 34 && t != 63) {
			return true;
		} else {
			return false;
		}
	}

	private String getPosNegString(int t) {
		String retString = "Positive";
		if (Traits.isTraitNegative(t))
			retString = "Negative";
		if (Traits.isTraitNeutral(t))
			retString = "Neutral";
		return retString;
	}
}