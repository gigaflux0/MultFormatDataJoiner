import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.LabeledCSVParser;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MultFormatDataJoiner {
	
	private static class DataCompiler {
		List<Data> dataList;
		
		DataCompiler() {
			dataList = new LinkedList<>();
		}
		
		public void add(String path) {
			//Regex - capture everything after last dot, check if it matches file types
			Pattern p = Pattern.compile(".*\\.(csv|json|xml?)");
			Matcher m = p.matcher(path);
			if (m.find()) {
				if (m.group(1).equals("csv")) {
					try {
						LabeledCSVParser lcsvp = new LabeledCSVParser(
								new CSVParser(new StringReader(
								new String(Files.readAllBytes(Paths.get(path))))));
						add(lcsvp);
					} catch (IOException e) {e.printStackTrace();}	
				}
				else if (m.group(1).equals("json")) {
					System.out.println("oh aye2");
				}
				else {
					System.out.println("oh aye3");
				}
			}
			else
				System.out.println("Data type not recognised");
		}
		
		
		private void add(LabeledCSVParser lcsvp) {
			try {
				while(lcsvp.getLine() != null){
					Data data = new Data(lcsvp.getValueByLabel("User ID"), 
										 lcsvp.getValueByLabel("First Name"), 
										 lcsvp.getValueByLabel("Last Name"), 
										 lcsvp.getValueByLabel("Username"), 
										 lcsvp.getValueByLabel("User Type"), 
										 lcsvp.getValueByLabel("Last Login Time"));
					dataList.add(data);
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
/*		
		private void add(//JSON) {
			//
		}
		
		private void add(//XML) {
			//
		}
*/
		
		public void write(String path) {
			//
		}
		
		public void printDataList() {
			for(Data data : dataList) {
				System.out.println(data);
			}
		}
		
		private class Data {
			private int id;		
			private String firstName;			
			private String lastName;			
			private String username;
			private String type;
			private String lastLoginTime;
			
			Data(String id, String firstName, String lastName, 
					String username, String type, String lastLoginTime) {
				this.id = Integer.parseInt(id);
				this.firstName = firstName;
				this.lastName = lastName;
				this.username = username;
				this.type = type;
				this.lastLoginTime = lastLoginTime;
			}
			
			public String toString() {
				return (Integer.toString(id)+"   "+firstName+"   "+lastName+
						"   "+username+"   "+type+"   "+lastLoginTime);
			}
		}
	}

	public static void main(String[] args) {
		DataCompiler data = new DataCompiler();
		data.add("Data/users.csv");
		data.add("Data/users.json");
		data.add("Data/users.xml");
		data.printDataList();
		//folder name for output data
		data.write("Result");
	}

}
