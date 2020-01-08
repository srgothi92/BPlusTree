import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


/**
 * B+ tree Impementation. The primary value of a B+ tree is in storing data for
 * efficient retrieval in a block-oriented storage context —in particular,
 * filesystems
 * 
 * Note that this implemenation is not snychroized.
 * 
 * @author Shaileshbhai Gothi
 *
 */
public class BPlusTreeImpl {

	private Integer order;
	private BPlusTreeNode root = null;

	/**
	 * Constructs an empty B+Tree or order provided.
	 * 
	 * @param order The order of B+ Tree. Normally an integer greater than 2.
	 */
	BPlusTreeImpl(Integer order) {
		this.order = order;
		this.root = new BPlusTreeNode();
	}

	/**
	 * Inserts an element with given key and value in the tree.
	 * 
	 * @param key   key of the element to be inserted
	 * @param value value of the element to be inserted
	 */
	public void insert(int key, Double value) {
		BPlusTreeNode externalNode = findExternalNode(this.root, key);
		addDataToExternalNode(externalNode, key, value);
		if (externalNode.isOverfullNode(this.order)) {
			BPlusTreeNode middleNode = splitExternalNode(externalNode);
			mergeNodes(externalNode.getParent(), middleNode, externalNode);
		}
	}

	/**
	 * Deletes an element with given key from the tree.
	 * 
	 * @param key key of the element to be deleted
	 */
	public void delete(int key) {
		BPlusTreeNode externalNode = findExternalNode(this.root, key);
		int keyIndex = externalNode.getKeyIndex(key) - 1;
		// key not found
		if (keyIndex < 0 || keyIndex >= externalNode.getDataListSize() || externalNode.getKeyAt(keyIndex) != key) {
			return;
		}
		externalNode.deleteData(keyIndex);
		// deficient external node as root is fine.
		if (externalNode != root && externalNode.getDataListSize() == 0) {
			balanceExternalNode(externalNode, key);
		}
	}

	/**
	 * In case of empty External Node we need to balance it using two cases: 
	 * 1. Borrow from adjacent sibling if they have key's greater than order/2.
	 * 2. Merge with sibling
	 * and delete the in between key 2. Merge parentKey and sibling
	 * 
	 * @param node The internal node that needs to be balanced
	 * @param key  key which was deleted that caused the imbalance
	 */
	private void balanceExternalNode(BPlusTreeNode node, int key) {
		BPlusTreeNode leftSibling = getLeftSibling(node, key);
		BPlusTreeNode rightSibling = getRightSibling(node, key);
		if (rightSibling != null && rightSibling.getDataListSize() > 1) {
			borrowFromRightSibling(node, rightSibling, key);
		} else if (leftSibling != null && leftSibling.getDataListSize() > 1) {
			borrowFromLeftSibling(node, leftSibling, key);
		} else {
			BPlusTreeNode sibling = null;
			if (rightSibling != null) {
				mergeWithSibling(node, key, true);
				sibling = rightSibling;
			} else {
				mergeWithSibling(node, key, false);
				sibling = leftSibling;
			}
			node.clear();
			// deficient internal node
			if (sibling.getParent().getDataListSize() == 0) {
				balanceInternalNode(sibling.getParent(), key);
			}
		}
	}

	/**
	 * In case of empty Internal node we need to balance it using two cases:
	 * 1. Borrow from adjacent sibling if they have key's greater than order/2 and change parent key
	 * from last leaf in case of left sibling or 2nd key in case of right sibling
	 * 2. Merge parent and sibling.
	 * 
	 * @param node The internal node that needs to be balanced.
	 * @param key The key which caused the imbalance.
	 */
	private void balanceInternalNode(BPlusTreeNode node, int key) {
		if (node == root) {
			// reached empty root case, delete the root and make child as root
			this.root = node.getChild(0);
			this.root.setParent(null);
		} else {
			BPlusTreeNode leftSibling = getLeftSibling(node, key);
			BPlusTreeNode rightSibling = getRightSibling(node, key);
			if (leftSibling != null && leftSibling.getDataListSize() > 1) {
				borrowFromInternalSibling(node, leftSibling, key, false);
			} else if (rightSibling != null && rightSibling.getDataListSize() > 1) {
				borrowFromInternalSibling(node, rightSibling, key, true);
			} else {
				BPlusTreeNode sibling = null;
				if (rightSibling != null) {
					mergeWithSiblingAndParentKey(node, rightSibling, key, true);
					sibling = rightSibling;
				} else {
					mergeWithSiblingAndParentKey(node, leftSibling, key, false);
					sibling = leftSibling;
				}
				node.clear();
				// deficient internal node
				if (sibling.getParent().getDataListSize() == 0) {
					balanceInternalNode(sibling.getParent(), key);
				}
			}
		}
	}

