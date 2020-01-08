import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class to represent B+ tree node.
 * 
 * @author Shaileshbhai Gothi
 *
 */
public class BPlusTreeNode {
	/**
	 * Nested class to hold B+ tree node data.
	 * 
	 * @author Shaileshbhai Gothi
	 *
	 */
	class Data {

		protected int key;
	}

	/**
	 * Represents data of external or leaf Node
	 * 
	 * @author Shaileshbhai Gothi
	 *
	 */
	class DataExternalNode extends Data {

		public double value;

		public DataExternalNode(int key, Double value) {
			this.key = key;
			this.value = value;
		}

	}

	/**
	 * Represents data of internal Node
	 * 
	 * @author Shaileshbhai Gothi
	 *
	 */
	class DataInternalNode extends Data {

		public DataInternalNode(int key) {
			this.key = key;
		}

	}

	private ArrayList<BPlusTreeNode> childrens;
	private ArrayList<Data> dataList;
	private BPlusTreeNode parent;
	private BPlusTreeNode nextNode;
	private BPlusTreeNode prevNode;

	/**
	 * Constructs an empty B+ tree node
	 */
	public BPlusTreeNode() {
		this.childrens = new ArrayList<BPlusTreeNode>();
		this.dataList = new ArrayList<Data>();
	}

	/**
	 * Get B+ tree node data
	 * 
	 * @return the data list of B+ tree node
	 */
	public ArrayList<Data> getDataList() {
		return this.dataList;
	}

	/**
	 * Adds a child to B+ tree node at give position
	 * 
	 * @param index the position where to add child.
	 * @param node  child to be added.
	 */
	public void addChild(int index, BPlusTreeNode node) {
		this.childrens.add(index, node);
	}

	/**
	 * Set B+ tree data list
	 * 
	 * @param dataList the data list that needs to be set.
	 */
	public void setDataList(List<Data> dataList) {
		this.dataList = (ArrayList<Data>) new ArrayList<Data>(dataList);
	}

	/**
	 * 
	 * @return Parent node of B+ tree
	 */
	public BPlusTreeNode getParent() {
		return this.parent;
	}

	/**
	 * Get B+ tree node children
	 * 
	 * @return the children of B+ tree node
	 */
	public ArrayList<BPlusTreeNode> getChildrens() {
		return childrens;
	}

	/**
	 * Set B+ tree node children
	 * 
	 * @param childrens ArrayList of children of B+ tree node
	 */
	public void setChildrens(ArrayList<BPlusTreeNode> childrens) {
		this.childrens = (ArrayList<BPlusTreeNode>) new ArrayList<BPlusTreeNode>(childrens);
	}

	/**
	 * Get Next B+ tree node in linked list for external Node
	 * 
	 * @return B+ tree node
	 */
	public BPlusTreeNode getNextNode() {
		return this.nextNode;
	}

	/**
	 * Get Previous B+ tree node in linked list for external Node
	 * 
	 * @return B+ tree node
	 */
	public BPlusTreeNode getPrevNode() {
		return this.prevNode;
	}

	/**
	 * 
	 * @param node Parent to be set for B+ tree node
	 */
	public void setParent(BPlusTreeNode node) {
		this.parent = node;
	}

	/**
	 * Set Next B+ tree node in linked list for external Node
	 * 
	 * @param node The B+ tree external node
	 */
	public void setNextNode(BPlusTreeNode node) {
		this.nextNode = node;
	}

	/**
	 * Set Previous B+ tree node in linked list for external Node
	 * 
	 * @param node The B+ tree external node
	 */
	public void setPrevNode(BPlusTreeNode node) {
		this.prevNode = node;
	}

	/**
	 * Add new external node data to B+ tree node.
	 * 
	 * @param keyIndex position where to add external node data
	 * @param key      the key of data to be added
	 * @param value    the value of data to be added
	 */
	public void addExternalData(int keyIndex, int key, Double value) {
		this.dataList.add(keyIndex, new DataExternalNode(key, value));
	}

	/**
	 * Add new internal node data to B+ tree node.
	 * 
	 * @param keyIndex position where to add external node data
	 * @param key      the key of data to be added
	 */
	public void addInternalData(int keyIndex, int key) {
		this.dataList.add(keyIndex, new DataInternalNode(key));
	}

	/**
	 * Check if Node stores mode data than it is supposed to.
	 * 
	 * @param order Order of B+ tree
	 * @return boolean value
	 */
	public boolean isOverfullNode(Integer order) {
		if (this.dataList.size() == order) {
			return true;
		}
		return false;
	}

