package goal.tools.history.events;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import eis.iilang.Percept;
import goal.core.executors.actions.ActionExecutor;
import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import krTools.KRInterface;
import krTools.language.DatabaseFormula;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Update;
import krTools.parser.SourceInfo;
import languageTools.program.ProgramMap;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.actions.AdoptAction;
import languageTools.program.agent.actions.DeleteAction;
import languageTools.program.agent.actions.DropAction;
import languageTools.program.agent.actions.InsertAction;
import languageTools.program.agent.actions.MentalAction;
import languageTools.program.agent.msg.Message;
import languageTools.program.agent.selector.Selector;
import languageTools.program.agent.selector.Selector.SelectorType;
import mentalState.BASETYPE;
import mentalState.error.MSTTranslationException;
import mentalState.translator.Translator;
import msFactory.InstantiationFailedException;
import msFactory.translator.TranslatorFactory;

// Note that this class is quite similar to mentalState.Result
public class ModificationAction {
	private final int base;
	private final String selector;
	private final List<DatabaseFormula> added;
	private final List<DatabaseFormula> removed;
	private final int info;

	public ModificationAction(final RunState runState, final BASETYPE base, final String selector,
			final List<DatabaseFormula> added, final List<DatabaseFormula> removed, final SourceInfo info) {
		this.base = base.ordinal();
		this.selector = selector;
		this.added = added;
		this.removed = removed;
		this.info = runState.getMap().getIndex(info);
	}

	public BASETYPE getBase() {
		return BASETYPE.values[this.base];
	}

	public List<DatabaseFormula> getAdded() {
		return this.added;
	}

	public List<DatabaseFormula> getRemoved() {
		return this.removed;
	}

	public SourceInfo getSourceInfo(final ProgramMap map) {
		return map.getObject(this.info).getSourceInfo();
	}

	public void execute(final RunState runState, final boolean reverse) throws GOALActionFailedException {
		final KRInterface kri = runState.getKRI();
		// TODO: use module/action context instead of empty subst
		final Substitution empty = kri.getSubstitution(null);
		final SourceInfo info = getSourceInfo(runState.getMap());
		switch (getBase()) {
		case BELIEFBASE:
			try {
				final Translator translator = TranslatorFactory.getTranslator(kri);
				final Selector select = getSelector(runState, translator);
				if (!this.added.isEmpty()) {
					final Update added = translator.makeUpdate(this.added);
					final MentalAction beliefaction1 = reverse ? new DeleteAction(select, added, info)
							: new InsertAction(select, added, info);
					final ActionExecutor exec1 = ActionExecutor.getActionExecutor(beliefaction1, empty);
					exec1.execute(runState);
				}
				if (!this.removed.isEmpty()) {
					final Update removed = translator.makeUpdate(this.removed);
					final MentalAction beliefaction2 = reverse ? new InsertAction(select, removed, info)
							: new DeleteAction(select, removed, info);
					final ActionExecutor exec2 = ActionExecutor.getActionExecutor(beliefaction2, empty);
					exec2.execute(runState);
				}
			} catch (final MSTTranslationException | InstantiationFailedException e1) {
				throw new GOALActionFailedException("failed to execute belief modification.", e1);
			}
			break;
		case GOALBASE:
			try {
				final Translator translator = TranslatorFactory.getTranslator(kri);
				final Selector select = getSelector(runState, translator);
				if (!this.added.isEmpty()) {
					final Update added = translator.makeUpdate(this.added);
					final MentalAction goalaction1 = reverse ? new DropAction(select, added, info)
							: new AdoptAction(select, added, info);
					final ActionExecutor exec1 = ActionExecutor.getActionExecutor(goalaction1, empty);
					exec1.execute(runState);
				}
				if (!this.removed.isEmpty()) {
					final Update removed = translator.makeUpdate(this.removed);
					final MentalAction goalaction2 = reverse ? new AdoptAction(select, removed, info)
							: new DropAction(select, removed, info);
					final ActionExecutor exec2 = ActionExecutor.getActionExecutor(goalaction2, empty);
					exec2.execute(runState);
				}
			} catch (final MSTTranslationException | InstantiationFailedException e2) {
				throw new GOALActionFailedException("failed to execute goal modification.", e2);
			}
			break;
		case PERCEPTBASE:
			final List<Percept> addList1 = new ArrayList<>(this.added.size());
			final List<Percept> delList1 = new ArrayList<>(this.removed.size());
			try {
				final Translator translator = TranslatorFactory.getTranslator(kri);
				for (final DatabaseFormula add : this.added) {
					final Percept toAdd = translator.convertPercept(add);
					addList1.add(toAdd);
				}
				for (final DatabaseFormula remove : this.removed) {
					final Percept toRemove = translator.convertPercept(remove);
					delList1.add(toRemove);
				}
			} catch (final MSTTranslationException | InstantiationFailedException e) {
				throw new GOALActionFailedException("failed to prepare percept modification.", e);
			}
			if (reverse) {
				runState.updatePercepts(delList1, addList1);
			} else {
				runState.updatePercepts(addList1, delList1);
			}
			break;
		case MESSAGEBASE:
			final Set<Message> addList2 = new LinkedHashSet<>(this.added.size());
			final Set<Message> delList2 = new LinkedHashSet<>(this.removed.size());
			try {
				final Translator translator = TranslatorFactory.getTranslator(kri);
				for (final DatabaseFormula add : this.added) {
					final Message toAdd = translator.convertMessage(add);
					addList2.add(toAdd);
				}
				for (final DatabaseFormula remove : this.removed) {
					final Message toRemove = translator.convertMessage(remove);
					delList2.add(toRemove);
				}
			} catch (final MSTTranslationException | InstantiationFailedException e) {
				throw new GOALActionFailedException("failed to prepare message modification.", e);
			}
			if (reverse) {
				runState.updateMessages(delList2, addList2);
			} else {
				runState.updateMessages(addList2, delList2);
			}
			break;
		default:
			throw new GOALActionFailedException("cannot execute a modification on base '" + this.base + "'.");
		}
	}

