package productmanager.db;

import productmanager.Product;

import java.sql.*;
import java.util.Vector;

public class ProductRepository {
    public Connection conn = null;

    public ProductRepository() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/productmanager",
                    "root",
                    "1234");
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        System.out.println("DB 연결 성공");
    }

    public void add(Product product) {
        try {
            String sql = "" +
                    "INSERT INTO products (name, price, stock) "
                    + "VALUES (?, ?, ?)";
            // PreparedStatement
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, product.getName());
            pstmt.setInt(2, product.getPrice());
            pstmt.setInt(3, product.getStock());

            int rows = pstmt.executeUpdate();
            System.out.println("저장된 행 수: " + rows);

            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Vector<Product> getProducts() {
        Vector<Product> products = new Vector<>();
        try {
            String sql = "SELECT * FROM products";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while(rs.next()) {
                Product product = new Product();
                product.setNo(rs.getInt("no"));
                product.setName(rs.getString("name"));
                product.setPrice(rs.getInt("price"));
                product.setStock(rs.getInt("stock"));

                products.add(product);
            }

            rs.close();
            pstmt.close();


        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return products;
        }
    }

    public void updateDb(Product product) {
        try {
            String sql = "" +
                    "UPDATE products SET name=?, price=?, stock=? WHERE no=?";
            // PreparedStatement
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setString(1, product.getName());
            pstmt.setInt(2, product.getPrice());
            pstmt.setInt(3, product.getStock());
            pstmt.setInt(4, product.getNo());

            int rows = pstmt.executeUpdate();
            System.out.println("저장된 행 수: " + rows);

            pstmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteDb(Product product) {
        try {
            String sql = "" +
                    "DELETE FROM products WHERE no=?";

            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            pstmt.setInt(1, product.getNo());

            int rows = pstmt.executeUpdate();
            System.out.println("저장된 행 수: " + rows);

        } catch (SQLException e) {

        }

    }

}
