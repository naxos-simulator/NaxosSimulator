/*
 * Copyright (c) 2015 Michal Markiewicz
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */ 

/**
 * 2009-01-26
 */
package de.tzi.gis;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import de.tzi.config.GlobalConfiguration;
import de.tzi.config.SettingsKeys;

/**
 * @author Michal Markiewicz
 *
 */
public class DAO {

	private static Logger logger = Logger.getLogger(GlobalConfiguration.class);
	
	private static DAO instance;
	
	public static DAO getInstance() {
		if (instance == null)
			instance = new DAO();
		return instance;
	}
	private Connection conn;
	
	private Connection getConnection() throws SQLException {
		if (conn == null || conn.isClosed()) {
		try {
			String driver = GlobalConfiguration.getInstance().getString(SettingsKeys.DB_DRIVER);
				Class.forName(driver);
			} catch (ClassNotFoundException e) {
				logger.error(e);
				throw new SQLException(e.getMessage());
			}
			String url = GlobalConfiguration.getInstance().getString(SettingsKeys.DB_URL);
			String user = GlobalConfiguration.getInstance().getDecodedString(SettingsKeys.DB_USER);
			String pass = GlobalConfiguration.getInstance().getDecodedString(SettingsKeys.DB_PASS);
//			logger.debug("Connecting to the database...");
			conn = DriverManager.getConnection(url, user, pass);
//			logger.debug("Connected to the database.");
		}
		return conn;
	}
	
	/**
	 * @see http://support.microsoft.com/kb/313100
	 */
	 public String debugInfo() {
		try {
			getConnection();
			DatabaseMetaData dm = conn.getMetaData();
			StringBuffer sb = new StringBuffer();
			sb.append("Driver Name: ").append(dm.getDriverName()).append('\n');
			sb.append("Driver Version: ").append(dm.getDriverVersion()).append('\n');
			sb.append("Database Name: ").append(dm.getDatabaseProductName()).append('\n');
			sb.append("Database Version: ").append(dm.getDatabaseProductVersion()).append('\n');
			closeConnection();
			return sb.toString();
		} catch (SQLException e) {
			logger.error("SQLException", e);
			return e.getMessage();
		}
	}     
    
	public void executeSQL(String sql, StringBuffer sb) throws SQLException {
		try {
			Statement stmt =  getConnection().createStatement();
			stmt.execute(sql);
			ResultSet rs = stmt.getResultSet();
			if (rs != null) {
				int cols = rs.getMetaData().getColumnCount();
				while (rs.next()) {
					for (int col = 1; col <= cols; col++) {
						sb.append(rs.getString(col)).append('\t');
					}
					sb.append('\n');
				}
			} else {
				sb.append("OK\n");
			}
		} finally {
			closeConnection();
		}
	}

    private void closeConnection() {
		try {
			if (conn != null)
				conn.close();
			conn = null;
		} catch (SQLException e) {
			logger.error("SQLException", e);
		}
	}
    
	/**
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException {
		DAO.getInstance().debugInfo();
	}

	public boolean checkConnection() {
		if (conn != null)
			return true;
		try {
			getConnection();
			closeConnection();
		} catch (SQLException e) {
			logger.error(e);
			return false;
		}
		return true;
	}
}