	private Selector getSelector(final RunState runState, final Translator translator)
			throws MSTTranslationException, InstantiationFailedException {
		if (getBase() == BASETYPE.GOALBASE) {
			return new Selector(SelectorType.valueOf(this.selector), null);
		} else {
			final AgentId agent = (this.selector == null) ? runState.getId() : new AgentId(this.selector);
			final Term term = translator.convert(agent);
			final List<Term> termList = new ArrayList<>(1);
			termList.add(term);
			return new Selector(termList, null);
		}
	}

	@Override
	public String toString() {
		final mentalState.Result result = new mentalState.Result(getBase(), this.selector) {
			@Override
			protected Translator getTranslator() {
				// FIXME: percepts/messages are untranslated now
				return null;
			}
		};
		result.added(this.added);
		result.removed(this.removed);
		final String returned = result.toString();
		return returned.substring(0, returned.length() - 1);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = this.info;
		result = prime * result + this.base;
		result = prime * result + ((this.selector == null) ? 0 : this.selector.hashCode());
		result = prime * result + ((this.added == null) ? 0 : this.added.hashCode());
		result = prime * result + ((this.removed == null) ? 0 : this.removed.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof ModificationAction)) {
			return false;
		}
		ModificationAction other = (ModificationAction) obj;
		if (this.base != other.base) {
			return false;
		}
		if (this.selector == null) {
			if (other.selector != null) {
				return false;
			}
		} else if (!this.selector.equals(other.selector)) {
			return false;
		}
		if (this.added == null) {
			if (other.added != null) {
				return false;
			}
		} else if (!this.added.equals(other.added)) {
			return false;
		}
		if (this.removed == null) {
			if (other.removed != null) {
				return false;
			}
		} else if (!this.removed.equals(other.removed)) {
			return false;
		}
		return (this.info == other.info);
	}
}
