package productmanager.server;

import org.json.JSONArray;
import org.json.JSONObject;
import productmanager.Product;
import productmanager.RequestCode;
import productmanager.db.ProductRepository;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.sun.tools.attach.VirtualMachine.list;

public class ProductServer {
    // 서버에서 뭐가 필요할까 (소켓,
    private ServerSocket serverSocket;
    private ExecutorService threadPool = Executors.newFixedThreadPool(100);

    // 소켓 관리
    private ProductRepository db;

    private List<Product> products;
    // products는 db라고 생각해보자
    private int sequence;

    private Connection conn = null;

    public static void main(String[] args) {
        ProductServer productServer = new ProductServer();
        try {
            productServer.start();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            // ProductServer.stop();
        } catch (SQLException e) {
            throw new RuntimeException(e);
            // db 연결 해서 exception 연결 해줘야한다
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void start() throws IOException, SQLException, ClassNotFoundException {
        // 소켓 포트 바인딩 해줘야함
        // 새로운 연결을 대기하고 있다 (연결이 되면 새로운 소켓을 생성한다)
        // 클라이언트와 새로운 소켓의 통신이 시작된다

        // socket client 가 이 기능을 담당함
        serverSocket = new ServerSocket(50003);

        threadPool = Executors.newFixedThreadPool(100);
        // 락을 걸기 위해서 벡터를 쓴다
      //  products = new Vector<Product>();
        // products 에 db에 있는 걸 start할때 먼저 채워 넣는다



        // DB conn (연결을 해준다)
        db = new ProductRepository();
        products = db.getProducts();
        list();

      //  try {
        //    Class.forName("com.mysql.cj.jdbc.Driver");
          //  conn = DriverManager.getConnection(
            //        "jdbc:mysql://localhost:3306/productmanager",
              //      "root",
                //    "1234"
      //      );
        //    System.out.println("DB 연결 성공");
      //  } catch (ClassNotFoundException e) {
        //    throw new RuntimeException();
      //  } catch (SQLException e) {
        //    throw new SQLException();
       // }

   //     products.add(
   //             new Product(sequence++ , "삼다수", 1000, 20)
   //     );


        while (true) {
            // 소켓을 생성하고 활용해야함
            // 이제 accept 받은 리턴된 소켓을 통해 통신을 할 수 있다
            Socket socket = serverSocket.accept();
            SocketClient sc  = new SocketClient(socket);
        }
    }


    public class SocketClient {
        private Socket socket;
        // 해당 클라이언트로부터 요청을 받을 때 사용
        private DataInputStream dis;
        // 해당 클라이언트로 응답을 보낼 때 사용
        private DataOutputStream dos;

        public SocketClient(Socket socket) {

            try {
                this.socket = socket;
                // 데이터 받은 걸 읽을때 dis 를 보면 된다
                this.dis = new DataInputStream(socket.getInputStream());
                this.dos = new DataOutputStream(socket.getOutputStream());
                receive();
            } catch (IOException e) {
                throw new RuntimeException();
            }
        }
        // 서버가 사용하는 소켓을 어떻게 쓸 것인가
        // 데이터 요청 (clinet -> server)
        // 데이터를 읽고
        // 어떤 요청인지 확인
        // dis를 통해 데이터를 읽으면 된다
        public void receive() {
            //execute 안에 처리할 작업을 써준다
            threadPool.execute(()->{
                try {
                    while (true) {
                        String receiveJson = dis.readUTF();

                        //String -> Object 시켜서 사용
                        JSONObject request = new JSONObject(receiveJson);
                        int menu = request.getInt("menu");

                        switch (menu) {
                            case RequestCode.READ:
                                list();
                                break;
                            case RequestCode.DELETE:
                                delete(request);
                                break;
                            case RequestCode.UPDATE:
                                update(request);
                                break;
                            case RequestCode.CREATE:
                                create(request);
                                break;
                            case RequestCode.EXIT:
                                exit(request);
                                break;

                        }
                    }
                } catch (IOException e) {
                    close();
                } catch (SQLException e) {
                    // db 로 보내 줘야해서 추가된 exception
                    throw new RuntimeException(e);
                }
            });
        }

        private void close() {
            try {
                socket.close();
            }catch (Exception e) {
                System.out.println("[클라이언트] 접속 끊음");
            }
        }
        // list
        // 상품 목록을 클라이언트에게 보여준다
        // 서버야 너가 가지고 있는 목록 좀 보여줘
        public void list() throws IOException {

            JSONArray data = new JSONArray();
            // product 객체를 가져와서
            // JSON 타입으로 바꿔줘야함

            for(Product p : products) {
                JSONObject product = new JSONObject();
                product.put("no", p.getNo());
                product.put("name", p.getName());
                product.put("price", p.getPrice());
                product.put("stock", p.getStock());
                data.put(product);
            }
            JSONObject response = new JSONObject();
            response.put("status", "success");
            response.put("data", data);
            dos.writeUTF(response.toString());
            dos.flush();
        }
        // Create
        public void create(JSONObject request) throws IOException, SQLException {
            JSONObject data = request.getJSONObject("data");
            // 클라이언트에서 요청이 서버로 들어옴
            Product product = new Product();
          //  product.setNo(sequence++);
            product.setName(data.getString("name"));
            product.setPrice(data.getInt("price"));
            product.setStock(data.getInt("stock"));


            products.add(product);
            //DB에 넣어주기
            db.add(product);

            products = db.getProducts();
//
//            String sql = "" +
//                    "INSERT INTO products (no, name, price, stock)" +
//                    "VALUES (?, ?, ?)";
//
//
//            PreparedStatement pstmt = conn.prepareStatement(sql , Statement.RETURN_GENERATED_KEYS);
//
//            // pstmt.setInt(1, product.getNo());
//            pstmt.setString(1, product.getName());
//            pstmt.setInt(2, product.getPrice());
//            pstmt.setInt(3, product.getStock());
//
//            int rows = pstmt.executeUpdate();
//            pstmt.close();


            //response 보내기
            // 1. JSON 만들기
            JSONObject response = new JSONObject();
            response.put("status", "success");
            response.put("data", "");
            // 2. 직렬화 하기
            dos.writeUTF(response.toString());
            dos.flush();


        }

        // 여기 해야함
        // Update
        public void update(JSONObject request) throws IOException {
            // 데이터를 읽고
            // 여기서는 requst 는 한번 펼쳐 졌다
            JSONObject data = request.getJSONObject("data");
            Product product = new Product();

            product.setName(data.getString("name"));
            product.setPrice(data.getInt("price"));
            product.setStock(data.getInt("stock"));
            product.setNo(data.getInt("no"));

            db.updateDb(product);

            products = db.getProducts();

//            int no = data.getInt("no");

//            Iterator<Product> iterator = products.iterator();
//            while(iterator.hasNext()) {
//                Product product = iterator.next();
//                if (product.getNo() == no) {
//                    product.setName(data.getString("name"));
//                    product.setPrice(data.getInt("price"));
//                    product.setStock(data.getInt("stock"));
//                }
//            }
            // reponse
            JSONObject response = new JSONObject();
            response.put("status", "success");
            response.put("data", new JSONObject());
            dos.writeUTF(response.toString());
            dos.flush();

        }
        // Delete
        public  void delete(JSONObject request) throws IOException {
            // 데이터를 읽고
            // 여기서는 requst 는 한번 펼쳐 졌다
            JSONObject data = request.getJSONObject("data");
            Product product = new Product();

//            int no = data.getInt("no");
//            // 해당 no 가 products에 있는 지 확인 후 삭제
//            Iterator<Product> iterator = products.iterator();
//            while(iterator.hasNext()) {
//                Product product = iterator.next();
//                if (product.getNo() == no) {
//                    iterator.remove();
//                }
//            }
            product.setNo(data.getInt("no"));

            db.deleteDb(product);

            products = db.getProducts();

            // reponse
            JSONObject response = new JSONObject();
            response.put("status", "success");
            response.put("data", new JSONObject());
            dos.writeUTF(response.toString());
            dos.flush();
        }

        public void exit(JSONObject request) throws IOException {
            JSONObject data = request.getJSONObject("data");

            System.out.println("user out");



        }
    }
}