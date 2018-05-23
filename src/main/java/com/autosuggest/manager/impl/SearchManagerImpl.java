package com.autosuggest.manager.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.autosuggest.manager.SearchManager;
import com.autosuggest.vo.SearchParams;
import com.autosuggest.vo.TernarySearchTree;

public class SearchManagerImpl implements SearchManager {

	public static Logger logger = Logger.getLogger(SearchManagerImpl.class);

	// Hold reference to the Root of the Search Tree
	TernarySearchTree root;

	/**
	 * Perform search of the requested string It reads all the words present in
	 * a file "words.txt"
	 * 
	 * The method iterates through the words and finds if we have any word that
	 * starts with the prefix requested, until it finds the maximum requested
	 * keywords.
	 * 
	 * @param searchParams
	 * @return List of search results
	 */
	@Override
	public List<String> searchWords(SearchParams searchParams) {

		//File that holds all the words to be searched from
		String fileName = "/words.txt";

		InputStream inputStream = null;
		BufferedReader reader = null;

		// List that holds the search results
		List<String> words = new ArrayList();

		try {
			// load the file
			inputStream = this.getClass().getResourceAsStream(fileName);
			// pass the reference to the inputstream reader for further
			// processing
			reader = new BufferedReader(new InputStreamReader(inputStream));

			//Hold each word read from file
			String sCurrentLine = "";

			int count = 0;
			int maxCount = searchParams != null ? searchParams.getMaxNumberOfResults() : Integer.MAX_VALUE;
			String prefixString = searchParams != null ? searchParams.getStartText() : "";

			//TODO : We can fetch all the values from the file and cache it and use this value in cache for searching
			// this would help us avoiding IO operation overhead on every request
			
			logger.debug("Start reading words from the file");
			// Fetch words from the file and create Ternary Search Tree, by
			// inserting each word in it
			while ((sCurrentLine = reader.readLine()) != null) {
				root = insertWord(root, sCurrentLine);
			}
			logger.debug("Reading from File Complete");
			
			StringBuffer buffer = new StringBuffer();
			char[] characterArray = new char[100];

			// start time of the search, used to check the time required for search
			long start = System.currentTimeMillis();

			logger.debug("Search for Prefix starts.....");
			
			TernarySearchTree head = null;
			
			//If requested search string is empty, add all the words
			//Initialize head to root
			if (prefixString.length() == 0) {
				head = root;
			}else{
			
				// Search the TST, for the prefix string(string to be searched)
				// Head node holds the node of last character user searched if the
				// pattern exists in the TST
				head = searchWord(root, prefixString, buffer);
				logger.debug("Prefix Search completed");
			}

			// Buffer is used to hold the characters in the prefix that are
			// already traversed in the TST.
			// These characters will be part of all subsequent words to be found.
			// Add these characters to the characterArray, to be used in finding
			// the rest of the words in TST
			int i = 0;
			for (char c : buffer.toString().toCharArray()) {
				characterArray[i++] = c;
			}

			logger.debug("Search for Words starting with prefix");
			// Find all the words that has the prefix after the prefix has been
			// found in the TST
			findWordsMatchingPrefix(head, characterArray, i, words,maxCount);

			logger.info("Total time required to search the string is : " + (System.currentTimeMillis() - start));

		} catch (IOException e) {
			logger.error("Exception occured while reading the file", e);
		} finally {

			// Close all the resources used for IO operations
			try {
				if (inputStream != null)
					inputStream.close();
				if (reader != null)
					reader.close();
			} catch (IOException ex) {
				logger.error("Exception occured while closing the resources", ex);
			}
		}
		logger.debug("Search completed. Number of words found is : " + words.size());
		return words;
	}

	/**
	 * Insert a word in Ternary Search Tree
	 * 
	 * @param root
	 *            : Root node of TST
	 * @param word
	 *            : Word to be searched
	 * @return root node
	 */
	public static TernarySearchTree insertWord(TernarySearchTree root, String word) {
		return insertCharacter(root, word.toCharArray(), 0);
	}

	/**
	 * Search a word in Ternary Search Tree
	 * 
	 * @param root
	 *            : Root node of TST
	 * @param word
	 *            : Word to be searched
	 * @param buffer
	 *            : String buffer that will hold the characters that have been
	 *            found in a sequence. As the search in incremental, instead of
	 *            backtracking we use this to form the words that have been
	 *            found in the TST, after the initail search.
	 * @return root node
	 */
	public static TernarySearchTree searchWord(TernarySearchTree root, String word, StringBuffer buffer) {
		return searchWord(root, word.toCharArray(), 0, buffer);
	}

