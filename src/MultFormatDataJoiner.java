import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.CSVPrinter;
import com.Ostermiller.util.LabeledCSVParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.StringReader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MultFormatDataJoiner {
	
	private static class DataCompiler {
		private List<Data> dataList;
		private boolean autosort;
		
		DataCompiler() {
			dataList = new LinkedList<>();
			autosort = false;
		}
		
		//Disabled by default, if enabled data sorts each time more is added
		public void autoSort(boolean in) {
			this.autosort = in;
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
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					try {
						add(dbFactory, path);
					} catch (Exception e) {e.printStackTrace();}
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
				if(autosort) sort();
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
				if(autosort) sort();
			} catch (IOException e) {e.printStackTrace();}
		}

		private void add(DocumentBuilderFactory dbFactory, String path) {
			try {				
				File fXmlFile = new File(path);
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(fXmlFile);
				doc.getDocumentElement().normalize();
				NodeList nList = doc.getElementsByTagName("user");
				for (int temp = 0; temp < nList.getLength(); temp++) {
					Node nNode = nList.item(temp);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						Data data = new Data(eElement.getElementsByTagName("userid").item(0).getTextContent(), 
											 eElement.getElementsByTagName("firstname").item(0).getTextContent(), 
											 eElement.getElementsByTagName("surname").item(0).getTextContent(), 
											 eElement.getElementsByTagName("username").item(0).getTextContent(), 
											 eElement.getElementsByTagName("type").item(0).getTextContent(), 
											 eElement.getElementsByTagName("lastlogintime").item(0).getTextContent());
						dataList.add(data);
					}
				}
				if(autosort) sort();
			} catch(Exception e) {e.printStackTrace();}
		}

		public void sort() {
			Collections.sort(dataList, Comparator.comparingInt(Data::getId));
		}
		
		public void write(String path) {
			//Nesting writer methods in inner DataWriter class to keep code clean
			new DataWriter(path);
		}
		
		private class DataWriter {
			private String path;
			
			DataWriter(String path) {
				this.path = path;
				writeCsv();
				writeJson();
				writeXML();
				System.out.println("Compiled data written to "+path+" folder.");
			}
			
			private void writeCsv() {
				try{
				    PrintWriter writer = new PrintWriter(path+"/users.csv", "UTF-8");
				    CSVPrinter csvp = new CSVPrinter(writer);
				    csvp.writeln(new String[]{"User ID","First Name","Last Name","Username","User Type","Last Login Time"});
				    for(Data data : dataList) {
				    	csvp.writeln(new String[]{Integer.toString(data.getId()),
				    							  data.getFirstName(),
				    							  data.getLastName(),
				    							  data.getUsername(),
				    							  data.getUserType(),
				    							  data.getLastLoginTime()});
				    }
				    writer.close();
				} catch (Exception e) {e.printStackTrace();}
			}
			
			private void writeJson() {
				try {
					PrintWriter writer = new PrintWriter(path+"/users.json", "UTF-8");
					Gson g = new GsonBuilder().setPrettyPrinting().create();
		            writer.write(g.toJson(dataList));
		            writer.close();
				} catch(Exception e){e.printStackTrace();}
			}
			
			private void writeXML() {
				try {
				    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
				    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
				    Document doc = docBuilder.newDocument();
				    
				    Element rootElement = doc.createElement("users");
				    doc.appendChild(rootElement);
				    
				    for(Data data : dataList) {
				    	Element base = doc.createElement("user");
				    	
				    	Element id = doc.createElement("userid");
					    id.appendChild(doc.createTextNode(Integer.toString(data.getId())));
					    base.appendChild(id);
					    
					    Element fName = doc.createElement("firstname");
					    fName.appendChild(doc.createTextNode(data.getFirstName()));
					    base.appendChild(fName);
					    
					    Element sName = doc.createElement("surname");
					    sName.appendChild(doc.createTextNode(data.getLastName()));
					    base.appendChild(sName);
					    
					    Element userName = doc.createElement("username");
					    userName.appendChild(doc.createTextNode(data.getUsername()));
					    base.appendChild(userName);
					    
					    Element type = doc.createElement("type");
					    type.appendChild(doc.createTextNode(data.getUserType()));
					    base.appendChild(type);
					    
					    Element lastLog = doc.createElement("lastlogintime");
					    lastLog.appendChild(doc.createTextNode(data.getLastLoginTime()));
					    base.appendChild(lastLog);
				    	
					    rootElement.appendChild(base);
				    }
				    
				    TransformerFactory transformerFactory = TransformerFactory.newInstance();
				    Transformer transformer = transformerFactory.newTransformer();
				    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
				    DOMSource source = new DOMSource(doc);
				    StreamResult result = new StreamResult(new File(path+"/users.xml"));
				    transformer.transform(source, result);
				} catch (Exception e) {e.printStackTrace();}
			}
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
			
			public int getId() {
				return user_id;
			}
			
			public String getFirstName() {
				return first_name;
			}
			
			public String getLastName() {
				return last_name;
			}
			
			public String getUsername() {
				return username;
			}
			
			public String getUserType() {
				return user_type;
			}
			
			public String getLastLoginTime() {
				return last_login_time;
			}
			
			public String toString() {
				return (Integer.toString(user_id)+"   "+first_name+"   "+last_name+
						"   "+username+"   "+user_type+"   "+last_login_time);
			}
		}
	}

	public static void main(String[] args) {
		DataCompiler data = new DataCompiler();
		
		//false by default, if enabled compiled data will
		//be sorted every time another chunk is added
		//data.autoSort(true);
		
		data.add("Data/users.csv");
		data.add("Data/users.json");
		data.add("Data/users.xml");
		
		//not needed if autoSort is enabled
		data.sort();
		
		//prints compiled data to console	
		//data.printDataList();
		
		//folder name for output data
		data.write("Result");
	}

}
