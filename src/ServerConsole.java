import java.io.*;
import common.*;

/**
 * Lớp giao diện dòng lệnh (Console) cho Server.
 * Chịu trách nhiệm khởi tạo và điều khiển EchoServer từ màn hình Console.
 *
 * @author Student
 * @version 2.0
 */
public class ServerConsole implements ChatIF {
    
    /** Cổng kết nối mặc định. */
    final public static int DEFAULT_PORT = 5555;
    
    /** Đối tượng xử lý logic Server. */
    EchoServer server;

    /**
     * Khởi tạo ServerConsole và lắng nghe kết nối trên port chỉ định.
     *
     * @param port Cổng kết nối mà Server sử dụng.
     */
    public ServerConsole(int port) {
        server = new EchoServer(port, this);
        try {
            server.listen();
        } catch (Exception e) {
            System.out.println("Error: Could not start listening for clients.");
        }
    }

    /**
     * In thông điệp ra màn hình Console của Server.
     *
     * @param message Thông điệp cần hiển thị.
     */
    @Override
    public void display(String message) {
        System.out.println("> " + message);
    }

    /**
     * Lắng nghe lệnh hoặc tin nhắn do Server Admin nhập vào từ bàn phím.
     */
    public void accept() {
        try {
            BufferedReader fromConsole = new BufferedReader(new InputStreamReader(System.in));
            String message;
            while (true) {
                message = fromConsole.readLine();
                server.handleMessageFromServerUI(message);
            }
        } catch (Exception ex) {
            System.out.println("Unexpected error while reading from console!");
        }
    }

    /**
     * Điểm bắt đầu thực thi chương trình phía Server.
     *
     * @param args Tham số truyền vào: [0] port (tùy chọn).
     */
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                port = DEFAULT_PORT;
            }
        }
        ServerConsole serverConsole = new ServerConsole(port);
        serverConsole.accept();
    }
}