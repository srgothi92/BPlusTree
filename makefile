JCC =javac
JFLAGS = -g

default: bplustree.class

bplustree.class: bplustree.java
	$(JCC) $(JFLAGS) bplustree.java
	
BPlusTreeImpl.class: BPlusTreeImpl.java
	$(JCC) $(JFLAGS) BPlusTreeImpl.java
	
BPlusTreeNode.class: BPlusTreeNode.java
	$(JCC) $(JFLAGS) BPlusTreeNode.java
	
clean:
	$(RM) *.class
