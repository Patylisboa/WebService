package com.rvl.poc.config;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.rvl.poc.model.SolutionRVL;
import com.rvl.poc.util.Constant;


public class DataUtil {

	private final String ADW_PropFileName = "rvl.properties";
	private String alias;

	private String server;

	private String portNumber;

	private String userId;

	private String password;
			
	private String sslConnection;
	
	private String sslTrustStoreLocation;
	
	private String sslTrustStorePassword;

	private Connection con = null;
	
	Properties dbprop = null;
	

	/**
	 * Default constructor.
	 * 
	 * @throws IOException
	 * 
	 */
	public DataUtil() {

		try {

			dbprop = new Properties();						
			ClassLoader loader = DataUtil.class.getClassLoader();
			if(loader == null)
			{				
				loader = ClassLoader.getSystemClassLoader();
				if(loader == null)
				{
					System.out.println("Error loading properties with getSystemClassLoader. NULL returned.");					
				}				
			}
			
			URL url = loader.getResource(this.ADW_PropFileName);											
			dbprop.load(url.openStream());		

			server = dbprop.getProperty("dbserver");
			portNumber = dbprop.getProperty("dbport");
			alias = dbprop.getProperty("dbalias");
			userId = dbprop.getProperty("dbuserid");
			password = dbprop.getProperty("dbpassword");			
			sslTrustStoreLocation = dbprop.getProperty("sslTrustStoreLocation");
			sslTrustStorePassword = dbprop.getProperty("sslTrustStorePassword");
			
			sslConnection = dbprop.getProperty("sslConnection");

			con = getConnection();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Connection getConnection() {
		String url = null;
		String nodeNumber = "0";

		Properties props = new Properties();
				
		try {
			
			url = "jdbc:db2://" + server + ":" + portNumber + "/" + alias;
			System.out.println("  Connect to '" + alias	+ "' database using JDBC type 4 driver.");
			Class.forName("com.ibm.db2.jcc.DB2Driver").newInstance();

			if (null != userId) {
				props.setProperty("user", userId);
				props.setProperty("password", password);
			}

			props.setProperty("CONNECTNODE", nodeNumber);
			props.setProperty("sslConnection", sslConnection);
			props.setProperty("sslTrustStoreLocation", sslTrustStoreLocation);
			props.setProperty("sslTrustStorePassword", sslTrustStorePassword);

			con = DriverManager.getConnection(url, props);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return con;
	}

	public void closeConnection() {
		try {
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<SolutionRVL> doSelect(Map<String, String> parameters) {

		SolutionRVL umc = new SolutionRVL();		
		List<SolutionRVL> listUmc = new ArrayList<SolutionRVL>();  		
		String query = dbprop.getProperty(parameters.get(Constant.MAP_QUERYNAME));
		
		try {
			PreparedStatement pstmt = con.prepareStatement(query);			
			pstmt.setString(1,parameters.get(Constant.MAP_ID));
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				umc = new SolutionRVL();
				umc.setid(rs.getInt(Constant.DB_COLUMN_ID));
				umc.setProductkey(rs.getString(Constant.DB_COLUMN_PRODUCTKEY));
				listUmc.add(umc);
				}
			pstmt.close();
		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}

		return listUmc;
	}
		
	public void doUpdate(Map<String, String> parameters) {
			
		String query = dbprop.getProperty(parameters.get(Constant.MAP_QUERYNAME));
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, parameters.get(Constant.MAP_PRODUCTKEY));
			pstmt.setString(2, parameters.get(Constant.MAP_ID));
			
			pstmt.executeUpdate();	
			pstmt.close();
	
		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace();
		}

	}
	
	public void doInsert(Map<String, String> parameters) {
		
		String query = dbprop.getProperty(parameters.get(Constant.MAP_QUERYNAME));
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, parameters.get(Constant.MAP_ID));
			pstmt.setString(2, parameters.get(Constant.MAP_PRODUCTKEY));
		
			pstmt.executeUpdate();
			pstmt.close();
			
		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
			ex.printStackTrace();
		}

	}

//Main method for testing post functional
	static public void main(String[] args) {
		String idPost = "22222";
		String productKeyPost = "TESTE6";
		
		Map<String, String> parameters = new HashMap<String, String>();
		
		try {
			System.out.println("begin test");
			DataUtil dbUtil = new DataUtil();
			
			parameters.put(Constant.MAP_QUERYNAME, Constant.SQL_GET_GETID);
			parameters.put(Constant.MAP_ID, idPost);
			parameters.put(Constant.MAP_PRODUCTKEY, productKeyPost);
			
			List<SolutionRVL> selectList = dbUtil.doSelect(parameters);
			
			
			for (SolutionRVL requestInfoModel : selectList) {
				System.out.println(requestInfoModel.getProductkey() + " " + requestInfoModel.getid());
			}		
			
			if(selectList.isEmpty()) {
				
				System.out.println("Empty List");
				parameters.put(Constant.MAP_QUERYNAME, Constant.SQL_INSERT_INSERT);
				dbUtil.doInsert(parameters);
				
			}else {
				System.out.println("Found ID!!");
				SolutionRVL solutionFound = selectList.get(0);
				parameters.put(Constant.MAP_QUERYNAME, Constant.SQL_UPDATE_UPDATES);
				parameters.put(Constant.MAP_PRODUCTKEY, productKeyPost);
				parameters.put(Constant.MAP_ID,String.valueOf(solutionFound.getid()));
				dbUtil.doUpdate(parameters);
			}
			
			dbUtil.closeConnection();
			

		} catch (Exception e) {
			e.printStackTrace();

		}
	}
}