	/**
	 * Case1 of balancing external node. Borrow the first key from right sibling.
	 * 
	 * @param externalNode the node that is getting balanced
	 * @param sibling      The right sibling which has key greater than order/2
	 * @param deletedKey   the key that got deleted and caused imbalance.
	 */
	private void borrowFromRightSibling(BPlusTreeNode externalNode, BPlusTreeNode sibling, int deletedKey) {
		int keyIndex = externalNode.getParent().getKeyIndex(deletedKey);
		BPlusTreeNode.Data firstData = sibling.removeFirstData();
		externalNode.addData(0, firstData);
		int newKey = sibling.getFirstKey();
		externalNode.getParent().updateKey(keyIndex, newKey);
	}

	/**
	 * Case1 of balancing external node. Borrow the last key from left sibling.
	 * 
	 * @param externalNode the node that is getting balanced
	 * @param sibling      The left sibling which has key greater than order/2
	 * @param deletedKey   the key that got deleted and caused imbalance.
	 */
	private void borrowFromLeftSibling(BPlusTreeNode externalNode, BPlusTreeNode sibling, int deletedKey) {
		int keyIndex = externalNode.getParent().getKeyIndex(deletedKey);
		BPlusTreeNode.Data lastData = sibling.removeLastData();
		externalNode.addData(0, lastData);
		externalNode.getParent().updateKey(keyIndex - 1, lastData.key);

	}

	/**
	 * Case2 of balancing external node. Delete in between key from parent and
	 * remove the external node.
	 * 
	 * @param externalNode the node that is getting balanced
	 * @param deletedKey   the key that got deleted and caused imbalance.
	 * @param isRight      true in case of merging with right sibling.
	 */
	private void mergeWithSibling(BPlusTreeNode externalNode, int deletedKey, boolean isRight) {
		int keyIndex = externalNode.getParent().getKeyIndex(deletedKey);
		if (isRight) {
			externalNode.getParent().deleteData(keyIndex);
			externalNode.getParent().removeChild(keyIndex);
		} else {
			externalNode.getParent().deleteData(keyIndex - 1);
			externalNode.getParent().removeChild(keyIndex);
		}
		// Adjust the doubly linked list
		if (externalNode.getNextNode() != null) {
			externalNode.getNextNode().setPrevNode(externalNode.getPrevNode());
		}
		if (externalNode.getPrevNode() != null) {
			externalNode.getPrevNode().setNextNode(externalNode.getNextNode());
		}

	}

	/**
	 * Case1 of balancing internal node. Borrow a key from sibling internal node and
	 * change parent key from last leaf in case of left sibling or 2nd key in case
	 * of right sibling.
	 * 
	 * @param node    The internal node that is getting balanced.
	 * @param sibling The sibling which has key greater than order/2
	 * @param key     the key that got deleted and caused imbalance.
	 * @param isRight true in case of right sibling
	 */
	private void borrowFromInternalSibling(BPlusTreeNode node, BPlusTreeNode sibling, int key, boolean isRight) {
		int keyIndex = node.getParent().getKeyIndex(key);
		BPlusTreeNode borrowedChild;
		BPlusTreeNode.Data borrowedData;
		int parentKey;
		if (isRight) {
			parentKey = node.getParent().getKeyAt(keyIndex);
			borrowedChild = sibling.removeChild(0);
			node.addChild(1, borrowedChild);
			borrowedData = sibling.removeFirstData();
		} else {
			keyIndex = keyIndex - 1;
			parentKey = node.getParent().getKeyAt(keyIndex);
			borrowedChild = sibling.removeChild(sibling.getChildrens().size() - 1);
			node.addChild(0, borrowedChild);
			borrowedData = sibling.removeLastData();
		}
		borrowedChild.setParent(node);
		node.addInternalData(0, parentKey);
		node.getParent().updateKey(keyIndex, borrowedData.key);
	}

