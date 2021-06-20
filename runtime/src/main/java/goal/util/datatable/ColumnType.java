package goal.util.datatable;

/**
 * interface, represents the type of a column (the abstract header). It provides
 * a function to get concrete text for the column header.
 */
public interface ColumnType {

	/**
	 *
	 * @return textual description of this column (to be used as column header in
	 *         the displayed table)
	 */
	public String getDescription();
}
