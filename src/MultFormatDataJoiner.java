import java.util.LinkedList;
import java.util.List;

public class MultFormatDataJoiner {
	
	private static class DataCompiler {
		List<Data> data;
		
		DataCompiler() {
			data = new LinkedList<>();
		}
		
		public void add(String path) {
			//
		}
		
/*		
		private void add(//CSV) {
			//
		}
		
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
		
		private class Data {
			//
		}
	}

	public static void main(String[] args) {
		DataCompiler data = new DataCompiler();
		data.add("Data/users.csv");
		data.add("Data/users.json");
		data.add("Data/users.xml");
		//folder name for output data
		data.write("Result");
	}

}
