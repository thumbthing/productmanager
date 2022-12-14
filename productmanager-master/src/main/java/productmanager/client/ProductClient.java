package productmanager.client;

import org.json.JSONArray;
import org.json.JSONObject;
import productmanager.Product;
import productmanager.RequestCode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ProductClient {
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Scanner scanner;

    public static void main(String[] args) {
        ProductClient productClient = new ProductClient();
        try {
            productClient.start();
        } catch (IOException e) {}
    }

    private void start() throws IOException {
        // 서버 연결 필요
        socket = new Socket("localhost", 50001);
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
        System.out.println("[클라이언트] 서버에 연결됨");

        scanner = new Scanner(System.in);

        list();
        receive();

        while(true) {
            showMenu();
        }
    }

    private void receive() {
        Thread thread = new Thread(()-> {
            while (true) {
                JSONObject response = null;
                try {
                    response = new JSONObject(dis.readUTF());
                    if(response.getString("status").equals("updated")) {
                        list();
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        thread.start();
    }

    private void showMenu() throws IOException {
        System.out.println();
        System.out.println("---------------------------------------------------------------");
        System.out.println("메뉴: 1.Create | 2.Update | 3.Delete | 4.Exit");
        System.out.print("선택: ");
        int menuNo = Integer.parseInt(scanner.nextLine());
        System.out.println();

        switch (menuNo) {
            case RequestCode.CREATE:
                create();
                break;
            case RequestCode.UPDATE:
                update();
                break;
            case RequestCode.DELETE:
                delete();
                break;
            case RequestCode.EXIT:
                exit();
                break;
        }
    }

    private void create() throws IOException {
        // 1. scanner로 입력받아서 상품(Product) 생성
        // 2. Json으로 변환해서 서버에 요청
        System.out.println("[상품 생성]");
        Product product = new Product();
        System.out.print("상품 이름: ");
        product.setName(scanner.nextLine());
        System.out.print("상품 가격: ");
        product.setPrice(Integer.parseInt(scanner.nextLine()));
        System.out.print("상품 재고: ");
        product.setStock(Integer.parseInt(scanner.nextLine()));

        JSONObject data = new JSONObject();
        data.put("name", product.getName());
        data.put("price", product.getPrice());
        data.put("stock", product.getStock());

        JSONObject request = new JSONObject();
        request.put("menu", RequestCode.CREATE);
        request.put("data", data);

        dos.writeUTF(request.toString());
        dos.flush();

    }
    private void update() throws IOException {
        // 콘솔에서 변경할 상품 정보 입력받기
        System.out.println("[상품 수정]");
        Product product = new Product();
        System.out.print("상품 번호: ");
        product.setNo(Integer.parseInt(scanner.nextLine()));
        System.out.print("이름 변경: ");
        product.setName(scanner.nextLine());
        System.out.print("가격 변경: ");
        product.setPrice(Integer.parseInt(scanner.nextLine()));
        System.out.print("재고 변경: ");
        product.setStock(Integer.parseInt(scanner.nextLine()));

        // 상품정보를 JSON으로 변환
        JSONObject data = new JSONObject();
        data.put("no", product.getNo());
        data.put("name", product.getName());
        data.put("price", product.getPrice());
        data.put("stock", product.getStock());
        JSONObject request = new JSONObject();
        request.put("menu", RequestCode.UPDATE);
        request.put("data", data);

        // JSON 파일을 직렬화해서 서버로 request
        dos.writeUTF(request.toString());
        dos.flush();
    }
    private void delete() throws IOException {
        // 삭제할 상품번호 입력받기
        System.out.println("[상품 삭제]");
        System.out.print("상품 번호: ");
        int no = Integer.parseInt(scanner.nextLine());

        JSONObject data = new JSONObject();
        data.put("no", no);

        // 상품 삭제 요청 (-> 서버)
        JSONObject request = new JSONObject();
        request.put("menu", RequestCode.DELETE);
        request.put("data", data);

        dos.writeUTF(request.toString());
        dos.flush();

    }
    private void exit() {
        stop();
    }

    private void stop() {
        try {
            socket.close();
            scanner.close();
        } catch (Exception e) {}
        System.out.println("[클라이언트] 종료됨");
    }

    // 클라이언트가 서버한테 상품목록좀 보여달라고 요청
    private void list() throws IOException {
        // 타이틀 및 컬럼명 출력
        System.out.println();
        System.out.println("[상품 목록]");
        System.out.println("---------------------------------------------------------------");
        System.out.printf("%-6s%-30s%-15s%-10s\n", "no", "name", "price", "stock");
        System.out.println("---------------------------------------------------------------");

        // 상품 목록 요청
        // JSON 만들어서 서버에 요청
        // GET 메소드와 같은 동작
        JSONObject request = new JSONObject();
        request.put("menu", RequestCode.READ);
//        request.put("data", new JSONObject());
        dos.writeUTF(request.toString());
        dos.flush();

        // response
        JSONObject response = new JSONObject(dis.readUTF());
        if(response.getString("status").equals("success")) {
            JSONArray data = response.getJSONArray("data");
            for(int i=0; i<data.length(); i++) {
                JSONObject product = data.getJSONObject(i);
                System.out.printf(
                        "%-6d%-30s%-15d%-10d\n",
                        product.getInt("no"),
                        product.getString("name"),
                        product.getInt("price"),
                        product.getInt("stock")
                );
            }
        }

    }
}