	/**
	 * Get the (index+1) child index of given key in dataList
	 * 
	 * @param key The key whose index needs to be found.
	 * @return the index
	 */
	public int getKeyIndex(int key) {
		int index = 0;
		for (Data oData : this.dataList) {
			if (oData.key > key) {
				return index;
			}
			index++;
		}
		return index;

	}

	/**
	 * Updates the data value at given index for a B+ tree node
	 * 
	 * @param keyIndex the position of data
	 * @param value    the value to be updated
	 */
	public void updateDataValue(int keyIndex, Double value) {
		((DataExternalNode) this.dataList.get(keyIndex)).value = value;
	}

	/**
	 * Check if Given key exist in the B+ node at given position
	 * 
	 * @param keyIndex the position of data in B+ node
	 * @param key      the key to be validated
	 * @return boolean value
	 */
	public boolean containsKeyAtIndex(int keyIndex, int key) {
		return this.dataList.get(keyIndex).key == key;
	}

	/**
	 * Get B+ tree node data list size
	 * 
	 * @return the size
	 */
	public int getDataListSize() {
		return this.dataList.size();
	}

	/**
	 * Clear B+ tree node data list
	 * 
	 * @param fromIndex starting index from where data list needs to be cleared
	 * @param toIndex   ending index till where data list needs to be cleared
	 */
	public void clearDataList(int fromIndex, int toIndex) {
		this.dataList.subList(fromIndex, toIndex).clear();

	}

	/**
	 * Get B+ tree node first key in data list
	 * 
	 * @return the first key
	 */
	public int getFirstKey() {
		return this.dataList.get(0).key;
	}

	/**
	 * Check if B+ tree is parent of External Node.
	 * 
	 * @return the boolean value
	 */
	public boolean isParentOfExternalNode() {
		return !this.getChildrens().isEmpty() && this.getChildrens().get(0).getChildrens().isEmpty();
	}

	/**
	 * Clear B+ tree node children list
	 * 
	 * @param fromIndex starting index from where children list needs to be cleared
	 * @param toIndex   ending index till where children list needs to be cleared
	 */
	public void clearChildrensList(int fromIndex, int toIndex) {
		this.childrens.subList(fromIndex, toIndex).clear();

	}

	/**
	 * Deletes data at provided index from B+ node
	 * 
	 * @param index the position of data
	 */
	public void deleteData(int index) {
		this.dataList.remove(index);
	}

	/**
	 * Get B+ tree node child at provided index
	 * 
	 * @param index the index of child
	 * @return the B+ tree node
	 */
	public BPlusTreeNode getChild(int index) {
		if (index < childrens.size() && index >= 0) {
			return this.childrens.get(index);
		}
		return null;
	}

	/**
	 * Update the key of B+ node data
	 * 
	 * @param index the index at which data needs to be updated
	 * @param key   the newKey value for update
	 */
	public void updateKey(int index, int key) {
		this.dataList.get(index).key = key;
	}

	/**
	 * Deletes the first data from B+ node
	 * 
	 * @return the deleted data
	 */
	public Data removeFirstData() {
		return this.dataList.remove(0);
	}

	/**
	 * Deletes the last data from B+ node
	 * 
	 * @return the deleted data
	 */
	public Data removeLastData() {
		return this.dataList.remove(getDataListSize() - 1);
	}

	/**
	 * Add data at given index in B+ node
	 * 
	 * @param index position at which the data needs to be added
	 * @param data  the data object that will be added
	 */
	public void addData(int index, Data data) {
		this.dataList.add(index, data);
	}

	/**
	 * Deletes the child at provided position from B+ node
	 * 
	 * @param index the position of child
	 * @return the deleted child
	 */
	public BPlusTreeNode removeChild(int index) {
		return this.childrens.remove(index);
	}

	/**
	 * Get B+ tree node data
	 * 
	 * @param index the index of key in node
	 * @return the data list of B+ tree node
	 */
	public int getKeyAt(int index) {
		return this.dataList.get(index).key;
	}

	/**
	 * Override the toString Method to display the data present in node
	 * 
	 * @return the stringified form of data.
	 */
	public String toString() {
		String out = "";
		for (int i = 0; i < getDataListSize(); i++) {
			out += (getDataList().get(i).key + ":(");
			String value = "" + ((getDataList().get(i) instanceof BPlusTreeNode.DataExternalNode)
					? ((BPlusTreeNode.DataExternalNode) getDataList().get(i)).value
					: "?");
			out += (value.isEmpty() ? ");" : value.substring(0, value.length() - 1) + ");");
		}
		return out;
	}

	/**
	 * Clear the node
	 */
	public void clear() {
		this.dataList = null;
		this.childrens = null;
		this.parent = null;
		this.nextNode = null;
		this.prevNode = null;
	}

}
