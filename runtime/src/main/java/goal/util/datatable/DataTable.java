package goal.util.datatable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.table.TableModel;

/**
 * A set of {@link DataRow}s with general data. For display and sorting
 * purposes, as data model between the real data model and an actual table to be
 * displayed. Supports stronger typechecking than {@link TableModel}. Can be
 * sorted and filtered as desired. Mutable. not thread safe. TODO We can easily
 * modify this to implement TableModel so that this data can also be used
 * directly for display in a JTable.
 *
 */
public class DataTable {
	private List<DataRow> rows = new ArrayList<>();
	private Set<ColumnType> columns = new LinkedHashSet<>();

	/**
	 * add new row.
	 */
	public void add(DataRow row) {
		this.rows.add(row);
		this.columns.addAll(row.columns());
	}

	/**
	 * Modifies the datatable so that the rows are ordered using given comparator.
	 *
	 * @param comparator
	 *            a {@link Comparator} of {@link DataRow} elements.
	 */
	public void sort(Comparator<DataRow> comparator) {
		Collections.sort(this.rows, comparator);
	}

	public boolean isEmpty() {
		return this.rows.isEmpty();
	}

	/**
	 * Get all the columns that we have in the table.
	 */
	public Set<ColumnType> columns() {
		return Collections.unmodifiableSet(this.columns);
	}

	/**
	 * @return all the {@link DataRow}s from the table. Do not modify the returned
	 *         table.
	 */
	public List<DataRow> getData() {
		return Collections.unmodifiableList(this.rows);
	}

	/**
	 * convert list of values to a string, using given separator as separator.
	 *
	 * @param cols
	 *
	 * @param sep
	 *            the separator character to use
	 * @return data row, as a single string with given separator char.
	 */
	public String header(List<ColumnType> cols, String sep) {
		String res = "";
		for (ColumnType col : cols) {
			if (!res.isEmpty()) {
				res = res + sep;
			}
			res = res + col.getDescription();
		}
		return res;
	}
}
