package Bus;

import DAO.*;
import DTO.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PhieuNhapBUS {

    private final PhieuNhapDAO phieuNhapDAO = new PhieuNhapDAO();
    private final ChiTietPhieuNhapDAO ctDAO = new ChiTietPhieuNhapDAO();

    public void taoPhieuNhap(List<chiTietPhieuNhap> dsChiTiet) {
        BigDecimal tongTien = BigDecimal.ZERO;

        for (chiTietPhieuNhap ct : dsChiTiet) {
            tongTien = tongTien.add(
                ct.getGiaNhap().multiply(BigDecimal.valueOf(ct.getSoLuong()))
            );
        }

        phieuNhap pn = new phieuNhap();
        pn.setNhaCungCapId(1);
        pn.setTongTien(tongTien);

        long phieuNhapId = phieuNhapDAO.insert(pn);

        for (chiTietPhieuNhap ct : dsChiTiet) {
            ct.setPhieuNhapId(phieuNhapId);
            ctDAO.insert(ct);
        }
    }
}
