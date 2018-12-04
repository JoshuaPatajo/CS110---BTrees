import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

public class BTreeManager {
	
	private RandomAccessFile btree;
	private long nodeCount;
	private ArrayList< Node > actualbtree = new ArrayList< Node >();
	
	private final static long ORDER = 5;
	
	public BTreeManager() {}
	
	public BTreeManager( String name ) throws IOException {
		
		File f = new File( name );
		
		if ( f.exists() && !f.isDirectory() ) {
			
			// File exists
			try {
				
				btree = new RandomAccessFile( f , "rwd" );
				btree.seek( 0 );
				nodeCount = btree.readLong();
				
			} catch ( FileNotFoundException fnfe ) {
				System.out.println( "FileNotFoundException in BTreeManager constructor (File exists)" );
			}
			
		}
		else {
			
			// File does not exist
			try {
				
				btree = new RandomAccessFile( name , "rwd" );
				nodeCount = 1;
				btree.seek( 0 );
				btree.writeLong( nodeCount );
				btree.writeLong( -1 );
				btree.writeLong( -1 );
				
			} catch ( FileNotFoundException fnfe ) {
				System.out.println( "FileNotFoundException in BTreeManager constructor (File does not exist)" );
			}
			
		}
		
	}
	
	public void insert( long key , long offset ) throws IOException {
		
		/* long testK = key;
		
		for ( int i = 0 ; i < ORDER ; i++ ) {
			
			btree.seek( ( 2 + 3 * i ) * 8 );
			
			long temp = btree.readLong();
			
			if ( temp > testK ) {
				
				testK = temp;
				btree.writeLong( key );
				
				
				btree.seek( ( 2 + 3 * i ) * 8 + 1 );
				
				long offsetN = btree.readLong();
				
				btree.writeLong( offset );
				
			}
			
		} */
		
		// find the correct node to place the new key
		
		// find the correct position in the node to place the new key
		
		// place new key
		
		// adjust greater keys ( split if necessary )
		
	}
	
	public long select( long key ) {
		
		long offset = -1;
		
		return offset;
		
	}
	
	class Node
	{
		private final int ORDER = 5;

		long parent;
		long[] child = new long[ORDER];
		long[] key = new long[ORDER - 1];
		long[] offset = new long[ORDER - 1];
		Node[] childNode = new Node[ORDER];
		long pos;

		public Node(long p)
		{
			pos = p;
			reset();
		}

		public void insert(long k, long os) throws IOException
		{
			for(int i = 0; i < ORDER - 1; i++)
			{
				if(offset[i] == -1)
				{
					key[i] = k;
					offset[i] = os;
					btree.seek(pos + 16 + (24 * i));
					btree.writeLong(k);
					break;
				}
				else if(key[i] > k)
				{
					if(child[i] != -1)
					{
						childNode[i].insert(child[i], k);
						break;
					}
					else
					{
						if(offset[ORDER - 2] != -1)
						{
							split(k, os, pos);
							break;
						}
						else
						{
							for(int j = ORDER - 3; j > i; j--)
							{
								key[j] = key[j+1];
								offset[j] = offset[j+1];
								child[j] = child[j+1];
								childNode[j] = childNode[j+1];
							}
							key[i] = k;
							offset[i] = os;
							child[i] = -1;
							childNode[i] = null;
							btree.seek(pos + 16 + (24 * i));
							btree.writeLong(k);
						}
					}
				}
				else if(key[i] < k)
				{
					if(i == ORDER - 2)
					{
						if(child[ORDER - 1] == -1)
						{
							split(k, os, pos);
							break;
						}
						else
						{
							childNode[ORDER - 1].insert(child[ORDER - 1], k);
							break;
						}
					}
				}
			}
		}

		public void split(long k, long os, long pos) throws IOException
		{
			long[] tempKey = new long[ORDER];
			long[] tempOS = new long[ORDER];
			boolean notFound = true;
			for(int i = 0; i < ORDER; i++)
			{
				if(i == ORDER - 1 && notFound)
				{
					tempKey[i] = k;
					tempOS[i] = os;
				}
				else if(i != ORDER - 1)
				{
					if(key[i] > k && notFound)
					{
						tempKey[i] = k;
						tempOS[i] = os;
						notFound = false;
					}
					else
					{
						if(notFound)
						{
							tempKey[i] = key[i];
							tempOS[i] = offset[i];
						}
						else
						{
							tempKey[i] = key[i - 1];
							tempOS[i] = offset[i - 1];
						}
					}
				}
				else
				{
					tempKey[i] = key[i-1];
					tempOS[i] = offset[i-1];
				}
			}

			btree.seek(0);
			long numNodes = btree.readLong();
			long newStart = 16 + (numNodes * 112);

			childNode[0] = new Node(newStart);
			for(int i = 0; i < ORDER/2; i++)
				childNode[0].insert(tempKey[i], tempOS[i]);
			childNode[0].setParent(pos);

			childNode[1] = new Node(newStart + 122);
			for(int i = (ORDER/2) + 1; i < ORDER - 1; i++)
				childNode[1].insert(tempKey[i], tempOS[i]);
			childNode[1].setParent(pos);

			reset();

			key[0] = tempKey[ORDER/2];
			offset[0] = tempOS[ORDER/2];
			btree.seek(pos + 16);
			btree.writeLong(key[0]);
		}

		public void reset()
		{
			for(int i = 0; i < ORDER - 1; i++)
			{
				key[i] = -1;
				offset[i] = -1;
			}

			for(int i = 0; i < ORDER; i++)
			{
				child[i] = -1;
				childNode[i] = null;
			}
		}

		public void setParent(long p)
		{
			parent = p;
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