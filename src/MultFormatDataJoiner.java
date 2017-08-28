import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.LabeledCSVParser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.StringReader;
import java.lang.reflect.Type;
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
					Gson g = new Gson();
					add(g, path);
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
		
		private void add(Gson g, String path) {
			try {
				String jsonFile = new String(Files.readAllBytes(Paths.get(path)));
				Type listType = new TypeToken<LinkedList<Data>>(){}.getType();
				@SuppressWarnings("unchecked")
				List<Data> proData = (LinkedList<Data>) g.fromJson(jsonFile, listType);
				dataList.addAll(proData);
			} catch (IOException e) {e.printStackTrace();}
		}
/*		
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
			private int user_id;		
			private String first_name;			
			private String last_name;			
			private String username;
			private String user_type;
			private String last_login_time;
			
			Data(String id, String firstName, String lastName, 
					String username, String type, String lastLoginTime) {
				this.user_id = Integer.parseInt(id);
				this.first_name = firstName;
				this.last_name = lastName;
				this.username = username;
				this.user_type = type;
				this.last_login_time = lastLoginTime;
			}
			
			public String toString() {
				return (Integer.toString(user_id)+"   "+first_name+"   "+last_name+
						"   "+username+"   "+user_type+"   "+last_login_time);
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
