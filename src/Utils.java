import java.lang.reflect.Proxy;

public class Utils {
    //F поставила вместо T, оказывается буква вообще не важна
    public static <F> F cache(F myObj) {
        Class cls = myObj.getClass();

        return (F) Proxy.newProxyInstance(cls.getClassLoader(),
                                            cls.getInterfaces(),
                                            new InvokeHandler(myObj));
    }
}
