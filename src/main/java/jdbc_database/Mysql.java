package jdbc_database;

import java.sql.*;

public class Mysql {
//    private String url = "jdbc:mysql://localhost:3306/dbgym";
//    private String user = "root";
//    private String password = "password";
//    private String password = "1234";
    private String url = "jdbc:mysql://localhost:3306/dbgym";
    private String user = "root";
    private String password = "password";
    private Connection conexion= null;
    
    public Mysql(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try{
            conexion = DriverManager.getConnection(url, user, password);
        }catch(SQLException error){
            error.printStackTrace();
        }
    }
    
    public Connection getConexion(){
        return conexion;
    }
}
