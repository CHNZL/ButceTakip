import java.util.Locale;
public class Test {
    public static void main(String[] args) {
        double d = 6055.010677;
        System.out.println("tr: " + String.format(Locale.forLanguageTag("tr"), "%,.2f", d));
    }
}
