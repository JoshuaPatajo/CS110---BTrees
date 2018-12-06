import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.EOFException;
import java.util.Scanner;

public class BTreeManager {
	
	private RandomAccessFile btree;
	private long nodeCount;
	private long rootNodePos;
	
	private final int ORDER = 5;
	private final int NODE_LENGTH = 3 * ORDER - 1;
	private Node[] nodeList;
	
	public BTreeManager() {}
	
	public BTreeManager( String name ) throws FileNotFoundException , IOException {
		
		File f = new File( name );
		
		if ( f.exists() ) {
			
			// File exists
			
			btree = new RandomAccessFile( f , "rwd" );
			btree.seek( 0 );
			nodeCount = btree.readLong();
			rootNodePos = btree.readLong();
			loadNodes();
		
		}
		else {
			
			// File does not exist
			btree = new RandomAccessFile( name , "rwd" );
			nodeCount = 1;
			rootNodePos = 16;
			btree.seek( 0 );
			btree.writeLong( nodeCount );
			btree.writeLong( rootNodePos );
			
			for ( int i = 0 ; i < NODE_LENGTH ; i++ ) {
				btree.writeLong( -1 );
			}
			
		}
		
	}

	public Node getRoot() throws IOException {
		
		btree.seek( 8 );
		Node rootNode = new Node();
		
		try {
			rootNodePos = btree.readLong();
		} catch ( EOFException e ) {
			
		}
		
		rootNode = new Node( rootNodePos , false );
		
		return rootNode;
		
	}
	
	public void insert( long key , long offset ) throws IOException {
		
		getRoot().insert( key , offset );
		
	}
	
	public long select ( long key ) throws IOException {
		
		long output = -1;
		
		output = getRoot().select( key );
		
		return output;
		
	}

	public void loadNodes() throws IOException
	{
		nodeList = new Node[(int)nodeCount];
		for(int i = 0; i < nodeCount; i++)
		{
			System.out.println(i + ": ");
			long pos = 16 + (i * 8 * NODE_LENGTH);
			btree.seek(pos);
			long[] data = new long[NODE_LENGTH];
			for(int j = 0; j < NODE_LENGTH; j++)
			{
				try
				{
					data[j] = btree.readLong();
					System.out.print(data[j] + " ");
				}
				catch (EOFException e) {}
			}
			nodeList[i] = new Node(pos, false);
			nodeList[i].load(data);
		}
	}
	
	class Node
	{
		long parent;
		long[] child = new long[ORDER + 1];
		long[] key = new long[ORDER];
		long[] offset = new long[ORDER];
		long position;
		
		public Node() {}

		public Node( long p , boolean isNew ) throws IOException {
			
			position = p;
			
			btree.seek( position );
			
			try {
				parent = btree.readLong();
			} catch ( EOFException e ) {}
			
			
			for ( int i = 0 ; i < ORDER - 1 ; i++ ) {
				
				try {
					child[ i ] = btree.readLong();
				} catch ( EOFException e ) {}
				
				try {
					key[ i ] = btree.readLong();
				} catch ( EOFException e ) {}
				
				try {
					offset[ i ] = btree.readLong();
					// System.out.println( "Offset " + i + ": " + offset[ i ] );
				} catch ( EOFException e ) {}
				
			}
			
			key[ ORDER - 1 ] = -1;
			offset[ ORDER - 1 ] = -1;
			
			try {
				child[ ORDER - 1 ] = btree.readLong();
			} catch ( EOFException e ) {}
			
			if ( isNew ) {
				reset();
			}
			
		}
		
		public void insert( long newKey , long newOffset ) throws IOException {
			
			// If there are children nodes where the new element can be inserted, try to insert them there.
			if ( countChildren() > 0 ) {
				
				// Check each key in this node. The child node that should be checked is the largest one that appears before the smallest key that is greater than the newKey.
				for ( int i = 0 ; i < ORDER ; i++ ) {
					
					if ( key[ i ] > newKey ) {
						
						Node childNode = new Node( child[ i ] , false );
						childNode.insert( newKey , newOffset );
						break;
						
					}
					else if ( key [ i ] < newKey ) {
						
						Node childNode = new Node( child[ i + 1 ] , false );
						childNode.insert( newKey , newOffset );
						break;
						
					}
					
				}
				
			}
			// If the program reaches this point, the node being checked does not have children nodes to pass the insertion.
			else { // Without children nodes to pass the new element to, it should accept insertions directly.
				
				// Insert the new key.
				
					// Go through each slot in the array.
					for ( int i = 0 ; i <= ORDER - 1 ; i++ ) {
						
						// At the first available slot, place the new element. It will be sorted later.
						if ( offset[ i ] == -1 ) {
							
							key[ i ] = newKey;
							offset[ i ] = newOffset;
							break;
							
						}
						
					}
				
				// After inserting the newest key, sort all the keys ( and associated offsets ) in this node.
				
					// Count the number of elements that need to be sorted.
					int numElem = countElements();
					
					// I used Bubble Sort. Mostly because I like the word "bubble"
					for( int i = numElem - 1 ; i >= 1 ; i-- ) {
						
						for ( int j = 1 ; j < i ; j++ ) {
							
							if ( key[ j ] > key[ j + 1 ] ) {
								
								long temp = key[ j ];
								key[ j ] = key[ j + 1 ];
								key[ j + 1 ] = temp;
								
								temp = offset[ j ];
								offset[ j ] = offset[ j + 1 ];
								offset[ j + 1 ] = temp;
								
							}
							
						}
						
					}
				
				// Check if this node is full i.e. there are ORDER offsets in this node. If it is full , split this node.
				if ( countElements() >= ORDER ) {
					split();
				}
				
				// Update the .bt file to reflect changes in the nodes.
				writeNodes();
				
			}
			
		}
		
