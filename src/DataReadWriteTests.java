import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class DataReadWriteTests {

	@Test
	public void testReaderSingle() {
		MultFormatDataJoiner.DataCompiler data = new MultFormatDataJoiner.DataCompiler();
		assertNotNull(data);

		//Loading csv alone
		data.add("TestResources/Data/users.csv");
		assertEquals("3   David   Payne   Dpayne   Manager   23-09-2014 09:35:02\n" + 
					 "10   Ruby   Wax   ruby   Employee   12-12-2014 08:09:13", data.toString());
		
		//Loading json alone
		data = new MultFormatDataJoiner.DataCompiler();
		data.add("TestResources/Data/users.json");
		assertEquals("4   Joe   Public   joey99   Employee   22-09-2014 08:23:54\n" + 
					 "5   Gary   May   gary23   Manager   02-01-2015 12:09:35\n" + 
					 "6   Jessica   James   jj56   Employee   13-01-2015 08:56:12\n" + 
					 "7   Frank   Bruno   franky   employee   12-01-2015 16:15:43\n" + 
					 "8   Bob   Marley   bobby3   Manager   01-02-2015 13:01:00", data.toString());
		
		//Loading xml alone
				data = new MultFormatDataJoiner.DataCompiler();
				data.add("TestResources/Data/users.xml");
				assertEquals("1   John   Doe   John1   Employee   12-01-2015 12:01:34\n" + 
							 "2   Mary   Jane   Mary23   Employee   01-11-2014 13:45:42\n" + 
							 "9   Shaun   Stevens   shaun   Employee   01-02-2015 13:00:00", data.toString());
		
	}
	
	@Test
	public void testReaderMulti() {
		MultFormatDataJoiner.DataCompiler data = new MultFormatDataJoiner.DataCompiler();
		assertNotNull(data);	
		data.add("TestResources/Data/users.csv");
		data.add("TestResources/Data/users.json");
		
		//Loading csv and json together
		assertEquals("3   David   Payne   Dpayne   Manager   23-09-2014 09:35:02\n" + 
					 "10   Ruby   Wax   ruby   Employee   12-12-2014 08:09:13\n" +
					 "4   Joe   Public   joey99   Employee   22-09-2014 08:23:54\n" + 
					 "5   Gary   May   gary23   Manager   02-01-2015 12:09:35\n" + 
					 "6   Jessica   James   jj56   Employee   13-01-2015 08:56:12\n" + 
					 "7   Frank   Bruno   franky   employee   12-01-2015 16:15:43\n" + 
					 "8   Bob   Marley   bobby3   Manager   01-02-2015 13:01:00", data.toString());
		
		//Check sort works
		data.sort();
		assertEquals("3   David   Payne   Dpayne   Manager   23-09-2014 09:35:02\n" + 
					 "4   Joe   Public   joey99   Employee   22-09-2014 08:23:54\n" + 
					 "5   Gary   May   gary23   Manager   02-01-2015 12:09:35\n" + 
					 "6   Jessica   James   jj56   Employee   13-01-2015 08:56:12\n" + 
					 "7   Frank   Bruno   franky   employee   12-01-2015 16:15:43\n" + 
					 "8   Bob   Marley   bobby3   Manager   01-02-2015 13:01:00\n" + 
					 "10   Ruby   Wax   ruby   Employee   12-12-2014 08:09:13", data.toString());
		
		//Enabled autosort, add xml in and check its sorted by default
		data.autoSort(true);
		data.add("TestResources/Data/users.xml");
		assertEquals("1   John   Doe   John1   Employee   12-01-2015 12:01:34\n" + 
					 "2   Mary   Jane   Mary23   Employee   01-11-2014 13:45:42\n" + 
					 "3   David   Payne   Dpayne   Manager   23-09-2014 09:35:02\n" + 
					 "4   Joe   Public   joey99   Employee   22-09-2014 08:23:54\n" + 
					 "5   Gary   May   gary23   Manager   02-01-2015 12:09:35\n" + 
					 "6   Jessica   James   jj56   Employee   13-01-2015 08:56:12\n" + 
					 "7   Frank   Bruno   franky   employee   12-01-2015 16:15:43\n" + 
					 "8   Bob   Marley   bobby3   Manager   01-02-2015 13:01:00\n" + 
				 	 "9   Shaun   Stevens   shaun   Employee   01-02-2015 13:00:00\n" + 
				 	 "10   Ruby   Wax   ruby   Employee   12-12-2014 08:09:13", data.toString());
	}
	
	@Test
	public void testWriter() throws IOException {
		MultFormatDataJoiner.DataCompiler data = new MultFormatDataJoiner.DataCompiler();
		assertNotNull(data);
		
		//add data to DataCompiler and clear any existing test result files
		data.add("TestResources/Data/users.csv");
		data.add("TestResources/Data/users.json");
		data.add("TestResources/Data/users.xml");		
		Path fileToDeletePath = Paths.get("TestResources/Results/users.csv");
		Files.deleteIfExists(fileToDeletePath);
		fileToDeletePath = Paths.get("TestResources/Results/users.json");
		Files.deleteIfExists(fileToDeletePath);
		fileToDeletePath = Paths.get("TestResources/Results/users.xml");
		Files.deleteIfExists(fileToDeletePath);

		
		//test unsorted files were written correctly
		data.write("TestResources/Results");
		String csv = new String(Files.readAllBytes(Paths.get("TestResources/Results/users.csv")));
		String json = new String(Files.readAllBytes(Paths.get("TestResources/Results/users.json")));
		String xml = new String(Files.readAllBytes(Paths.get("TestResources/Results/users.xml")));
		
		//regex csv
		String[] lines = csv.split(System.getProperty("line.separator"));
		Pattern p = Pattern.compile("([0-9]+)");
		int[] resu = new int[10];
		int i = 0;
		for (String s : lines) {
			Matcher m = p.matcher(s);
			if (m.find()) resu[i++] = Integer.parseInt(m.group(1));
		}
		int[] expec = {3, 10, 4, 5, 6, 7, 8, 1, 2, 9};
		assertArrayEquals(expec, resu);
	
		
		//test data sorted and files rewritten correctly
		data.sort();
		data.write("TestResources/Results");
		csv = new String(Files.readAllBytes(Paths.get("TestResources/Results/users.csv")));
		json = new String(Files.readAllBytes(Paths.get("TestResources/Results/users.json")));
		xml = new String(Files.readAllBytes(Paths.get("TestResources/Results/users.xml")));
		
		//regex csv
		String[] linesOrd = csv.split(System.getProperty("line.separator"));
		int[] resuOrd = new int[10];
		i = 0;
		for (String s : linesOrd) {
			Matcher m = p.matcher(s);
			if (m.find()) resuOrd[i++] = Integer.parseInt(m.group(1));
		}
		int[] expecOrd = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
		assertArrayEquals(expecOrd, resuOrd);
	}

}
