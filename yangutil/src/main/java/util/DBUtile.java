package util;

import handler.VoidHandler;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hwyang on 2014/8/14.
 *
 * @author hwyang
 */
public class DBUtile {
    public static String URL = "10.6.6.123:3306";
    public static String dbName = "bw_hotel";
    public static String username = "tech_nlp";
    public static String password = "tech_nlpNVxzJN)~%C0M";
    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url = "jdbc:mysql://" + URL + "/" + dbName + "?useUnicode=true&characterEncoding=utf-8";
//            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
//            String url = "jdbc:sqlserver://" + URL + ";DatabaseName=" + dbName;
            Connection conn = DriverManager.getConnection(url, username, password);
            return conn;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

    }

    public static void main(String[] args) {
        Connection connection = getConnection();
        System.out.println("success ...");
    }
    public static List<Map<String, Object>> executeQuery(String sql) {
        System.out.println("excute sql : " + sql);
        try {
            Connection conn = DBUtile.getConnection();
            Statement st = conn.createStatement();
            ResultSet set = st.executeQuery(sql);
            ResultSetMetaData metaData = set.getMetaData();
            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            int columnCount = metaData.getColumnCount();
            while (set.next()) {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 1; i <= columnCount; i++) {
                    String name = metaData.getColumnName(i);
                    Object value = set.getObject(name);
                    map.put(name, value);
                }
                result.add(map);
            }
            set.close();
            st.close();
            conn.close();
            return result;
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        } finally {

        }
    }

    public static List<Map<String, Object>> executeQueryAutoByPage(String sql, int pageSize) {
        int total = executeCountQuery(sql);
        int pageCount = total / pageSize + 1;
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < pageCount; i++) {
            List<Map<String, Object>> maps = executeQueryByPage(sql, pageSize * i, pageSize);
            result.addAll(maps);
        }
        return result;
    }


    public static int executeCountQuery(String sql) {
        String countSql = String.format("select count(*) from (%s) t", sql);
        List<Map<String, Object>> maps = executeQuery(countSql);
        Object o = maps.get(0).get("count(*)");
        return ((Number) o).intValue();
    }

    public static List<Map<String, Object>> executeQueryByPage(String sql, int start, int size) {
        sql = sql + String.format(" limit %d,%d", start, size);
        System.out.println("excute sql : " + sql);
        try {
            Connection conn = DBUtile.getConnection();
            Statement st = conn.createStatement();
            ResultSet set = st.executeQuery(sql);
            ResultSetMetaData metaData = set.getMetaData();
            List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
            int columnCount = metaData.getColumnCount();
            while (set.next()) {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int i = 1; i <= columnCount; i++) {
                    String name = metaData.getColumnName(i);
                    Object value = set.getObject(name);
                    map.put(name, value);
                }
                result.add(map);
            }
            set.close();
            st.close();
            conn.close();
            return result;
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static long selectCount(String tableName){
        String sql = "select count(*) from " + tableName;
        List<Map<String, Object>> result = executeQuery(sql);
        Number number = (Number) result.get(0).get("count(*)");
        return number.longValue();
    }

    public static void readAll(String tableName, int offset, VoidHandler<Map<String, Object>> processor) throws Exception {
        long total = selectCount(tableName);
        int count = (int) (total / offset) + 1;
        for (int i = 0; i < count; i++) {
            String sql = "select * from " + tableName + " limit " + i * offset + "," + offset;
            List<Map<String, Object>> maps = executeQuery(sql);
            for (Map<String, Object> map : maps) {
                processor.doHandler(map);
            }
            System.out.println("read " + i + " : " + count);
        }
    }
}