		public void split() throws IOException {
			
			// SET-UP
			
				// Keep track of the original values in this node. Around half will be removed.
				long[] keys = key;
				long[] offsets = offset;
				
				// Identify the middle element in this node. It will be passed to a parent node. If none exist, one will be made.
				int median = ORDER / 2;
				
				// Read the number of nodes to determine where the new node/s will be made.
				btree.seek( 0 );
				long numNodes = btree.readLong();
				long newStart =  16 + ( numNodes * ( 8 * NODE_LENGTH ) );
			
			// NODE MODE
			
				// At least 1 node is made each time split is called. If this is not actually the case, it is 3am and I cannot think about the logic of this particular error at the moment. It makes sense though.
				
				// Make that first node, the sister node.
				Node newSisterNode = new Node( newStart , true );
				
				// Insert the elements greater than the median into the sister node. Remove those elements from this node. If any children are attached to the keys in the sister node, transfer those too.
				for ( int i = median + 1 ; i < ORDER ; i++ ) {
					
					newSisterNode.insert( keys[ i ] , offsets[ i ] );
					
					key[ i ] = -1;
					offset[ i ] = -1;
					
					if ( child[ i ] != -1 ) {
						
						newSisterNode.addChild( child[ i ] );
						child[ i ] = -1;
						
					}
					
				}
				
				// if there is no parent node to take the median, create a new root node and set this one as its child
				if ( parent == -1 ) {
					
					// Also set the sister node's parent to the new root node.
					newSisterNode.setParent( newStart + ( NODE_LENGTH * 8 ) );
					
					// The new root node is made in the position immediately following the new sister node.
					Node newRootNode = new Node( newStart + ( NODE_LENGTH * 8 ) , true );
					
					// Insert the median in the parent node. Remove it from this node.
					newRootNode.insert( keys[ median ] , offsets[ median ] );
					key[ median ] = -1;
					offset[ median ] = -1;
					
					// Update the parent field of this node to refer to the new root node.
					parent = newStart + NODE_LENGTH * 8;
					btree.seek( position );
					btree.writeLong( parent );
					btree.seek( 8 );
					btree.writeLong( parent );
					
					// As a new node, the new root node will not have children, so this node and its sister can be passed to it directly.
					newRootNode.addChild( position );
					newRootNode.addChild( newStart );
					
					// Because you made both a sister and parent node, the number of nodes has increased by 2. Indicate this in the file.
					numNodes += 2;
					btree.seek( 0 );
					btree.writeLong( numNodes );
					
					newRootNode.writeNodes();
					
					newSisterNode.writeNodes();
					
				}
				// If there is a parent node, the median should be inserted there.
				else {
					
					Node parentNode = new Node( parent , false );
					
					// Insert the median into the parent node. Remove it from this node.
					parentNode.insert( keys[ median ] , offsets[ median ] );
					key[ median ] = -1;
					offset[ median ] = -1;
					
					// Find out where in the parent node the median went. This node should go to the child slot that immediately precedes the median and the sister node should immediately come after the median.
					int medianPos = findElement( keys[ median ] );
					
					// Indicate that the parent of the sister node is the same parent of this node.
					newSisterNode.setParent( parent );
					
					// Unlike in the other case, it is possible that an existing parent node already has children. The nodes in this split go in these locations specifically.
					parentNode.child[ medianPos ] = position;
					parentNode.child[ medianPos + 1 ] = newStart;
					
					parentNode.writeNodes();
					newSisterNode.writeNodes();
					
				}
			
			// WINDING DOWN
			
			writeNodes();
			
		}

		public void reset() throws IOException {
			
			for(int i = 0; i < ORDER - 1; i++) {
				
				key[i] = -1;
				offset[i] = -1;
				child[i] = -1;
				
			}

			child[ ORDER - 1 ] = -1;
			child[ ORDER ] = -1;
			key[ ORDER - 1 ] = -1;
			offset[ ORDER - 1 ] = -1;
			
			writeNodes();
			
		}

