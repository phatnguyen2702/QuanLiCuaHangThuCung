package control;

import dao.ThongKeDoanhThuDAO;
import dao.ThongKeDoanhThuDAO.BaoCao;
import dao.ThongKeDoanhThuDAO.CheDo;
import java.sql.SQLException;
import java.time.LocalDate;

public final class ThongKeDoanhThuControl {
    private final ThongKeDoanhThuDAO dao = new ThongKeDoanhThuDAO();

    public BaoCao layBaoCao(CheDo cheDo, LocalDate ngayThamChieu) throws SQLException {
        return dao.layBaoCao(cheDo, ngayThamChieu);
    }
}
