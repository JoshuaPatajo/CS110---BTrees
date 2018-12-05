import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class BTreeDB {
	
	public static boolean exit;
	private static long recordNumber = 0;
	private static BTreeManager btm;
	private static ValueManager vm;
	
	public static void main( String[] args ) throws FileNotFoundException, IOException {
		
		// have the BTM open the .bt
		btm = new BTreeManager( args[0] );
		
		// have VM open the .val
		vm = new ValueManager( args[1] );
		
		Scanner sc = new Scanner( System.in );
		
		while ( exit != true ) {
			acceptCommands( sc.nextLine() );
		}
		
	}
	
	public static void acceptCommands( String s ) throws IOException {
		
		String command = ""; // Expected values: insert , update , select , exit ( , delete )
		long key = 0;
		String value = ""; // May also contain spaces
		
		String[] com = new String[2];
		
		try {
			com = s.split( " " , 2 );
			command = com[0];
			// If command != exit, com[1] should contain the key, and if command != select, com[1] must also contain an entry
		} catch ( ArrayIndexOutOfBoundsException e ) {
			command = s;
		}
		// Splits line of input along first space - isolates command
		
		
		if ( command.equals( "exit" ) ) {
			exit = true;
		}
		else if ( command.equals( "select" ) ) {
			
			// check for key
			
			try {
				key = Long.parseLong( com[1] );
			} catch ( NumberFormatException e ) {
				
				// if "key" isn't a valid long, indicate error and proceed to next input
				System.out.println( " > ERROR: Invalid key" );
				return;

			} catch ( ArrayIndexOutOfBoundsException e ) {
				System.out.println( " > ERROR: Key not specified" );
				return;
			}
			
			long offset = btm.getRoot().select( key ); // is set to -1 if offset key does not have associated entry

			if (offset == -1)
				System.out.println("Key not found");
			else
				System.out.println("Key found: " + vm.read(offset));
			
			// if ( offset >= 0 ) {
			// 	System.out.println( vm.read( offset ) );
			// } else {
			// 	System.out.println( " > ERROR: key is empty" );
			// }
			
		}
		else if ( command.equals( "insert" ) ) {
			
			String[] com2 = com[1].split( " " , 2 );
			// Splits "key ent ry" to "key" and "ent ry"
			
			// Processes key from input, if there is an error with the key, it does not proceed with insertion
			try {
				key = Long.parseLong( com2[0] );
			} catch ( NumberFormatException e ) {
				
				// if "key" isn't a valid long, indicate error and proceed to next input
				System.out.println( " > ERROR: Invalid key" );
				return;
			} catch ( ArrayIndexOutOfBoundsException e ) {
				System.out.println( " > ERROR: Key not specified" );
				return;
			}
			
			// Note: btm.select( key ) returns the ValOffset of the entry at given key, default value is -1 if the entry does not exist
			if ( btm.getRoot().select( key ) >= 0 ) { // i.e. key is not empty
				System.out.println( " > ERROR: Key is not empty" );
			} else {
				try {
					value = com2[1];
				} catch ( ArrayIndexOutOfBoundsException e ) {
					// System.out.println( " > ERROR: Invalid entry" );
					value = "";
				}
				
				vm.insert( value );

				btm.getRoot().insert( key , 8 + (recordNumber * 258));
				recordNumber++;
				System.out.println(key + " was inserted");
			}			
		}
		// else if ( command.equals( "update" ) ) {
			
		// 	String[] com2 = com[1].split( " " , 2 );
			
		// 	try {
		// 		key = Long.parseLong( com[1] );
		// 	} catch ( NumberFormatException e ) {
				
		// 		// if "key" isn't a valid long, indicate error and proceed to next input
		// 		System.out.println( " > ERROR: Invalid key" );
		// 		return;
		// 	}
			
		// 	if ( btm.select( key ) == -1 ) { // i.e. key is empty
		// 		System.out.println( " > ERROR: Key is empty" );
		// 	} else {
				
		// 		try {
		// 			value = com2[1];
		// 		} catch ( NullPointerException e ) {
		// 			System.out.println( " > ERROR: Invalid entry" );
		// 			return;
		// 		}
				
		// 		vm.update( value , key );
		// 		btm.update( key , vm.getNumRecords() );
				
		// 	}
			
		// }
		/* // uncomment if deletion is implemented
		else if ( command.equals( "remove" ) {
			
		} */
		else {
			System.out.println( " > ERROR: invalid command" );
		}
		
	}
	
}