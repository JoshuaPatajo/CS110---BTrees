import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class BTreeManager {
	
	private RandomAccessFile btree;
	private long nodeCount;
	private long rootNodePos;
	
	private final int ORDER = 5;
	private final int NODE_LENGTH = 3 * ORDER - 1; // I did the math
	
	public BTreeManager() {}
	
	public BTreeManager( String name ) throws FileNotFoundException , IOException {
		
		File f = new File( name );
		
		if ( f.exists() ) {
			
			// File exists
			try {
				
				btree = new RandomAccessFile( f , "rwd" );
				btree.seek( 0 );
				nodeCount = btree.readLong();
				rootNodePos = btree.readLong();
				
			} catch ( FileNotFoundException fnfe ) {
				System.out.println( "FileNotFoundException in BTreeManager constructor (File exists)" );
			}
			
		}
		else {
			
			// File does not exist
			btree = new RandomAccessFile( name , "rwd" );
			nodeCount = 1;
			btree.seek( 0 );
			btree.writeLong( nodeCount );
			btree.writeLong( 16 );
			
		}
		
	}
	
	public void insert( long key , long offset ) throws IOException {
		
		// find the correct position to place the new key
		long currentNodePos = rootNodePos;
		
		btree.seek( currentNodePos + 16 ); // position of first child of root node
		
		long keyToCompare = btree.readLong();
		
		// place new key
		
		// adjust greater keys ( split if necessary )
		
	}
	
	public long select( long key ) {
		
		long offset = -1;
		
		return offset;
		
	}
	
	class Node {
		
		private final int ORDER = 5; // maximum number of children - based on the powerpoint
		/* private final int ARRAYLENGTH = 1 + ORDER + 2 * ( ORDER - 1 );
		// 1 for the parent, ORDER for the children, 2 * ( ORDER - 1 ) for the keys and offsets
		long[] array; // combination of the simpler three */
		Node parent;
		Node[] children;
		long[] keys;
		long[] offsets;
		
		public Node() {
			
			int temp = ORDER - 1;
			
			// array = new long[ ARRAYLENGTH ];
			children = new long[ ORDER ];
			keys = new long[ temp ];
			offsets = new long[ temp ];
			
			/* for ( int i = 0 ; i < ARRAYLENGTH ; i++ ) {
				array[i] = -1;
			} */
			
			for ( int i = 0 ; i < ORDER ; i++ ) {
				children[i] = -1;
			}
			
			for ( int i = 0 ; i < temp ; i++ ) {
				keys[i] = -1;
				offsets[i] = -1;
			}
			
		}
		
		public Node( Node parentIn ) {
			
			// https://stackoverflow.com/questions/285177/how-do-i-call-one-constructor-from-another-in-java
			this();
			
			parent = parentIn;
			
		}
		
		/* public void insertNew( long key , long offset ) {
			
			int temp = ORDER - 1;
			
			long test = key;
			
			for ( int i = 0 ; i < temp ; i++ ) {
				
				if ( keys[i] > test ) {
					test = keys[i];
					keys[i] = key;
				}
				
			}
			
			while ( keyPlace < temp ) {
				
				if ( keys[ keyPlace ] < key ) {
					keys[ keyPlace ] = keys[ keyPlace + 1 ];
				} else if ( keys[ keyPlace ] > key ) {
					keys[ keyPlace - 1 ] = key;
					offsets[ keyPlace - 1 ] = offset;
				}
				
			}
			
			while ( keyPlace > 0 ) {
				
				if ( keys[ keyPlace ] > key ) {
					keys[ keyPlace ] = keys[ keyPlace - 1 ];
				} else if ( keys[ keyPlace ] < key ) {
					keys[ keyPlace ] = key;
					break;
				}
				
				keyPlace--;
				
			}
			
			
			// array[ 3 * ( keyPlace + 1 ) - 1 ] = keys[ keyPlace ];
			
		} */
		
		public void insertNew( long key , long offset ) {
			
			// 
			
		}
		
	}
	
}

/* class Node {
	
	private final int ORDER = 5; // maximum number of children - based on the powerpoint
	private final int ARRAYLENGTH = 1 + ORDER + 2 * ( ORDER - 1 );
	// 1 for the parent, ORDER for the children, 2 * ( ORDER - 1 ) for the keys and offsets
	long[] array; // combination of the simpler three
	long[] children;
	long[] keys;
	long[] offsets;
	Node parent;
	
	public Node() {
		
		int temp = ORDER - 1;
		
		// array = new long[ ARRAYLENGTH ];
		children = new long[ ORDER ];
		keys = new long[ temp ];
		offsets = new long[ temp ];
		
		for ( int i = 0 ; i < ARRAYLENGTH ; i++ ) {
			array[i] = -1;
		}
		
		for ( int i = 0 ; i < ORDER ; i++ ) {
			children[i] = -1;
		}
		
		for ( int i = 0 ; i < temp ; i++ ) {
			keys[i] = -1;
			offsets[i] = -1;
		}
		
	}
	
	public Node( Node parentIn ) {
		
		// https://stackoverflow.com/questions/285177/how-do-i-call-one-constructor-from-another-in-java
		this();
		
		parent = parentIn;
		
	}
	
	public void insertNew( long key , long offset ) {
		
		int temp = ORDER - 1;
		
		long test = key;
		
		for ( int i = 0 ; i < temp ; i++ ) {
			
			if ( keys[i] > test ) {
				test = keys[i];
				keys[i] = key;
			}
			
		}
		
		while ( keyPlace < temp ) {
			
			if ( keys[ keyPlace ] < key ) {
				keys[ keyPlace ] = keys[ keyPlace + 1 ];
			} else if ( keys[ keyPlace ] > key ) {
				keys[ keyPlace - 1 ] = key;
				offsets[ keyPlace - 1 ] =
			}
			
		}
		
		while ( keyPlace > 0 ) {
			
			if ( keys[ keyPlace ] > key ) {
				keys[ keyPlace ] = keys[ keyPlace - 1 ];
			} else if ( keys[ keyPlace ] < key ) {
				keys[ keyPlace ] = key;
				break;
			}
			
			keyPlace--;
			
		}
		
		
		// array[ 3 * ( keyPlace + 1 ) - 1 ] = keys[ keyPlace ];
		
	}
	
} */