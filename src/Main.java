public class Main {
    public static void main(String[] args) {
        Fraction fr= new Fraction(2,3);
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
        num.doubleValue();// sout молчит
        num.setNum(1);
        System.out.println("set 1");
        num.doubleValue();// sout сработал
    }
}
