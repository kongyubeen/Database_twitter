package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    
    // 1. DB 연결 정보 (★반드시 본인 환경에 맞게 수정하세요★)
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";// MySQL 8.0 이상
    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/twitter?serverTimezone=Asia/Seoul&useSSL=false&allowPublicKeyRetrieval=true";// 'my_twitter_db' 부분은 본인 DB 이름으로!
    private static final String DB_USER = "root"; // 본인 MySQL 아이디
    private static final String DB_PASS = "Orcle137!"; // 본인 MySQL 비밀번호

    private static Connection conn = null;
    // 2. 연결 객체를 반환하는 static 메소드
    public static Connection getConnection() throws SQLException {
        if (conn == null) { // 연결이 아직 안됐으면
            try {
                // 3. 드라이버 로드
                Class.forName(DB_DRIVER); 
                // 4. 연결 시도
                conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                System.out.println("!! MySQL 드라이버를 찾을 수 없습니다 !!");
            }
        }
        return conn;
    }
}