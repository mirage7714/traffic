import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/*
 * Date:2016/02/04
 * Version:v1.2
 * Main features:1. Auto download xml date to sqlite per five minutes.
 * 				 2. Download weather forecast per day.
 *     			 3. Output for xml file to xls file twice per day, 
 *     				which means once at noon and once at night.
 *     			 4. The limitation of sqlite table number is 71688, 
 *     			  	and the limitation of xls per page is 65536.
 * Main change: 1. Remove output to xls function. 
 * 				   This function will be executed by another application.
 * 
*/

public class XmlLoadToDBv1_2 {
	
	static Date date = new Date();
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMddHHmm");
	public static String title = "traffic" + sdf.format(date);
	static boolean exist = false;
	public static String state;
	public static int a = 0;
	static String newtitle;
	static int out = 0;
	@SuppressWarnings({ "deprecation", "static-access" })
	public static void main(String [] args){
		WeatherForecast weather = new WeatherForecast();
		weather.Extract();
		System.out.println("weather load finish");
		load: while (true) {
			Calendar cal = Calendar.getInstance();
			System.out.println("wake up at " + cal.getTime());
			if(cal.getTime().getHours()<12){
				newtitle = title+"am";
			}
			else{
				newtitle=title+"pm";
			}
			if (cal.getTime().getHours() >= 19 && cal.getTime().getMinutes() >= 30) {
				break load;
			}
			Extract();
			System.out.println("sleep at " + Calendar.getInstance().getTime());
			System.out.println("-------------------");
			try {
				Thread.sleep(240000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("output finish at " + Calendar.getInstance().getTime());
	}
	
	public static void Extract() {
		Long t1 = Calendar.getInstance().getTimeInMillis();
		int batchsize = 0;
		try {
			URL url = new URL(
					"http://data.taipei/opendata/datalist/apiAccess?scope=resourceAquire&rid=5aacba65-afda-4ad5-88f5-6026934140e6&format=xml");
			try (Connection conn = DriverManager.getConnection("jdbc:sqlite:traffic.db");
					Statement st = conn.createStatement();) {
				Date record = new Date();
				String n = sdf2.format(record);
			
				st.execute("CREATE TABLE IF NOT EXISTS "+newtitle+" (SectionId TEXT,SectionName TEXT, AVGSpd TEXT,AvgOcc TEXT,TotalVol TEXT,MOELevel TEXT,StartWgsY TEXT,StartWgsX TEXT,EndWgsY TEXT,EndWgsX TEXT,time TEXT)");
				PreparedStatement pstmt = conn
						.prepareStatement("insert into " + newtitle + " values (?,?,?,?,?,?,?,?,?,?,?)");
				Calendar cal = Calendar.getInstance();
				System.out.println(cal.getTime());
				Document xmlDoc = Jsoup.parse(url, 2000000);
				Elements SectionName = xmlDoc.select("SectionName");
				Elements SectionId = xmlDoc.select("SectionId");
				Elements AvgSpd = xmlDoc.select("AvgSpd");
				Elements AvgOcc = xmlDoc.select("AvgOcc");
				Elements MOELevel = xmlDoc.select("MOELevel");
				Elements TotalVol = xmlDoc.select("TotalVol");
				Elements StartWgsX = xmlDoc.select("StartWgsX");
				Elements StartWgsY = xmlDoc.select("StartWgsY");
				Elements EndWgsX = xmlDoc.select("EndWgsX");
				Elements EndWgsY = xmlDoc.select("EndWgsY");

				for (int i = 0; i < SectionId.size(); i++) {
					pstmt.setString(1, SectionName.get(i).text());
					pstmt.setString(2, SectionId.get(i).text());
					pstmt.setString(3, AvgSpd.get(i).text());
					pstmt.setString(4, AvgOcc.get(i).text());
					pstmt.setString(5, TotalVol.get(i).text());
					pstmt.setString(6, MOELevel.get(i).text());
					pstmt.setString(7, StartWgsY.get(i).text());
					pstmt.setString(8, StartWgsX.get(i).text());
					pstmt.setString(9, EndWgsY.get(i).text());
					pstmt.setString(10, EndWgsX.get(i).text());
					pstmt.setString(11, n);
					pstmt.addBatch();
					batchsize++;
					if (batchsize % 1100 == 0) {
						pstmt.executeBatch();
						pstmt.clearBatch();
					}
				}
				pstmt.executeBatch();

				Long t2 = Calendar.getInstance().getTimeInMillis();
				String t = Long.toString((t2 - t1)/1000);
				System.out.println("Time of traffic extract is "+t+" s");
				System.out.println(t);
			} catch (IOException | SQLException e) {
				e.printStackTrace();
			}
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
	}
	
}
