import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class ValueManager {
	
	public RandomAccessFile val;
	public long numRecords;
	
	public ValueManager() {}
		
	public ValueManager( String name ) {
				
		File f = new File(name);
		
		if (f.exists() && !f.isDirectory()) {
			
			System.out.println( "File exists" );
			
			try {
				val = new RandomAccessFile( name , "rwd" );
				try {
					numRecords = val.readLong();
				} catch (IOException e) {
					numRecords = 0;
				}
			} catch ( FileNotFoundException fnfe ) {} 
		}

		else
		{
			try {
				val = new RandomAccessFile(name, "rwd");
				numRecords = 0;
			} catch (IOException e) {}
		}
	}
	
	public long insert( String value ) {
		
		long index = 0;

		String in[] = value.split(" ", 2);
		
		System.out.println( " --> in method insert( long key, String value ), with key = " + in[0] + " and value = " + in[1]);
				
		try {
			val.seek( 8 + 256 * numRecords );
			val.writeByte( in[1].length() );
			val.writeBytes( in[1] );
			numRecords++;
			val.seek(0);
			val.writeLong(numRecords);
		} catch ( IOException e ) {
			System.out.println( "IOException in insert method" );
		}
		
		return index;
		
	}
	
	public void close() {
		
		try {
			val.close();
		} catch ( IOException e ) {
			System.out.println( "IOException in close method" );
		}
		
	}
	
}