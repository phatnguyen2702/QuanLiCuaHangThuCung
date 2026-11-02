package UI;

/** Giao diện tra cứu phiếu kiểm kê kho. */
public class TraCuuKiemKeKhoUI extends TraCuuKhoPanel {
    // Khởi tạo đối tượng từ các giá trị được truyền vào.
    // Tham số quayLai: quay lại màn hình trước.
    public TraCuuKiemKeKhoUI(Runnable quayLai) {
        super(true, quayLai);
    }
}
