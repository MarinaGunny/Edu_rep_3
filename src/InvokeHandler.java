import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InvokeHandler implements InvocationHandler {
    private Object obj;
    private boolean isMutated;      //Значимые поля были изменены
    private Object retCachedObj;         // Возвращаемое кэшированное значение

    private State objState;

    private Map<State, Map<String, Result>> cachedObjects;   //Список сохраненных состояний объекта
    private Map<String, Result> cachedValues;   //Список сохраненных значений. Метод - значение

    InvokeHandler(Object obj) {
        this.obj = obj;
        this.isMutated = true;
        this.cachedObjects = new ConcurrentHashMap<>();
    }
    //Получить состояние объекта
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
        //Если объект был изменен, сохраняем состояние
        if (isMutated) {
            objState = getState();
        }

        Runnable Cleaner = ()->{
            for (Map<String, Result> mlist: cachedObjects.values()) {
                for (String m: mlist.keySet()) {
                    Result r = mlist.get(m);
                    if (r.lifeTime != 0 && r.lifeTime < System.currentTimeMillis()) {
                        isMutated = true;   //Обязательно! Иначе пойдет искать по кешу, который уже почистили и выдаст NullPointerException
                        mlist.remove(m);
                    }
                }
            }
        };

        //Просроченных больше или столько же чем непросроченных
        int rate=0;
        for (Map<String, Result> mlist: cachedObjects.values()) {
            for (Result r: mlist.values()) {
                if (r.lifeTime < System.currentTimeMillis()) ++rate; else --rate;
            }
        }
        if (rate >= 0)   new Thread(Cleaner).start();

        //Проход по аннотациям. Отбираем те что нас интересуют, вдруг их там много разных
        for (Annotation a : Arrays.stream(myMethod.getDeclaredAnnotations()).filter(x -> x.annotationType().equals(Mutator.class) || x.annotationType().equals(Cache.class)).toList()) {
            if (a.annotationType() == Mutator.class) {
                isMutated = true;
                return method.invoke(obj, args);
            }
            if (a.annotationType() == Cache.class) {
                Long lifeTime = (long) ((Cache) a).lifecycle();

                if (isMutated) {
                    isMutated = false;  //Сбрасываем признак
                    if (cachedObjects.containsKey(objState) && cachedObjects.get(objState).containsKey(method.getName())) {

                        Result res = cachedObjects.get(objState).get(method.getName());
                        //Если вообще нужно следить за временем
                        if (lifeTime != 0L) {
                                res.lifeTime += lifeTime;
                                cachedValues = new ConcurrentHashMap<>();
                                cachedValues.put(method.getName(), res);
                                cachedObjects.put(objState, cachedValues);
                                return res.cachedValue;
                            }
                        else //Если 0 - всегда берем из кеша, он вечный
                        {
                            return res.cachedValue;
                        }
                    } else {
                        retCachedObj = method.invoke(obj, args);
                        //Нет этого состояния объекта вообще
                        if (!cachedObjects.containsKey(objState)) {
                            cachedValues = new ConcurrentHashMap<>();
                            cachedValues.put(method.getName(), new Result(retCachedObj, lifeTime==0L?0L:System.currentTimeMillis()+lifeTime));
                            cachedObjects.put(objState, cachedValues);
                        }
                        //Состояние это есть, но методы другие (если несколько методов помечены как Cache и нашего нет)
                        else {
                            cachedValues = cachedObjects.get(objState);
                            cachedValues.put(method.getName(), new Result(retCachedObj, lifeTime==0L?0L:System.currentTimeMillis()+lifeTime));
                        }
                        return retCachedObj;
                    }
                } else
                    //Надо вернуть старое
                    return cachedObjects.get(objState).get(method.getName()).cachedValue;
            }
        }
        return method.invoke(obj, args);
    }
}
