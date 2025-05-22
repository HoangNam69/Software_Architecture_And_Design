package iuh.fit.reportservice.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class CurrencyFormatter {

    /**
     * Định dạng số double thành chuỗi tiền tệ VNĐ
     * @param amount số tiền cần định dạng (kiểu double)
     * @return chuỗi đã định dạng theo VNĐ
     */
    public static String formatDoubleToVND(double amount) {
        // Chuyển đổi double sang BigDecimal để tránh mất chính xác
        BigDecimal bigDecimal = BigDecimal.valueOf(amount);

        // Kiểm tra nếu số có phần thập phân .0 thì bỏ phần thập phân
        if (bigDecimal.scale() <= 0 || bigDecimal.stripTrailingZeros().scale() <= 0) {
            return formatVND(bigDecimal.setScale(0));
        }
        return formatVND(bigDecimal);
    }

    /**
     * Định dạng BigDecimal thành chuỗi VNĐ
     */
    private static String formatVND(BigDecimal amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        DecimalFormat formatter = new DecimalFormat();
        formatter.setDecimalFormatSymbols(symbols);

        // Nếu là số nguyên thì không hiển thị phần thập phân
        if (amount.scale() <= 0 || amount.stripTrailingZeros().scale() <= 0) {
            formatter.setMaximumFractionDigits(0);
            formatter.setMinimumFractionDigits(0);
        } else {
            formatter.setMaximumFractionDigits(2);
            formatter.setMinimumFractionDigits(2);
        }

        formatter.setGroupingSize(3);
        formatter.setGroupingUsed(true);

        return formatter.format(amount) + " VNĐ";
    }

    /**
     * Phương thức main để test
     */
//    public static void main(String[] args) {
//        double testNumber1 = 12690340234.0;
//        double testNumber2 = 12690340234.5;
//        double testNumber3 = 123456789.123456;
//
//        System.out.println(formatDoubleToVND(testNumber1)); // 12.690.340.234 VNĐ
//        System.out.println(formatDoubleToVND(testNumber2)); // 12.690.340.234,5 VNĐ
//        System.out.println(formatDoubleToVND(testNumber3)); // 123.456.789,12 VNĐ
//    }
}