	/**
	 * Case2 of balancing internal node. Make the parent key as in between key of
	 * internal node.
	 * 
	 * @param node       The internal node that is getting balanced.
	 * @param sibling    The sibling which is getting merged with parent.
	 * @param deletedKey the key that got deleted and caused imbalance.
	 * @param isRight    true in case of right sibling
	 */
	private void mergeWithSiblingAndParentKey(BPlusTreeNode node, BPlusTreeNode sibling, int deletedKey,
			boolean isRight) {
		int keyIndex = node.getParent().getKeyIndex(deletedKey);
		int parentKey;
		if (isRight) {
			parentKey = node.getParent().getKeyAt(keyIndex);
			sibling.addChild(0, node.getChild(0));
			sibling.addInternalData(0, parentKey);
			node.getChild(0).setParent(sibling);
			node.getParent().deleteData(keyIndex);
			node.getParent().removeChild(keyIndex);
		} else {
			keyIndex = keyIndex - 1;
			parentKey = node.getParent().getKeyAt(keyIndex);
			sibling.addChild(sibling.getChildrens().size(), node.getChild(0));
			sibling.addInternalData(sibling.getDataListSize(), parentKey);
			node.getChild(0).setParent(sibling);
			node.getParent().deleteData(keyIndex);
			node.getParent().removeChild(keyIndex + 1);
		}
	}

	/**
	 * Get the right sibling for a node
	 * 
	 * @param node B+ node whose right sibling is needed.
	 * @param key  key that got deleted from node.
	 * @return The right sibling
	 */
	private BPlusTreeNode getRightSibling(BPlusTreeNode node, int key) {
		BPlusTreeNode parent = node.getParent();
		int keyIndex = parent.getKeyIndex(key);
		return parent.getChild(keyIndex + 1);
	}

	/**
	 * Get the left sibling for a node
	 * 
	 * @param node B+ node whose left sibling is needed.
	 * @param key  key that got deleted from node.
	 * @return The left sibling
	 */
	private BPlusTreeNode getLeftSibling(BPlusTreeNode node, int key) {
		BPlusTreeNode parent = node.getParent();
		int keyIndex = parent.getKeyIndex(key);
		return parent.getChild(keyIndex - 1);
	}

	/**
	 * Search an element with given key in the tree.
	 * 
	 * @param key key of the element to be searched.
	 * @return list containing the value whose key is searched.
	 */
	public ArrayList<Double> search(int key) {
		return search(key, key);
	}

	/**
	 * Search all element that lies between and including startKey and endKey
	 * 
	 * @param startKey starting Key of the element to be searched.
	 * @param endKey   ending key of the element to be searched.
	 * @return list of values between startKey and endKey
	 */
	public ArrayList<Double> search(int startKey, int endKey) {
		ArrayList<Double> listValues = new ArrayList<Double>();
		BPlusTreeNode startNode = findExternalNode(this.root, startKey);
		BPlusTreeNode current = startNode;
		boolean isEndKeyReached = false;
		while (current != null && !isEndKeyReached) {
			ArrayList<BPlusTreeNode.Data> dataList = current.getDataList();
			int i;
			for (i = 0; i < current.getDataListSize(); i++) {
				int currentKey = ((BPlusTreeNode.DataExternalNode) dataList.get(i)).key;
				double currentValue = ((BPlusTreeNode.DataExternalNode) dataList.get(i)).value;
				if (currentKey <= endKey && currentKey >= startKey) {
					listValues.add(currentValue);
				}
				if (currentKey >= endKey) {
					isEndKeyReached = true;
					break;
				}
			}
			current = current.getNextNode();
		}
		return listValues;
	}

	/**
	 * The external node that may contain given key.
	 * 
	 * @param root Root of B+ tree
	 * @param key  The key that needs to be found.
	 * @return The external node.
	 */
	private BPlusTreeNode findExternalNode(BPlusTreeNode root, int key) {
		BPlusTreeNode current = this.root;
		while (!current.getChildrens().isEmpty()) {
			current = current.getChildrens().get(current.getKeyIndex(key));
		}
		return current;
	}

