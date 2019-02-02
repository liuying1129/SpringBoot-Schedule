package com.yklis.schedule.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DbUtil {
	
	private static Logger logger = LoggerFactory.getLogger(DbUtil.class);

	private static ComboPooledDataSource cpds=null;  
    static{  
        //这里有个优点，写好配置文件，想换数据库，简单 ,更改参数即可
        cpds = new ComboPooledDataSource("sqlServer2000");
    }
    
    /** 
     * 获得数据库连接 
     * @return   Connection 
     */  
    public static Connection getConnection(){  
    	
    	/*
    	 * 非c3p0连接池，原生jdbc连接
    	try {
			Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver") ;
		} catch (ClassNotFoundException e1) {			
			e1.printStackTrace();
		}
    	try {
			Connection con = DriverManager.getConnection("jdbc:microsoft:sqlserver://211.97.0.5:1433; DatabaseName=TMS","liu","");
		} catch (SQLException e1) {
			e1.printStackTrace();
		}*/
    	
        try {  
            return cpds.getConnection();  
        } catch (SQLException e) {  
        	logger.error("连接数据库失败:"+e.toString());
            return null;  
        }  
    }  
      
    /** 
     * 数据库关闭操作 
     * @param conn   
     * @param st     
     * @param pst 
     * @param rs 
     */  
    public static void close(Connection conn,Statement st,PreparedStatement pst,ResultSet rs){  
        if(rs!=null){  
            try {  
                rs.close();  
            } catch (SQLException e) {  
            	logger.error("ResultSet关闭失败:"+e.toString());
            }  
        } 
        if(st!=null){  
	        try {
				st.close();
			} catch (SQLException e) {
				logger.error("Statement关闭失败:"+e.toString());
			}
        }
        if(pst!=null){  
            try {  
                pst.close();  
            } catch (SQLException e) {  
            	logger.error("PreparedStatement关闭失败:"+e.toString());
            }  
        }  
  
        if(conn!=null){  
            try {  
                conn.close();  
            } catch (SQLException e) {  
            	logger.error("Connection关闭失败:"+e.toString());
            }  
        }  
    }  

}
