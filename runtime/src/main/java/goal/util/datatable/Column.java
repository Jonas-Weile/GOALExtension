package goal.util.datatable;

/**
 * Prototype column description for Data Table.
 */
public class Column {

	private String description;

	public Column(String name) {
		this.description = name;
	}

	public String getDescription() {
		return this.description;
	}

	@Override
	public String toString() {
		return this.description;
	}
}
