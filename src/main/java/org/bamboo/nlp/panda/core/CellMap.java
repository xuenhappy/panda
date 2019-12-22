package org.bamboo.nlp.panda.core;

public class CellMap implements HtmlVisually{
	public static class Node{
		private final WordCell val;
		private Node next;
		public Node(WordCell val, Node next) {
			super();
			this.val = val;
			this.next = next;
		}
	}
	
	/**
	 * data
	 */
	private Node data;
	private int rownum;
	private int colnum;
	
	
	
	/**
	 * 
	 * @param cell
	 */
	public void addCell(WordCell cell) {
		
	}
	
	
	public Node addNext(Node pre,WordCell cell) {
		return null;
	}



	@Override
	public String toHtml() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
	

}
