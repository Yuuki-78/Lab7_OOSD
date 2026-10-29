package common;

/**
 * Interface này khai báo phương thức chung để hiển thị thông điệp
 * cho các giao diện người dùng (UI) như ClientConsole và ServerConsole.
 *
 * @author Student
 * @version 1.0
 */
public interface ChatIF {
    
    /**
     * Hiển thị một chuỗi thông điệp lên màn hình giao diện.
     *
     * @param message Thông điệp cần hiển thị.
     */
    public abstract void display(String message);
}