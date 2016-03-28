
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/*
 * Date:2016/02/04
 * Version:v1.2
 * Main features:1. Auto download xml date to sqlite per five minutes.
 * 				 2. Download weather forecast per day.
 *     			 3. Output for xml file to xls file twice per day, 
 *     				which means once at noon and once at night.
 *     			 4. The limitation of sqlite table number is 71688, 
 *     			  	and the limitation of xls per page is 65536.
 *     
 */
public class XmlLoadToDBv1_3 {

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
	public static void main(String[] args) {
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
			/*
			if(out==0 && cal.getTime().getHours()==13){
				Export((title+"am"));
				out++;
			
			}
			*/
			if (cal.getTime().getHours() >= 19 && cal.getTime().getMinutes() >= 30) {
				break load;
			}
			Extract();
			System.out.println("sleep at " + Calendar.getInstance().getTime());
			System.out.println("-------------------");
			/*
			try {
				Thread.sleep(240000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			*/
		}
	
		//Export(newtitle);
		System.out.println("output finish at " + Calendar.getInstance().getTime());
	
	}
	

	public static void Export(String newname) {
		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:traffic.db");
				Statement st = conn.createStatement();
				ResultSet rs1 = st.executeQuery("select * from " + newname + " order by SectionId,time");
				) {
			WritableWorkbook workbook;
			try {
				workbook = Workbook.createWorkbook(new File(newtitle + ".xls"));
				WritableSheet sheet = workbook.createSheet(newtitle, 0);
				WritableCellFormat fontformat = new WritableCellFormat(new WritableFont(WritableFont.ARIAL, 12));
				String[] name = new String[] { "SectionName", "SectionId", "AvgSpd", "AvgOcc", "TotalVol", "MOELevel",
						"StartWgsY", "StartWgsX", "EndWgsY", "EndWgsX", "time" };
				for (int k = 0; k < name.length; k++) {
					Label label = new Label(k, 0, name[k], fontformat);
					try {
						sheet.addCell(label);
					} catch (WriteException e) {
						e.printStackTrace();
					}
				}
				int i = 0;
				while (rs1.next()) {

					Label label1 = new Label(0, i + 1, rs1.getString("SectionName"), fontformat);
					Label label2 = new Label(1, i + 1, rs1.getString("SectionId"), fontformat);
					Label label3 = new Label(2, i + 1, rs1.getString("AvgSpd"), fontformat);
					Label label4 = new Label(3, i + 1, rs1.getString("AvgOcc"), fontformat);
					Label label5 = new Label(4, i + 1, rs1.getString("TotalVol"), fontformat);
					Label label6 = new Label(5, i + 1, rs1.getString("MOELevel"), fontformat);
					Label label7 = new Label(6, i + 1, rs1.getString("StartWgsY"), fontformat);
					Label label8 = new Label(7, i + 1, rs1.getString("StartWgsX"), fontformat);
					Label label9 = new Label(8, i + 1, rs1.getString("EndWgsY"), fontformat);
					Label label10 = new Label(9, i + 1, rs1.getString("EndWgsX"), fontformat);
					Label label11 = new Label(10, i + 1, rs1.getString("time"), fontformat);
					try {
						sheet.addCell(label1);
						sheet.addCell(label2);
						sheet.addCell(label3);
						sheet.addCell(label4);
						sheet.addCell(label5);
						sheet.addCell(label6);
						sheet.addCell(label7);
						sheet.addCell(label8);
						sheet.addCell(label9);
						sheet.addCell(label10);
						sheet.addCell(label11);
						i++;
					} catch (WriteException e) {
						e.printStackTrace();
					}
				}
				
				try {
					workbook.write();
					workbook.close();
					a++;
					System.out.println("output finish");
				} catch (WriteException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
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
				//System.out.println(cal.getTime());
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
				//System.out.println(t);
			} catch (IOException | SQLException e) {
				e.printStackTrace();
			}
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
	}
}
