package tuprolog.language;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * class to store a set of elements that do not implement HashCode. We don't
 * extend ArrayList to shield it from use.
 *
 * @param <T>
 *            the type of the set elements.
 */
public class SetWithoutHash<T> implements Set<T> {
	private final List<T> set = new LinkedList<>();

	@Override
	public int size() {
		return this.set.size();
	}

	@Override
	public boolean isEmpty() {
		return this.set.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return this.set.contains(o);
	}

	@Override
	public Iterator<T> iterator() {
		return this.set.iterator();
	}

	@Override
	public Object[] toArray() {
		return this.set.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return this.set.toArray(a);
	}

	@Override
	public boolean add(T e) {
		if (!this.set.contains(e)) {
			return this.set.add(e);
		}
		return false;
	}

	@Override
	public boolean remove(Object o) {
		return this.set.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return this.set.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean changed = false;
		for (T element : c) {
			changed |= add(element);
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return this.set.retainAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return this.set.removeAll(c);
	}

	@Override
	public void clear() {
		this.set.clear();
	}

}
