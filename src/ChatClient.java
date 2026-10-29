import com.lloseng.ocsf.client.*;
import common.*;
import java.io.*;

/**
 * Lớp này xử lý kết nối phía Client, kế thừa từ AbstractClient của OCSF.
 * Nó chịu trách nhiệm gửi/nhận thông điệp và xử lý các lệnh điều khiển phía client.
 *
 * @author Student
 * @version 2.0
 */
public class ChatClient extends AbstractClient {
    
    /** Giao diện hiển thị phản hồi cho người dùng. */
    ChatIF clientUI;
    
    /** Định danh đăng nhập của người dùng. */
    String loginId;

    /**
     * Khởi tạo một đối tượng ChatClient mới và tự động mở kết nối, gửi loginID tới Server.
     *
     * @param loginId  Tên đăng nhập của người dùng.
     * @param host     Địa chỉ IP hoặc hostname của Server.
     * @param port     Cổng kết nối của Server.
     * @param clientUI Đối tượng UI dùng để tương tác với người dùng.
     * @throws IOException Nếu không thể tạo kết nối tới Server.
     */
    public ChatClient(String loginId, String host, int port, ChatIF clientUI) throws IOException {
        super(host, port);
        this.clientUI = clientUI;
        this.loginId = loginId;
        
        openConnection();
        sendToServer("#login " + loginId);
    }

    /**
     * Nhận và xử lý thông điệp gửi từ Server về Client.
     *
     * @param msg Thông điệp nhận được từ Server.
     */
    @Override
    public void handleMessageFromServer(Object msg) {
        clientUI.display(msg.toString());
    }

    /**
     * Xử lý dữ liệu do người dùng nhập từ bàn phím.
     * Nếu bắt đầu bằng dấu '#' thì coi là lệnh điều khiển, ngược lại gửi tới Server.
     *
     * @param message Thông điệp hoặc lệnh nhập từ giao diện UI.
     */
    public void handleMessageFromClientUI(String message) {
        if (message.startsWith("#")) {
            handleCmd(message);
        } else {
            try {
                sendToServer(message);
            } catch (IOException e) {
                clientUI.display("Could not send message to server. Terminating client.");
                quit();
            }
        }
    }

    /**
     * Phân tích và thực thi các lệnh điều khiển dòng lệnh của Client.
     *
     * @param command Chuỗi lệnh chứa dấu '#' ở đầu.
     */
    private void handleCmd(String command) {
        if (command.equalsIgnoreCase("#quit")) {
            quit();
        } else if (command.equalsIgnoreCase("#logoff")) {
            try {
                closeConnection();
            } catch (IOException e) {
                clientUI.display("Error logging off.");
            }
        } else if (command.startsWith("#login")) {
            if (isConnected()) {
                clientUI.display("Error: Already connected.");
            } else {
                try {
                    openConnection();
                    sendToServer("#login " + loginId);
                } catch (IOException e) {
                    clientUI.display("Cannot connect to server.");
                }
            }
        } else if (command.startsWith("#sethost ")) {
            if (isConnected()) {
                clientUI.display("Error: Cannot set host while connected.");
            } else {
                setHost(command.substring(9).trim());
                clientUI.display("Host set to: " + getHost());
            }
        } else if (command.startsWith("#setport ")) {
            if (isConnected()) {
                clientUI.display("Error: Cannot set port while connected.");
            } else {
                try {
                    setPort(Integer.parseInt(command.substring(9).trim()));
                    clientUI.display("Port set to: " + getPort());
                } catch (NumberFormatException e) {
                    clientUI.display("Invalid port number.");
                }
            }
        } else if (command.equalsIgnoreCase("#gethost")) {
            clientUI.display("Current host: " + getHost());
        } else if (command.equalsIgnoreCase("#getport")) {
            clientUI.display("Current port: " + getPort());
        } else {
            clientUI.display("Unknown command: " + command);
        }
    }

    /**
     * Ngắt kết nối và dừng chương trình Client.
     */
    public void quit() {
        try {
            closeConnection();
        } catch (IOException e) {}
        System.exit(0);
    }

    /**
     * Callback được gọi tự động khi kết nối bị đóng thành công.
     */
    @Override
    protected void connectionClosed() {
        clientUI.display("Connection closed.");
    }

    /**
     * Callback được gọi khi xảy ra ngoại lệ làm đứt kết nối với Server.
     *
     * @param exception Ngoại lệ xảy ra.
     */
    @Override
    protected void connectionException(Exception exception) {
        clientUI.display("The server has stopped responding.");
    }
}