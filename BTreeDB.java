import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class BTreeDB {
	
	public static RandomAccessFile bt;
	public static RandomAccessFile val;
	public static boolean exit;
	
	public static void main( String[] args ) throws FileNotFoundException, IOException {
		
		bt = new RandomAccessFile(args[0], "rwd");

		ValueManager vm = new ValueManager( args[1] );
				
		while ( exit != true ) {
			acceptCommands(vm);
		}
	}
		
	public static void acceptCommands( ValueManager vm ) {
		
		Scanner sc = new Scanner( System.in );
		
		String s = sc.nextLine();
		String[] com = s.split( " " , 2 );

		//If there an input error
		try {

		} catch (IOException e) {

		}
		
		if ( com.length == 1 && !com[0].equals( "exit" ) )
			System.out.println( "ERROR: invalid command" );
		else if ( com[0].equals( "insert" ) ) {
			vm.insert( com[1] );
		}
		else if ( com[0].equals( "update" ) ) {
			update( Long.parseLong( com[1] ) );
		}
		else if ( com[0].equals( "select" ) ) {
			select( Long.parseLong( com[1] ) );
		}
		else {
			exit = true;
		}
		
	}
	
	public static void update( long key ) {
		
		System.out.printf( " --> in method update( long key , String value ), with key = %d and value = n" , key );
		
	}
	public static void select( long key ) {
		
		System.out.printf( " --> in method select( long key ), with key = %d\n" , key );
		
	}
	
}