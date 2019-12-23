package org.bamboo.nlp.panda.tools;

import java.util.ArrayList;
import java.util.Iterator;

public class SimpleStrList implements StrList {
	private final ArrayList<String> datas;
	private final String alls;

	public SimpleStrList(ArrayList<String> datas) {
		this.datas = datas;
		this.alls = String.join(" ", datas);
	}

	@Override
	public Iterator<CharSequence> iterator() {
		final Iterator<String> c = datas.iterator();
		return new Iterator<CharSequence>() {

			@Override
			public boolean hasNext() {
				return c.hasNext();
			}

			@Override
			public CharSequence next() {
				return c.next();
			}
		};
	}

	@Override
	public int size() {
		return datas.size();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alls == null) ? 0 : alls.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleStrList other = (SimpleStrList) obj;
		if (alls == null) {
			if (other.alls != null)
				return false;
		} else if (!alls.equals(other.alls))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SimpleStrList [alls=" + alls + "]";
	}

}