	/**
	 * Search the TST for words that starts with the prefix(string for which
	 * auto suggestion was requested) The node lookup starts from the last
	 * character in the prefix i.e. partial string has been matched, find words
	 * that has this prefix part of them
	 * 
	 * @param head
	 *            : Node that lastly matched the prefix character
	 * @param charArray
	 *            : Array to hold the words that are formed/starts with the
	 *            prefix
	 * @param ptr
	 *            : Index location of charArray to be filled
	 * @param autoSuggests
	 *            : List holding all the words that starts with the prefix
	 */
	private static void findWordsMatchingPrefix(TernarySearchTree head, char[] charArray, int ptr, List<String> autoSuggests, int maxCount) {
		if (head != null) {

			if(maxCount <= autoSuggests.size()){
				return;
			}
			
			// Process the left subtree to find words after the prefix
			findWordsMatchingPrefix(head.getLeft(), charArray, ptr, autoSuggests,maxCount);

			// Add the character to the charArray (word formed)
			charArray[ptr] = head.getCharacter();

			// If the node is last character, add the character array to list of
			// words found
			if (head.isWord()) {
				String wordFounded = String.valueOf(charArray);
				logger.debug("Word found in findWordsMatchingPrefix : " + wordFounded);
				autoSuggests.add(wordFounded);
				if(maxCount <= autoSuggests.size()){
					return;
				}
			}

			// Process the middle subtree to find words after the prefix
			findWordsMatchingPrefix(head.getMid(), charArray, ptr + 1, autoSuggests,maxCount);

			// Process the right subtree to find words after the prefix
			findWordsMatchingPrefix(head.getRight(), charArray, ptr, autoSuggests,maxCount);
		}
	}

	/*
	 * private static void findEntireWords(TernarySearchTree head, StringBuffer
	 * word, List<String> autoSuggests) { if (head != null) {
	 * 
	 * findEntireWords(head.getLeft(), word, autoSuggests);
	 * 
	 * word.append(head.getCharacter());
	 * 
	 * if(head.isWord()){ autoSuggests.add(word.toString()); //A new word if the
	 * mid element is null, empty the buffer in that case //else, new word will
	 * have existing characters in the buffer //if(head.getMid()==null)
	 * //word.delete(0, word.length()); }
	 * 
	 * findEntireWords(head.getMid(), word, autoSuggests); //word.delete(0,
	 * word.length());
	 * 
	 * findEntireWords(head.getRight(), word, autoSuggests); } }
	 */

	/**
	 * Search a word in Ternary search Tree starting from head(root node of the
	 * Tree)
	 * 
	 * If character < head.character search character towards the left head If
	 * character > head.character search character towards the right of head If
	 * character == head.character insert character in the middle node If the
	 * character is last element in the word, mark the node as complete.
	 * 
	 * @param head
	 *            : root node of the TST
	 * @param charArray
	 *            : array representation of word to be searched
	 * @param ptr
	 *            : index position of the character to be processed/searched
	 *            next
	 * @param buffer
	 *            : characters matched, last but one character from the word to
	 *            be searched
	 * @return : Node of the character in charArray lastly found in the TST
	 */
	private static TernarySearchTree searchWord(TernarySearchTree head, char[] charArray, int ptr, StringBuffer buffer) {
		if (head == null) {
			return null;
		}

		logger.debug("Inside searchWord, processing character : " + charArray[ptr]);

		// If inserting character < head.character, search character towards the
		// left of head
		if (Character.toLowerCase(charArray[ptr]) < Character.toLowerCase(head.getCharacter())) {
			return searchWord(head.getLeft(), charArray, ptr, buffer);
		}
		// If inserting character > head.character, insert character towards the
		// right of head
		else if (Character.toLowerCase(charArray[ptr]) > Character.toLowerCase(head.getCharacter())) {
			return searchWord(head.getRight(), charArray, ptr, buffer);
		}
		// If inserting character == head.character
		else {
			// Check if the word is complete
			if (head.isWord()) {
				return head;
			}
			// If the character searched is last character in the charArray,
			// return it's node
			else if (ptr == charArray.length - 1) {
				return head;
			}
			// Append the character to the Stringbuffer and process/search in
			// the middle node
			else {
				buffer.append(head.getCharacter());
				return searchWord(head.getMid(), charArray, ptr + 1, buffer);
			}
		}
	}

	/**
	 * Insert a word in Ternary search Tree starting from head(root node of the
	 * Tree) Insertion is done one character at a time.
	 * 
	 * If inserting character < head.character insert character towards the left
	 * head If inserting character > head.character insert character towards the
	 * right of head If inserting character == head.character insert character
	 * in the middle node If the character is last element in the word, mark the
	 * node as complete.
	 * 
	 * @param head
	 *            : root node of the TST
	 * @param charArray
	 *            : array representation of word to be inserted
	 * @param ptr
	 *            : index position of the character to be processed/inserted
	 *            next
	 * @return head node
	 */
	private static TernarySearchTree insertCharacter(TernarySearchTree head, char[] charArray, int ptr) {

		if (head == null) {
			logger.debug("Head is null, initialized");
			head = new TernarySearchTree(charArray[ptr]);
		}
		logger.debug("Inside insertCharacter, processing character : " + charArray[ptr]);

		// If inserting character < head.character, insert character towards the
		// left of head
		if (Character.toLowerCase(charArray[ptr]) < Character.toLowerCase(head.getCharacter())) {
			head.setLeft(insertCharacter(head.getLeft(), charArray, ptr));
		}
		// If inserting character > head.character, insert character towards the
		// right of head
		else if (Character.toLowerCase(charArray[ptr]) > Character.toLowerCase(head.getCharacter())) {
			head.setRight(insertCharacter(head.getRight(), charArray, ptr));
		}
		// If inserting character == head.character, insert character in the
		// middle node of head
		else {

			// If it is not last character then insert it into the middle node
			if (ptr + 1 < charArray.length) {
				head.setMid(insertCharacter(head.getMid(), charArray, ptr + 1));
			}
			// If the character is last element of word, mark it as complete
			// word
			else {
				head.setWord(true);
			}
		}
		return head;
	}

}
