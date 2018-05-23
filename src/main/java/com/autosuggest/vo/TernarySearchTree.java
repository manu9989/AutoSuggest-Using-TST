package com.autosuggest.vo;

/**
 * Node for Ternary Search Tree
 * 
 * @author Manu
 * 
 */
public class TernarySearchTree {

	/**
	 * Hold the character
	 */
	private Character character;

	/**
	 * Reference to node whose character value is less than the current node's
	 * character
	 */
	private TernarySearchTree left;

	/**
	 * Reference to node whose character value is equal to current node's
	 * character
	 */
	private TernarySearchTree mid;

	/**
	 * Reference to node whose character value is greater than the current
	 * node's character
	 */
	private TernarySearchTree right;

	/**
	 * If the character marks end of a word
	 */
	private boolean isWord;

	public TernarySearchTree() {
	}

	public TernarySearchTree(Character character) {
		this.character = character;
	}

	public char getCharacter() {
		return character;
	}

	public void setCharacter(char character) {
		this.character = character;
	}

	public TernarySearchTree getLeft() {
		return left;
	}

	public void setLeft(TernarySearchTree left) {
		this.left = left;
	}

	public TernarySearchTree getMid() {
		return mid;
	}

	public void setMid(TernarySearchTree mid) {
		this.mid = mid;
	}

	public TernarySearchTree getRight() {
		return right;
	}

	public void setRight(TernarySearchTree right) {
		this.right = right;
	}

	public boolean isWord() {
		return isWord;
	}

	public void setWord(boolean isWord) {
		this.isWord = isWord;
	}

}
