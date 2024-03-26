import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;

class MainTest {

    static ByteArrayOutputStream baos;
    static PrintStream ps;

    @BeforeAll
    static void preparing() {
        baos = new ByteArrayOutputStream();
        ps = new PrintStream(baos);
        System.setOut(ps);
    }

    @Test
    void testInvoke() {
        Fraction fr= new Fraction(2,3);
        Fractionable num = Utils.cache(fr);
        num.doubleValue();// sout сработал
        Assertions.assertEquals("invoke double value\r\n", baos.toString());
        baos.reset();
        num.doubleValue();// sout молчит
        num.doubleValue();// sout молчит
        Assertions.assertEquals("", baos.toString());
        num.setNum(5);
        num.doubleValue();// sout сработал
        Assertions.assertEquals("invoke double value\r\n", baos.toString());
        num.doubleValue();// sout молчит
        Assertions.assertEquals("invoke double value\r\n", baos.toString());
    }
    @Test
    void testGarbage() throws InterruptedException{
        Fraction fr= new Fraction(2,3);
        Fractionable num = Utils.cache(fr);
        num.doubleValue();// запись в кеш
        Thread.sleep(2000);             //Чтобы всё просрочилось
        num.doubleValue();                   //Тут мы это отловим и запустим дворника
        Thread.sleep(100);             //Чтобы дать время запуститься потоку
        baos.reset();
        num.doubleValue();
        Assertions.assertEquals("invoke double value\r\n", baos.toString());
    }

}