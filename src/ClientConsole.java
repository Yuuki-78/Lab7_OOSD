import java.io.*;
import common.*;

/**
 * Lớp giao diện dòng lệnh (Console) cho Client.
 * Chịu trách nhiệm nhận tham số dòng lệnh và khởi tạo ChatClient.
 *
 * @author Student
 * @version 2.0
 */
public class ClientConsole implements ChatIF {
    
    /** Cổng mặc định nếu người dùng không truyền tham số port. */
    final public static int DEFAULT_PORT = 5555;
    
    /** Đối tượng quản lý kết nối Client. */
    ChatClient client;

    /**
     * Khởi tạo đối tượng ClientConsole và thiết lập kết nối tới Server.
     *
     * @param loginId Định danh người dùng.
     * @param host    Địa chỉ Server.
     * @param port    Cổng kết nối.
     */
    public ClientConsole(String loginId, String host, int port) {
        try {
            client = new ChatClient(loginId, host, port, this);
        } catch (IOException exception) {
            System.out.println("Error: Can't setup connection! Terminating client.");
            System.exit(1);
        }
    }

    /**
     * In thông điệp nhận được ra màn hình Console.
     *
     * @param message Thông điệp cần hiển thị.
     */
    @Override
    public void display(String message) {
        System.out.println("> " + message);
    }

    /**
     * Lắng nghe chuỗi nhập vào từ Console và chuyển cho ChatClient xử lý.
     */
    public void accept() {
        try {
            BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
            String message;
            while (true) {
                message = fromConsole.readLine();
                client.handleMessageFromClientUI(message);
            }
        } catch (Exception ex) {
            System.out.println("Unexpected error while reading from console!");
        }
    }

    /**
     * Điểm bắt đầu thực thi chương trình phía Client.
     *
     * @param args Tham số truyền vào: [0] loginID (bắt buộc), [1] host, [2] port.
     */
    public static void main(String[] args) {
        String loginId = "";
        String host = "localhost";
        int port = DEFAULT_PORT;

        if (args.length < 1) {
            System.out.println("ERROR - No login ID specified. Connection aborted.");
            System.exit(1);
        }
        loginId = args[0];

        if (args.length >= 2) {
            host = args[1];
        }
        if (args.length >= 3) {
            try {
                port = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                port = DEFAULT_PORT;
            }
        }

        ClientConsole chat = new ClientConsole(loginId, host, port);
        chat.accept();
    }
}