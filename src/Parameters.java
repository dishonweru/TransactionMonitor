

import java.io.*;

public class Parameters {
	public static String[] parameters = new String[10];
	
	public Parameters(){
		try{
			File dir = new File(".");
			File fin = new File(dir.getCanonicalPath() + File.separator + "params.txt");
			readFile(fin);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
		
	public static String[] readFile(File file) throws Exception {
		int counter = 0;
		try{
		FileInputStream fis = new FileInputStream(file);
		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
		
		while((parameters[counter] = br.readLine()) != null){
			System.out.println(parameters[counter]);
			counter++;			
		}
		}catch(Exception ex){
			ex.printStackTrace();			
		}
		return parameters;
	}

}
