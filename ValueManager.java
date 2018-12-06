import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class ValueManager {
	
	private RandomAccessFile values;
	private long numRecords;
	
	private final int LENGTH_OF_STR_LENGTH = 2; // the length of the string
	private final int MAX_LENGTH_OF_ENTRY = 256; // the actual entry length
	private final int COMPLETE_ENTRY = LENGTH_OF_STR_LENGTH + MAX_LENGTH_OF_ENTRY;
	
	public ValueManager( String name ) throws FileNotFoundException , IOException {
		
		// check if "data.val" or otherwise named value file exists
		File f = new File( name );
		
		if ( !f.exists() ) {
			
			values = new RandomAccessFile( "data.val" , "rwd" );
			numRecords = 0;
			values.writeLong( numRecords );
			
		} else {
			
			values = new RandomAccessFile( f , "rwd" );
			values.seek( 0 );
			numRecords = values.readLong();
			
		}
		
	}
	
	public void insert( String value ) throws IOException {
		
		values.seek( 8 + numRecords * COMPLETE_ENTRY );
		
		values.writeShort( value.length() ); // googled max value of byte - got 127 so changed byte to short to accommodate possible greaters lengths (128-256)
		values.writeBytes( value );
		
		// increment number of records ( offset ) and update .val
		numRecords++;
		values.seek( 0 );
		values.writeLong( numRecords );
		
	}
	
	public String read( long offset ) throws IOException {
		
		// values.seek( 8 + offset * COMPLETE_ENTRY );
		values.seek( offset );
		short length = values.readShort();
		
		// https://stackoverflow.com/questions/33780798/java-how-to-read-from-randomaccessfile-into-string
		byte[] entry = new byte[ length ];
		values.read( entry );
		String output = new String( entry , "UTF8" );
		
		return output;
		
	}
	
	public void update( long offset , String newEntry ) throws IOException {
		
		values.seek( offset );
		values.writeShort( newEntry.length() );
		values.writeBytes( newEntry );
		
	}
	
	public void writeNumRecords() {
		
		try {
			values.seek( 0 );
			values.writeLong( numRecords );
		} catch ( IOException e ) {
			System.out.println( "IOException in writeNumRecords method" );
		}
		
	}
	
	public long getNumRecords() {
		return numRecords;
	}
	
	public void close() throws IOException {
		values.close();
	}
	
}