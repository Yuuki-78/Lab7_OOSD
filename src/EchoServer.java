import com.lloseng.ocsf.server.*;
import common.*;
import java.io.*;

/**
 * Lớp xử lý phía Server, kế thừa từ AbstractServer của OCSF.
 * Lớp này quản lý danh sách Client, chuyển tiếp thông điệp và xử lý các lệnh phía Server.
 *
 * @author Student
 * @version 2.0
 */
public class EchoServer extends AbstractServer {
    
    /** Cổng kết nối mặc định của Server. */
    final public static int DEFAULT_PORT = 5555;
    
    /** Giao diện Console hiển thị nhật ký và thông báo của Server. */
    ChatIF serverUI;

    /**
     * Khởi tạo EchoServer trên một cổng chỉ định.
     *
     * @param port     Số cổng lắng nghe kết nối.
     * @param serverUI Giao diện hiển thị cho Server.
     */
    public EchoServer(int port, ChatIF serverUI) {
        super(port);
        this.serverUI = serverUI;
    }

    /**
     * Xử lý dữ liệu nhận được từ Client.
     * Kiểm tra trạng thái đăng nhập (#login) trước khi cho phép chat.
     *
     * @param msg    Thông điệp gửi từ Client.
     * @param client Đối tượng kết nối của Client tương ứng.
     */
    @Override
    public void handleMessageFromClient(Object msg, ConnectionToClient client) {
        String message = msg.toString();

        if (message.startsWith("#login ")) {
            if (client.getInfo("loginID") != null) {
                try {
                    client.sendToClient("Error: You are already logged in.");
                } catch (IOException e) {}
                return;
            }
            String loginId = message.substring(7).trim();
            client.setInfo("loginID", loginId);
            System.out.println("Message received: #login " + loginId + " from null.");
            System.out.println(loginId + " has logged on.");
            this.sendToAllClients(loginId + " has logged on.");
        } else {
            if (client.getInfo("loginID") == null) {
                try {
                    client.sendToClient("Error: You must login first.");
                    client.close();
                } catch (IOException e) {}
                return;
            }
            String loginId = (String) client.getInfo("loginID");
            System.out.println("Message received: " + message + " from " + loginId);
            this.sendToAllClients(loginId + ": " + message);
        }
    }

    /**
     * Xử lý tin nhắn hoặc lệnh do Server Admin nhập vào từ ServerConsole.
     *
     * @param message Tin nhắn hoặc lệnh từ giao diện Server.
     */
    public void handleMessageFromServerUI(String message) {
        if (message.startsWith("#")) {
            handleCmd(message);
        } else {
            serverUI.display("SERVER MESSAGE> " + message);
            sendToAllClients("SERVER MESSAGE> " + message);
        }
    }

    /**
     * Phân tích và thực thi các lệnh điều khiển từ phía Server (bắt đầu bằng dấu '#').
     *
     * @param command Chuỗi lệnh do quản trị viên nhập từ ServerConsole.
     */
    private void handleCmd(String command) {
        // Lệnh #quit: Đóng tất cả kết nối và thoát Server hoàn toàn
        if (command.equalsIgnoreCase("#quit")) {
            try {
                close();
            } catch (IOException e) {}
            System.exit(0);
        } 
        // Lệnh #stop: Tạm ngưng lắng nghe kết nối mới (giữ nguyên các Client đang online)
        else if (command.equalsIgnoreCase("#stop")) {
            stopListening();
        } 
        // Lệnh #start: Mở lại cổng lắng nghe kết nối từ Client
        else if (command.equalsIgnoreCase("#start")) {
            if (!isListening()) {
                try {
                    listen();
                } catch (IOException e) {
                    serverUI.display("Error listening for clients.");
                }
            } else {
                serverUI.display("Server is already listening.");
            }
        } 
        // Lệnh #close: Ngắt kết nối toàn bộ Client và ngưng lắng nghe
        else if (command.equalsIgnoreCase("#close")) {
            try {
                close();
            } catch (IOException e) {
                serverUI.display("Error closing connections.");
            }
        } 
        // Lệnh #setport: Cập nhật Port mới (chỉ cho phép khi Server đã đóng hẳn và không có Client)
        else if (command.startsWith("#setport ")) {
            if (isListening() || getNumberOfClients() > 0) {
                serverUI.display("Error: Server must be closed to set port.");
            } else {
                try {
                    setPort(Integer.parseInt(command.substring(9).trim()));
                    serverUI.display("Port set to: " + getPort());
                } catch (NumberFormatException e) {
                    serverUI.display("Invalid port.");
                }
            }
        } 
        // Lệnh #getport: Hiển thị Port hiện tại của Server
        else if (command.equalsIgnoreCase("#getport")) {
            serverUI.display("Current port: " + getPort());
        } 
        // Thông báo khi quản trị viên nhập sai cú pháp lệnh
        else {
            serverUI.display("Unknown command: " + command);
        }
    }
    /**
     * Hook method được gọi tự động sau khi Server bắt đầu lắng nghe kết nối.
     */
    @Override
    protected void serverStarted() {
        serverUI.display("Server listening for connections on port " + getPort());
    }

    /**
     * Hook method được gọi tự động khi Server ngừng lắng nghe kết nối mới.
     */
    @Override
    protected void serverStopped() {
        serverUI.display("Server has stopped listening for connections.");
    }

    /**
     * Hook method được gọi khi một Client kết nối thành công tới Server.
     *
     * @param client Đối tượng đại diện cho kết nối của Client.
     */
    @Override
    protected void clientConnected(ConnectionToClient client) {
        serverUI.display("A new client is attempting to connect to the server.");
    }

    /**
     * Hook method được gọi khi Client ngắt kết nối an toàn.
     *
     * @param client Client ngắt kết nối.
     */
    @Override
    synchronized protected void clientDisconnected(ConnectionToClient client) {
        String loginId = (String) client.getInfo("loginID");
        if (loginId != null) {
            serverUI.display("Client " + loginId + " disconnected.");
        }
    }

    /**
     * Hook method được gọi khi kết nối của Client bị gián đoạn do sự cố mạng.
     *
     * @param client    Client gặp lỗi.
     * @param exception Ngoại lệ làm đứt kết nối.
     */
    @Override
    synchronized protected void clientException(ConnectionToClient client, Throwable exception) {
        String loginId = (String) client.getInfo("loginID");
        if (loginId != null) {
            serverUI.display(loginId + " has disconnected.");
        }
    }
}