	/**
	 * Add a new data to external node
	 * 
	 * @param node  The external node.
	 * @param key   The key of data.
	 * @param value The value of data.
	 */
	private void addDataToExternalNode(BPlusTreeNode node, int key, Double value) {
		int keyIndex = node.getKeyIndex(key);
		int matchedIndex = keyIndex - 1;
		if (matchedIndex >= 0 && matchedIndex < node.getDataListSize() && node.containsKeyAtIndex(matchedIndex, key)) {
			// key already exist, so update the value
			node.updateDataValue(matchedIndex, value);
		} else {
			// add new data
			node.addExternalData(keyIndex, key, value);
		}
	}

	/**
	 * Split external node making the middle key as parent and from middle key to
	 * end as child.
	 * 
	 * @param node The external node
	 * @return The middle node.
	 */
	private BPlusTreeNode splitExternalNode(BPlusTreeNode node) {
		int midIndex = this.order / 2;
		BPlusTreeNode middleNode = new BPlusTreeNode();
		BPlusTreeNode rightPartNode = new BPlusTreeNode();
		rightPartNode.setDataList(node.getDataList().subList(midIndex, node.getDataListSize()));
		rightPartNode.setParent(middleNode);
//		middle.addInternalData(((BPlusTreeNode.DataExternalNode) curr.getDataList().get(midIndex)).key);
		middleNode.addInternalData(0, node.getDataList().get(midIndex).key);
		middleNode.addChild(0, rightPartNode);
		// remove the right part from externalNode
		node.clearDataList(midIndex, node.getDataListSize());
		return middleNode;
	}

	/**
	 * Split internal node making the middle key as parent and middle+1 till end as
	 * child
	 * 
	 * @param node The internal node
	 * @return The middle node.
	 */
	private BPlusTreeNode splitInternalNode(BPlusTreeNode node) {
		int midIndex = this.order / 2;
		BPlusTreeNode middleNode = new BPlusTreeNode();
		BPlusTreeNode rightPartNode = new BPlusTreeNode();
		rightPartNode.setDataList(node.getDataList().subList(midIndex + 1, node.getDataListSize()));
		rightPartNode.setParent(middleNode);
		middleNode.addInternalData(0, node.getDataList().get(midIndex).key);
		middleNode.addChild(0, rightPartNode);
		// remove the right part from externalNode
		node.clearDataList(midIndex, node.getDataListSize());
		// Move all the children belonging to right part from split node
		ArrayList<BPlusTreeNode> splitNodeChildrens = node.getChildrens();
		ArrayList<BPlusTreeNode> righPartChildrens = new ArrayList<>();
		int indexLastChildOfLeft = splitNodeChildrens.size() - 1;
		for (int i = splitNodeChildrens.size() - 1; i >= 0; i--) {
			ArrayList<BPlusTreeNode.Data> iChildrenDataList = splitNodeChildrens.get(i).getDataList();
			if (middleNode.getFirstKey() <= iChildrenDataList.get(0).key) {
				splitNodeChildrens.get(i).setParent(rightPartNode);
				righPartChildrens.add(0, splitNodeChildrens.get(i));
				indexLastChildOfLeft--;
			} else {
				break;
			}
		}
		rightPartNode.setChildrens(righPartChildrens);
		node.clearChildrensList(indexLastChildOfLeft + 1, splitNodeChildrens.size());
		node.clearDataList(midIndex, node.getDataListSize());
		return middleNode;
	}

	/**
	 * Recursively Split and Merge internal Nodes, the generated middle node from
	 * split needs to be merged with parent internal node.
	 * 
	 * @param internalNode    The parent internal node
	 * @param newInternalNode The middle node
	 * @param prevSplitNode   The split node
	 */
	private void mergeNodes(BPlusTreeNode internalNode, BPlusTreeNode newInternalNode, BPlusTreeNode prevSplitNode) {
		mergeInternalNodes(internalNode, newInternalNode, prevSplitNode);
		if (internalNode != null && internalNode.isOverfullNode(order)) {
			BPlusTreeNode middleNode = splitInternalNode(internalNode);
			mergeNodes(internalNode.getParent(), middleNode, internalNode);
		}

	}

