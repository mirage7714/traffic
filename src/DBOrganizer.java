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
			String[] roads = new String[] { "�����j�D �ָs�G��-����", "�����j�D �ָs�@��-���Y��", "���s�_�� ������-�p�_��", "���s�_�� �p�_��-��e��",
					"���s�_�� �w��F��-���۸�", "���ͪF�� �ذ�_��-�Q����", "���ͪF�� �Q����-�s�ͥ_��", "���_�_�� �n�ʪF��-�K�w��", "���_�n�� �K�w��-�����j�D",
					"���_�n�� �����j�D-�����F��", "�n�ʦ�� ���y�_��-���_��", "���ͦ�� �Ӽw��-���y�_��", "���ͦ�� ���y�_��-���e�_��", "����� �򶩸�-�_���n��",
					"����� �_���n��-�ذ�n��", "�H�q�� ���_�n��-�򶩸�" };
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