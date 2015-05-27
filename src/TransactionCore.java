import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.util.Timer;
import java.util.Date;


public class TransactionCore {

	static Connection conn = null;
	Timer timer;
	Toolkit toolkit;
	static PreparedStatement pstmt = null;
	Parameters parameters = null;
	static String[] paramValue = new String[10];
	public static String USER_AGENT = "Mozilla/5.0";
	public static  int counter, successCounter;	
	static Date date;
	
	public TransactionCore(){
		//on class startup, Parameters and Chrono classes are initialized
		Parameters parameters = new Parameters();
		date = new Date();
		System.out.println("--------------------------------New Cycle StartTime :"+new Timestamp(date.getTime())+"----------------------------------------------------");
		timer = new Timer();
		//chrono class inialiazed and set to 30s interval
		timer.schedule(new Chrono(), 0, 30 * 1000); // subsequent rate
	}

	public static void dbConnect() {
		//method initializes connection object to an instance of the running DB Engine
		try {
			paramValue = Parameters.parameters[0].split("=");
			String url = "jdbc:postgresql://" + paramValue[1] + ":"
					+ paramValue[2] + "/" + paramValue[3];
			Class.forName("org.postgresql.Driver");
			// conn = DriverManager.getConnection( url, paramValue[4],
			// paramValue[5]);
			conn = DriverManager.getConnection(
					"jdbc:postgresql://localhost:5432/postgres", "postgres",
					"postgres");
			System.out.println("Database Connection  Created  Successfully!!");

		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public static void transactionCheck() throws Exception {
		//method fetches un-checked transactions from db and fires them to the server for confirmation.
		String sql = "Select * from transaction where tran_status ='05'";
		counter = 0;
		successCounter = 0;
		try {				
			String url = Parameters.parameters[1];
			
			pstmt = conn.prepareStatement(sql);
			//excuting db select query and assign results to a resultset object
			ResultSet rs = pstmt.executeQuery();
			//System.out.println("txn Selection  Success!!: "+rs.getFetchSize());

			while (rs.next()) {
				//get the transaction parameters from each row returned and construct url 
				String request_url = url
						+ "txn_ref="
						+ "\""
						+ rs.getString("tran_ref")
						+ "\""
						+ "&txn_origin="
						+ "\""
						+ rs.getString("tran_origin")
						+ "\"";
				System.out.println("Request Url Is: " + request_url);
				
				//Sending transation for confirmation via HTTP GET
				String rsp = sendGet(request_url);
				System.out.println("Response From Web Service: " + rsp);

				if (rsp.contentEquals("Ok")) {
					//if response from server is ok, update transation state in db
					String update_query = "Update transaction set tran_status = '00' where tran_status = '05' and tran_ref ="+"'"+rs.getString("tran_ref") + "'"+" and tran_origin ="+"'"+ rs.getString("tran_origin")+"'";
					//System.out.println(update_query);
					pstmt = conn.prepareStatement(update_query);
					int update_status = pstmt.executeUpdate();
					if(update_status == 1){
						System.out.println("Successfully Fetched and Updated TransactionServer Ref "+rs.getString("tran_ref") + "  For user: " + rs.getString("tran_origin"));
						successCounter++;
					}
				}
				counter++;
			}
			System.out.println("Total Un-Verified Transactions Fetched: " + counter + "   Successfully Verified Transactions: "+ successCounter);
			conn.close();
			System.out.println("Database Connection  Closed  Successfully!!");
			System.out.println("--------------------------------TransactionServer Check Cycle Ends At:"+new Timestamp(date.getTime())+"----------------------------------------------------\n\n\n\n\n\n");
		} catch (Exception ex) {
			ex.printStackTrace();
			conn.close();
			System.out.println("Database Connection  Closed  Successfully!!");
			System.out.println("--------------------------------TransactionServer Check Cycle Ends At:"+new Timestamp(date.getTime())+"----------------------------------------------------\n\n\n\n\n\n");
		}
		
	}

	public static String sendGet(String url) throws Exception {
		//method fires http GET request to transaction server for confirmation
		StringBuffer response = new StringBuffer();
		try {
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			// add request header
			con.setRequestProperty("User-Agent", USER_AGENT);

			int responseCode = con.getResponseCode();
			//check if request was successful
			if (responseCode == 200) {
				System.out.println("\nSending 'GET' request to URL : " + url);
				System.out.println("Response Code : " + responseCode);

				BufferedReader in = new BufferedReader(new InputStreamReader(
						con.getInputStream()));
				String inputLine;

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
			} else {
				System.out
						.println("Failed To Secure A response From Transaction Server with http response code:"
								+ responseCode
								+ "......Check Service Availablity.....");

			}
		} catch (Exception ex) {
			System.out.println("An Error Occurred When Firing HTTP Request URL"
					+ " " + url);
			conn.close();
			System.out.println("Database Connection  Closed  Successfully!!");
			System.out.println("--------------------------------TransactionServer Check Cycle Ends At:"+new Timestamp(date.getTime())+"----------------------------------------------------\n\n\n\n\n\n");
		}

		// print result
		System.out.println(response.toString());
		return response.toString();

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("--------------------------------TransactionServer Status Check(Process Will Run At Intervals of 1 Min)----------------------------------------------------");
		//initialize the constructor method here
		TransactionCore transactioncore = new TransactionCore();
		}

}
