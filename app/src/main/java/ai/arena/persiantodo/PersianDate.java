package ai.arena.persiantodo;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class PersianDate {
    private static final int[] G_DAYS = {0,31,59,90,120,151,181,212,243,273,304,334};
    public int year, month, day, hour, minute;

    public PersianDate(int y, int m, int d, int h, int min) { year=y; month=m; day=d; hour=h; minute=min; }

    public static PersianDate now() { return fromMillis(System.currentTimeMillis()); }

    public static PersianDate fromMillis(long millis) {
        Calendar c = Calendar.getInstance(new Locale("fa", "IR"));
        c.setTimeInMillis(millis);
        int gy = c.get(Calendar.YEAR), gm = c.get(Calendar.MONTH) + 1, gd = c.get(Calendar.DAY_OF_MONTH);
        int[] j = gregorianToJalali(gy, gm, gd);
        return new PersianDate(j[0], j[1], j[2], c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    }

    public long toMillis() {
        int[] g = jalaliToGregorian(year, month, day);
        Calendar c = new GregorianCalendar(g[0], g[1] - 1, g[2], hour, minute, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public String format() {
        return digits(String.format(Locale.US, "%04d/%02d/%02d  %02d:%02d", year, month, day, hour, minute));
    }

    public static String digits(String s) {
        char[] fa = {'۰','۱','۲','۳','۴','۵','۶','۷','۸','۹'};
        StringBuilder b = new StringBuilder();
        for (char ch : s.toCharArray()) b.append(ch >= '0' && ch <= '9' ? fa[ch - '0'] : ch);
        return b.toString();
    }

    public static int[] gregorianToJalali(int gy, int gm, int gd) {
        int jy;
        if (gy > 1600) { jy = 979; gy -= 1600; } else { jy = 0; gy -= 621; }
        int gy2 = gm > 2 ? gy + 1 : gy;
        int days = 365 * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 - 80 + gd + G_DAYS[gm - 1];
        jy += 33 * (days / 12053); days %= 12053;
        jy += 4 * (days / 1461); days %= 1461;
        if (days > 365) { jy += (days - 1) / 365; days = (days - 1) % 365; }
        int jm = days < 186 ? 1 + days / 31 : 7 + (days - 186) / 30;
        int jd = 1 + (days < 186 ? days % 31 : (days - 186) % 30);
        return new int[]{jy, jm, jd};
    }

    public static int[] jalaliToGregorian(int jy, int jm, int jd) {
        int gy;
        if (jy > 979) { gy = 1600; jy -= 979; } else { gy = 621; }
        int days = 365 * jy + (jy / 33) * 8 + ((jy % 33) + 3) / 4 + 78 + jd + (jm < 7 ? (jm - 1) * 31 : ((jm - 7) * 30 + 186));
        gy += 400 * (days / 146097); days %= 146097;
        if (days > 36524) { gy += 100 * (--days / 36524); days %= 36524; if (days >= 365) days++; }
        gy += 4 * (days / 1461); days %= 1461;
        if (days > 365) { gy += (days - 1) / 365; days = (days - 1) % 365; }
        int gd = days + 1;
        int[] salA = {0,31, (gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0) ? 29 : 28,31,30,31,30,31,31,30,31,30,31};
        int gm;
        for (gm = 1; gm <= 12 && gd > salA[gm]; gm++) gd -= salA[gm];
        return new int[]{gy, gm, gd};
    }
}
