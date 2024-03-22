import java.lang.reflect.Proxy;

public class Utils {
    public static <T> T cache(T myObj) {
        Class cls = myObj.getClass();

        return (T) Proxy.newProxyInstance(cls.getClassLoader(),
                                            cls.getInterfaces(),
                                            new InvokeHandler(myObj));
    }
}
