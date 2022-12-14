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
    // 필드를 작성하고 필드를 일단은 초기화
    private Socket socket;

    // input 에 넣어서 보낸다
    private DataInputStream dis;

    // output 에 담겨진 걸 읽는다
    private DataOutputStream dos;
    private Scanner scanner;


    public static void main(String[] args) {
        // 소켓 만들어준다
        try {
            ProductClient productClient = new ProductClient();
            productClient.start();
        } catch (Exception e) {}

    }

    private void start() throws IOException {
        // 서버 연결 192.168.10.248
        socket = new Socket("localhost",50003);
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
        System.out.println("클라이언트 서버에 연결됨");

        scanner = new Scanner(System.in);
        list();
        while (true) {
            showMenu();
        }
    }

    private void showMenu() throws IOException {
        System.out.println();
        System.out.println("-----------------------------------------------");
        System.out.println("메뉴 1: Create | 2: Update | 3: Delete | 4: Exit");
        System.out.print("선택: ");
        Integer menuNo = scanner.nextInt();
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
        //상품 생성
        // scanner로 입력 받음
        // JSON으로 변환해서 서버에 요청
        System.out.println("상품 생성");
        Product product = new Product();
        System.out.println("상품 이름");
        product.setName(scanner.next());
        System.out.println("상품 가격");
        product.setPrice(scanner.nextInt());
        System.out.println("상품 제고");
        product.setStock(scanner.nextInt());


        // json 파일을 생성하고 거기에 데이터를 넣는다
        JSONObject data = new JSONObject();
        data.put("name", product.getName());
        data.put("price", product.getPrice());
        data.put("stock", product.getStock());


        // json 파일을 하나 더 생성 해서
        // 위에 생성한 json 파일과 requestcode를 넣는다
        JSONObject request = new JSONObject();
        request.put("menu", RequestCode.CREATE);
        request.put("data", data);

        // 생성한 json 파일을 직력화하고 DataOutputStream에 넣고 flush
        dos.writeUTF(request.toString());
        dos.flush();

        // 서버로 부터 받은 직렬화된 json 파일을 JSONObject 형식으로 바꿔서 읽는다
        JSONObject response = new JSONObject(dis.readUTF());
        if(response.getString("status").equals("success")) {
            list();
        }
    }

    private void update() throws IOException {
        // Product 가 맵 형태이다
        System.out.println("상품 수정");
        Product product = new Product();
        System.out.println("상품 번호");
        product.setNo(scanner.nextInt());
        System.out.println("상품 이름");
        product.setName(scanner.next());
        System.out.println("상품 가격");
        product.setPrice(scanner.nextInt());
        System.out.println("상품 제고");
        product.setStock(scanner.nextInt());


        // data 라는 JSON 형식의 파일을 생성하고
        // product에 입력된 데이터를 키값과 함께 넣어준다
        JSONObject data = new JSONObject();
        data.put("no", product.getNo());
        data.put("name", product.getName());
        data.put("price", product.getPrice());
        data.put("stock", product.getStock());


        // 위에 생성한 JSON 파일과 RequestCode 를
        // 다시 JSON 형식으로 바꾸고
        JSONObject request = new JSONObject();
        request.put("menu", RequestCode.UPDATE);
        request.put("data", data);

        // DataOutPutStream에 직렬화한 JSON을 넣고 FLUSH
        dos.writeUTF(request.toString());
        dos.flush();
        // 응답 받은거 확인
        JSONObject response = new JSONObject(dis.readUTF());
        if(response.getString("status").equals("success")) {
            list();
        }
    }

    private void delete() throws IOException {
        //상품 삭제
        // 상품 번호로
        // 상품 번호 입력 받기
        // 상품 삭제 요청 (서버에게)
        System.out.println("상품 삭제");
        System.out.print("상품 번호: ");
        int no = scanner.nextInt();


        // 어떤 상품을 삭제할지 정해야 하기 때문에
        // 상품 번호를 입력하고 그걸 JSON 형식으로 만들어준다
        JSONObject data = new JSONObject();
        data.put("no", no);

        // 삭제 요청 (서버로)
        // 요청 코드와 상품 번호를 JSON에 넣어준다
        JSONObject request = new JSONObject();
        request.put("menu", RequestCode.DELETE);
        request.put("data", data);

        // 위에 만들어준 JSON 파일을 직렬화 해서
        // 서버로 보내주는데 그때
        // DataOutPutStream을 사용해서 보내주고 FLUSH
        dos.writeUTF(request.toString());
        dos.flush();
        // 응답 받은거 확인
        // 서버로 부터 받은 JSON 파일을
        // DataInputStream에서 꺼내서 읽어오고
        // 그걸 JSONObject 형식으로 변환해서
        // key 값으로 값을 불러와서 함수를 돌려준다
        JSONObject response = new JSONObject(dis.readUTF());
        if(response.getString("status").equals("success")) {
            list();
        }
    }
    private void exit() {
//        socket.close();
//        JSONObject data = new JSONObject();
//        data.put("Socket", socket);
//
//        JSONObject request = new JSONObject();
//        request.put("menu", RequestCode.EXIT);
//        request.put("data" ,data);
//
//        dos.writeUTF(request.toString());
//        dos.flush();
        stop();
    }

    private void stop() {
        try {
            // 서버 연결을 끊고 싶을때
            // 클라이언트 측에서 일단 소켓을 닫아주면 된다

            socket.close();
            scanner.close();
        } catch (Exception e) {
            System.out.println("[클라이언트] 종료됨");
        }
    }


    // 클라이언트가 서버에게
    // 상품 목록을 보여달라고 하는 메소드
    private void list() throws IOException {
        System.out.println("-----------------------------------------------");
        System.out.println("no  | name           | price        | stock    ");
        System.out.println("-----------------------------------------------");
        System.out.printf("%-6s%-30s%-15s%-10s\n", "no", "name", "price", "stock");


        //상품 목록 요청
        //JSON 만들어서 서버에 요청 하는 것
        // get 메소드 와 같은 동작
        //
        JSONObject request = new JSONObject();
        request.put("menu", RequestCode.READ);
        request.put("data", "");
        dos.writeUTF(request.toString());
        dos.flush();

        // 응답을 받아야함
        JSONObject response = new JSONObject(dis.readUTF());
        if(response.getString("status").equals("success")) {
            JSONArray data = response.getJSONArray("data");
            for (int i = 0; i < data.length(); i++) {
                JSONObject product = data.getJSONObject(i);
                System.out.printf("%-6s%-30s%-15s%-10s\n",
                        product.getInt("no"),
                        product.getString("name"),
                        product.getInt("price"),
                        product.getInt("stock"));
            }
        }
    }
}