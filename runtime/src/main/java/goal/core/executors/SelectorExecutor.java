package goal.core.executors;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import events.Channel;
import events.ExecutionEventGeneratorInterface;
import goal.core.agent.AgentRegistry;
import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.exceptions.GOALRuntimeErrorException;
import krTools.KRInterface;
import krTools.language.Term;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.selector.Selector;
import mentalState.error.MSTTranslationException;
import mentalState.translator.Translator;
import msFactory.InstantiationFailedException;
import msFactory.translator.TranslatorFactory;

/**
 * Extends selector from mental state project with a method to evaluate a
 * selector using an agent registry.
 */
public class SelectorExecutor extends mentalState.executors.SelectorExecutor {
	public SelectorExecutor(Selector selector) {
		super(selector);
	}

	public List<AgentId> evaluate(RunState state) {
		AgentId aid = state.getId();
		AgentRegistry<?> registry = state.getRegistry();
		ExecutionEventGeneratorInterface events = state.getEventGenerator();
		List<AgentId> agents = new LinkedList<>();
		switch (getSelector().getType()) {
		default:
		case THIS:
		case SELF:
			agents.add(aid);
			break;
		case ALL: // ALL is inclusive; keep own aid.
			agents.addAll(registry.getRegisteredAgents());
			break;
		case ALLOTHER: // ALLOTHER is exclusive; remove own aid.
			List<AgentId> registered1 = new ArrayList<>(registry.getRegisteredAgents());
			registered1.remove(aid);
			if (registered1.isEmpty()) {
				events.event(Channel.WARNING, new Warning("no other agent(s) to send messages to."),
						getSelector().getSourceInfo());
			} else {
				agents.addAll(registered1);
			}
			break;
		case SOME: // SOME is inclusive; keep own aid.
			List<AgentId> registered2 = new ArrayList<>(registry.getRegisteredAgents());
			int pick1 = new Random().nextInt(registered2.size());
			agents.add(registered2.get(pick1));
			break;
		case SOMEOTHER: // SOMEOTHER is exclusive; remove own aid.
			List<AgentId> registered3 = new ArrayList<>(registry.getRegisteredAgents());
			registered3.remove(aid);
			if (registered3.isEmpty()) {
				events.event(Channel.WARNING, new Warning("no other agent to send messages to."),
						getSelector().getSourceInfo());
			} else {
				int pick2 = new Random().nextInt(registered3.size());
				agents.add(registered3.get(pick2));
			}
			break;
		case PARAMETERLIST:
			try {
				Translator translator = getTranslator(state.getKRI());
				for (Term t : getSelector().getParameters()) {
					for (Term groundterm : translator.unpackTerm(t)) {
						String name = groundterm.toString();
						// Strip single or double quotes in term's name, if any.
						if (name.startsWith("\'") || name.startsWith("\"")) {
							name = name.substring(1, name.length() - 1);
						}
						// Try to find the referenced agent or channel...
						if (registry.isChannel(name)) {
							agents.addAll(registry.getSubscribers(name));
						} else {
							agents.add(new AgentId(name));
						}
					}
				}
			} catch (InstantiationFailedException | MSTTranslationException e) {
				throw new GOALRuntimeErrorException("translating selector '" + getSelector() + "' failed.", e);
			}
			break;
		case VARIABLE:
			throw new GOALRuntimeErrorException(
					"variable selector '" + getSelector() + "' should have been instantiated at this point.");
		}
		return agents;
	}

	/**
	 * Factory method
	 *
	 * @param kri
	 * @return translator for kri
	 * @throws InstantiationFailedException
	 */
	public Translator getTranslator(KRInterface kri) throws InstantiationFailedException {
		return TranslatorFactory.getTranslator(kri);
	}
}