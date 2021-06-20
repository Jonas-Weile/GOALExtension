package languageTools.program;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import krTools.parser.ParsedObject;
import krTools.parser.SourceInfo;
import languageTools.program.agent.Module;

public class ProgramMap {
	private final Map<SourceInfo, Integer> sources = new LinkedHashMap<>();
	private final Map<Integer, ParsedObject> objects = new LinkedHashMap<>();
	private int counter = 0;

	public void register(ParsedObject object) {
		final int register = this.counter++;
		if (object instanceof Module) {
			this.sources.put(((Module) object).getDefinition(), register);
		} else {
			this.sources.put(object.getSourceInfo(), register);
		}
		this.objects.put(register, object);
	}

	public boolean isEmpty() {
		return (this.counter == 0);
	}

	public List<ParsedObject> getAll() {
		return new ArrayList<>(this.objects.values());
	}

	public ParsedObject getObject(int index) {
		return this.objects.get(index);
	}

	public ParsedObject getObject(SourceInfo info) {
		return getObject(this.sources.get(info));
	}

	public int getIndex(SourceInfo info) {
		return this.sources.get(info);
	}

	public void merge(ProgramMap map) {
		for (ParsedObject object : map.objects.values()) {
			register(object);
		}
	}
}