		public void writeReset() throws IOException {
			
			btree.seek( position + 8 );

			for ( int i = 0; i < NODE_LENGTH ; i++ ) {
				btree.writeLong(0);
			}
			
		}

		public void setParent( long p ) throws IOException {
			
			parent = p;
			btree.seek( position );
			btree.writeLong( p );
			
		}
		
		/**
		 * 
		 * Recursive method. Checks this node for a particular key. If it is possible that the key exists in one of its children, it will have that child node call the method.
		 * 
		 * @param  k  the key to find within the BTree
		 * @return  the offset of the element with the key being searched for
		 * 
		 */
		public long select( long keyToFind ) throws IOException {
			
			// Go through each key in the node.
			for ( int i = 0 ; i < ORDER - 1 ; i++ ) {
				
				// If key is in the node, return the offset associated with that key.
				if ( key[ i ] == keyToFind ) {
					return offset[ i ];
				}
				// If key is less than a key in the node, call the method from the relevant child node. If this node has no children, the key cannot exist in this tree.
				else if ( key[ i ] > keyToFind ) {
					
					if( child[i] != -1 ) {
						
						Node childNode = new Node( child[ i ] , false );
						return childNode.select( keyToFind );
						
					} else {
						return -1;
					}
					
				}
				// At this point, the key either doesn't exist in the tree, or it is in the last child node. All valid keys will have been checked when i is countElements() + 1.
				else if( i == countElements() ) {
					
					// Check that this node's last child exists. If it doesn't, then the key cannot be in this tree. If it does exist, check it for the key.
					
					if ( child[ countElements() ] != -1 ) {
						
						Node childNode = new Node( child[ countElements() ] , false );
						return childNode.select( keyToFind );
						
					} else {
						return -1;
					}
					
				}
				
			}
			
			return -1;
			
		}
	
		/**
		 * 
		 * Counts the number of elements in this node. Elements refers to a combination of key and offset. The maximum number a node is allowed to have is one less than the ORDER of the BTree. If the count exceeds this limit, the node is split into multiple nodes.
		 * 
		 * @return  the number of elements in this node
		 * 
		 */
		public int countElements() {
			
			int output = 0;
			
			for ( int i = 0 ; i <= ORDER - 1 ; i++ ) {
				
				if ( offset[ i ] >= 0 ) {
					output++;
				}
				
			}
			
			return output;
			
		}
		
		/**
		 * 
		 * Counts the number of children in this node. The maximum number a node is allowed to have is defined by the ORDER. It is not possible for a node to have more than this limit of children, unless it also exceeds the limit for the number of elements. How this problem is dealt with is explained in the countElements() method.
		 * 
		 * @return  the number of children nodes this node has
		 * 
		 */
		public int countChildren() {
			
			int output = 0;
			
			for ( int i = 0 ; i < ORDER - 1 ; i++ ) {
				
				if ( child[ i ] >= 0 ) {
					output++;
				}
				
			}
			
			return output;
			
		}
		
		/**
		 * 
		 * Designates an existing node as the child of another this node.
		 * 
		 * @param  newChild  the node to be designated as the child of this node
		 * 
		 */
		public void addChild( long childOffset ) throws IOException {
			
			for ( int i = 0 ; i <= ORDER ; i++ ) {
				
				if ( child[ i ] == -1 ) {
					// add child offset to array of children offsets
					child[ i ] = childOffset;
					break;
					
				}
				
			}
			
		}
		
		public int findElement( long element ) {
			
			for ( int i = 0 ; i < ORDER ; i++ ) {
				
				if ( key[ i ] == element ) {
					return i;
				}
				
			}
			
			return -1;
			
		}
		
		public void writeNodes() throws IOException {
			
			btree.seek( position );
			
			btree.writeLong( parent );
			
			for( int i = 0 ; i < ORDER - 1 ; i++ ) {
				
				btree.writeLong( child[ i ] );
				btree.writeLong( key[ i ] );
				btree.writeLong( offset[ i ] );
				
			}
			
			btree.writeLong( child[ ORDER - 1 ] );
			
			/* if ( parent != -1 ) {
				
				Node parentNode = new Node( parent );
				parentNode.writeNodes();
				
			} */
			
		}

		public void load(long[] data) throws IOException
		{
			parent = data[0];
			for(int i = 0; i < ORDER - 1; i++)
			{
				child[i] = data[1 + (i * 3)];
				key[i] = data[2 + (i * 3)];
				offset[i] = data[3 + (i * 3)];
			}
			child[ORDER - 1] = data[NODE_LENGTH - 1];
		}
		
	}
	
}