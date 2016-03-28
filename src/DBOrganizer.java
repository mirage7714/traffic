import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBOrganizer {
	public static void main(String[] args) {
		try (Connection conn = DriverManager.getConnection("jdbc:sqlite:traffic.db");
				Connection conn1 = DriverManager.getConnection("jdbc:sqlite:organized.db");
				Statement st = conn.createStatement();
				Statement st1 = conn1.createStatement();) {
			String[] roads = new String[] { "堤頂大道 樂群二路-基湖路", "堤頂大道 樂群一路-港墘路", "中山北路 中正路-小北街", "中山北路 小北街-基河路",
					"中山北路 德行東路-忠誠路", "民生東路 建國北路-松江路", "民生東路 松江路-新生北路", "光復北路 南京東路-八德路", "光復南路 八德路-市民大道",
					"光復南路 市民大道-忠孝東路", "南京西路 重慶北路-西寧北路", "民生西路 承德路-重慶北路", "民生西路 重慶北路-環河北路", "辛亥路 基隆路-復興南路",
					"辛亥路 復興南路-建國南路", "信義路 光復南路-基隆路" };
			for (int n = 0; n < roads.length; n++) {
				String name = "road" + n;
				st1.execute("DROP TABLE IF EXISTS " + name);
				System.out.println("start");
				st1.execute("CREATE TABLE IF NOT EXISTS road" + n
						+ " (SectionId TEXT, AVGSpd TEXT,AvgOcc TEXT,TotalVol TEXT,MOELevel TEXT,time TEXT)");
				PreparedStatement pstmt = conn1.prepareStatement("insert into road" + n + " values (?,?,?,?,?,?)");
				DatabaseMetaData dbmd = conn.getMetaData();
				ResultSet rs2 = dbmd.getTables("traffic", null, null, new String[] { "TABLE" });
				while (rs2.next()) {
					String tablename = rs2.getString("TABLE_NAME");
					ResultSet rs1 = st.executeQuery(
							"select * from " + tablename + " where SectionId like '" + roads[n] + "' order by time");
					while (rs1.next()) {
						System.out.println(rs1.getString(11));
						pstmt.setString(1, rs1.getString(1));
						pstmt.setString(2, rs1.getString(3));
						pstmt.setString(3, rs1.getString(4));
						pstmt.setString(4, rs1.getString(5));
						pstmt.setString(5, rs1.getString(6));
						pstmt.setString(6, rs1.getString(11));
						pstmt.execute();
					}
				}
			}
			System.out.println("organized finshed");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}