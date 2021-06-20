package goal.util.datatable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import goal.tools.logging.CsvFormatter;

/**
 * Contains a row of general data. Ment as output formatter for data.
 */
public class DataRow {
	private Map<ColumnType, Object> data;

	/**
	 * Create new data object.
	 *
	 * @param data
	 *            a Map with the keys Strings: the headers/column names, and
	 *            Object the data for each column. If the order of the data is
	 *            relevant, you should provide a map that respects the order,
	 *            like {@link LinkedHashMap}.
	 */
	public DataRow(Map<ColumnType, Object> data) {
		this.data = data;
	}

	/**
	 * @return the header data. See also {@link #dataString(Set, String)}
	 */
	public List<ColumnType> columns() {
		List<ColumnType> cols = new ArrayList<>();
		cols.addAll(this.data.keySet());
		return cols;
	}

	/**
	 * @param name
	 * @return the column with the given name, or null if no such data
	 */
	public Object column(ColumnType col) {
		return this.data.get(col);
	}

	/**
	 * @return the actual data. See also {@link #dataString(Set, String)}
	 */
	public List<Object> data() {
		List<Object> list = new ArrayList<>();
		for (Object key : this.data.keySet()) {
			list.add(this.data.get(key));
		}
		return list;
	}

	/**
	 *
	 * @param cols
	 *            the requested columns.
	 * @param sep
	 *            the separator to use
	 * @return string with the data for the requested columns. Each column is
	 *         escaped - see {@link CsvFormatter#escape(String)}
	 */
	public String format(List<ColumnType> cols, String sep) {
		String res = "";
		for (ColumnType col : cols) {
			if (!res.isEmpty()) {
				res += sep;
			}
			Object valobject = this.data.get(col);
			String valstring = (valobject == null) ? "--" : valobject.toString();
			res += CsvFormatter.escape(valstring);
		}
		return res;
	}

	@Override
	public String toString() {
		return this.data.toString();
	}
}
