import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class InvokeHandler implements InvocationHandler {
    private Object obj;
    private boolean isMutated;      //Значимые поля были изменены
    private Object retCachedObj;         // Возвращаемое кэшированное значение

    private State objState;

    private HashMap<State, HashMap<String, Object>> cachedObjects;   //Список сохраненных состояний объекта
    private HashMap<String, Object> cachedValues;   //Список сохраненных значений. Метод - значение

    InvokeHandler(Object obj) {
        this.obj = obj;
        this.isMutated = true;
        this.cachedObjects = new HashMap<>();
    }
    //TODO убрать то что ниже
    //Получаем условное состояние объекта на основе параметров
//    private String getState() throws IllegalAccessException {
//        String retStr = "";
//        for (Field f : obj.getClass().getDeclaredFields()) {
//            f.setAccessible(true);
//            retStr += "#" + f.getName() + "#" + f.get(obj).toString();
//        }
//        return retStr;
//    }
        private State getState() throws IllegalAccessException {

        HashMap<String, Object> list = new HashMap<>();

        for (Field f : obj.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            list.put(f.getName(), f.get(obj));
        }
        return new State(list);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //Входящий method не брать! Он от интерфейса и аннотаций там нет.
        Method myMethod = obj.getClass().getMethod(method.getName(), method.getParameterTypes());

        if (isMutated)  {objState = getState();
        System.out.println(objState);}

        //Проход по аннотациям. Отбираем те что нас интересуют, вдруг их там много разных
        for (Annotation a : Arrays.stream(myMethod.getDeclaredAnnotations()).filter(x -> x.annotationType().equals(Mutator.class) || x.annotationType().equals(Cache.class)).toList()) {
            if (a.annotationType() == Mutator.class) {
                isMutated = true;
                return method.invoke(obj, args);
            }
            if (a.annotationType() == Cache.class) {
                //TODO убрать. Пример доступа к параметру аннотации.
                //System.out.println("lifetime " + ((Cache) a).lifecycle());
                if (isMutated) {
                    isMutated = false;  //Сбрасываем признак
                    if (cachedObjects.containsKey(objState) && cachedObjects.get(objState).containsKey(method.getName())) {
                        return cachedObjects.get(objState).get(method.getName());
                    } else {
                        retCachedObj = method.invoke(obj, args);
                        //Нет этого состояния объекта вообще
                        if (!cachedObjects.containsKey(objState)) {
                            cachedValues = new HashMap<>();
                            cachedValues.put(method.getName(), retCachedObj);
                            cachedObjects.put(objState, cachedValues);
                        }
                        //Состояние это есть, но методы другие
                        else{
                            cachedValues = cachedObjects.get(objState);
                            cachedValues.put(method.getName(), retCachedObj);
                        }
                        return retCachedObj;
                    }
                } else
                    //return retCachedObj;  //Надо вернуть старое
                    return cachedObjects.get(objState).get(method.getName());
            }
        }
        return method.invoke(obj, args);
    }
}
