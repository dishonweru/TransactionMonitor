import java.sql.Timestamp;
import java.util.Date;
import java.util.TimerTask;

public class Chrono extends TimerTask {

	public void run() {
		Date startDate = new Date();
		try {
					System.out
					.println("---------------------Starting TransactionServer Check Cycle At:"
							+ " "
							+ new Timestamp(startDate.getTime())
							+ "------------------------------------");
			TransactionCore.dbConnect();
			TransactionCore.transactionCheck();
			//performTillUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
