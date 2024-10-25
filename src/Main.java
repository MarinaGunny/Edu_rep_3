import java.sql.Time;
import java.util.Date;

public class Main  {
    public static void main(String[] args) throws InterruptedException {
        Fraction fr= new Fraction(2,3);
        Date d1, d2;
        Fractionable num = Utils.cache(fr);
        num.doubleValue();// sout сработал
        num.doubleValue();// sout молчит
        num.doubleValue();// sout молчит
        num.setNum(5);
        System.out.println("set 5");
        num.doubleValue();// sout сработал
        num.doubleValue();// sout молчит
        num.setNum(2);
        System.out.println("set 2");
        num.doubleValue();// sout молчит
        Thread.sleep(2000);             //Чтобы всё просрочилось
        num.doubleValue();// sout молчит
        Thread.sleep(500);             //Чтобы дать время запуститься потоку
        num.doubleValue();// sout сработал
//        System.out.println(90%50);

    }
}
