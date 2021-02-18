package jdbc_database;

import java.sql.*;

public class Mysql {
    private String url = "jdbc:mysql://localhost:3306/db_gym";
    private String user = "root";
    private String password = "";
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