	/**
	 * Merge two internal Nodes, the generated middle node from split needs to be
	 * merged with parent internal node.
	 * 
	 * @param parentNode    The parent internal node
	 * @param newSplitMiddleNode The middle node
	 * @param prevSplitNode  The split node
	 */
	private void mergeInternalNodes(BPlusTreeNode parentNode, BPlusTreeNode newSplitMiddleNode,
			BPlusTreeNode prevSplitNode) {
		// root node was split
		if (parentNode == null) {
			boolean isRootExternaNode = this.root.getChildrens().size() == 0;
			this.root = newSplitMiddleNode;
			// find index in children list where external node needs to be placed
			int indexMiddleKey = newSplitMiddleNode.getKeyIndex(prevSplitNode.getDataList().get(0).key);
			prevSplitNode.setParent(newSplitMiddleNode);
			newSplitMiddleNode.addChild(indexMiddleKey, prevSplitNode);
			if (isRootExternaNode) {
				newSplitMiddleNode.getChildrens().get(0).setNextNode(newSplitMiddleNode.getChildrens().get(1));
				newSplitMiddleNode.getChildrens().get(1).setPrevNode(newSplitMiddleNode.getChildrens().get(0));
			}
		} else {
			int keyToBeInserted = newSplitMiddleNode.getDataList().get(0).key;
			BPlusTreeNode childToBeInserted = newSplitMiddleNode.getChildrens().get(0);
			int newKeyIndex = parentNode.getKeyIndex(keyToBeInserted);
			int childInsertPos = newKeyIndex;
			// if new child has key greater than equal to new key than it comes to right of
			// key in other words 1 position extra to key position.
			if (keyToBeInserted <= childToBeInserted.getFirstKey()) {
				childInsertPos = newKeyIndex + 1;
			}
			childToBeInserted.setParent(parentNode);
			parentNode.addChild(childInsertPos, childToBeInserted);
			parentNode.addInternalData(newKeyIndex, keyToBeInserted);

			if (parentNode.isParentOfExternalNode()) {

				if (childInsertPos == 0) {
					BPlusTreeNode nextNode = parentNode.getChildrens().get(childInsertPos + 1);
					childToBeInserted.setPrevNode(nextNode.getPrevNode());
					if (nextNode.getPrevNode() != null) {
						nextNode.getPrevNode().setNextNode(childToBeInserted);
					}
					childToBeInserted.setNextNode(nextNode);
					nextNode.setPrevNode(childToBeInserted);
				} else if (childInsertPos == parentNode.getChildrens().size() - 1) {
					BPlusTreeNode prevNode = parentNode.getChildrens().get(childInsertPos - 1);
					childToBeInserted.setNextNode(prevNode.getNextNode());
					if (prevNode.getNextNode() != null) {
						prevNode.getNextNode().setPrevNode(childToBeInserted);
					}
					childToBeInserted.setPrevNode(prevNode);
					prevNode.setNextNode(childToBeInserted);
				} else {
					BPlusTreeNode prevNode = parentNode.getChildrens().get(childInsertPos - 1);
					childToBeInserted.setPrevNode(prevNode);
					prevNode.setNextNode(childToBeInserted);
					BPlusTreeNode nextNode = parentNode.getChildrens().get(childInsertPos + 1);
					childToBeInserted.setNextNode(nextNode);
					nextNode.setPrevNode(childToBeInserted);
				}
			}
		}
	}

	/**
	 * Prints the complete tree in a human readable format for debugging.
	 */
	public void printBPlusTree() {
		LinkedList<BPlusTreeNode> queue = new LinkedList<BPlusTreeNode>();
		queue.add(this.root);
		queue.add(null);
		int level = 0;
		BPlusTreeNode current = null;
		while (!queue.isEmpty()) {
			current = queue.poll();
			if (current == null) {
				queue.add(null);
				if (queue.peek() == null) {
					break;
				}
				System.out.println("\n" + "End of level " + level++);
				continue;
			}

			System.out.print(current.toString() + " | ");
			// Once a external node is found break, than use linked list to print all the
			// nodes data this way we can ensure tree is created correctly.
			if (current.getChildrens().isEmpty()) {
				break;
			}
			for (int i = 0; i < current.getChildrens().size(); i++) {
				queue.add(current.getChildrens().get(i));
			}
		}

		current = current.getNextNode();
		// Use Doubly Linked list to print data.
		while (current != null) {
			System.out.print(current.toString() + " | ");
			current = current.getNextNode();
		}
		System.out.println("\n End of Level " + level);

	}

}