import java.text.SimpleDateFormat;

public class Result {
    Long lifeTime;  //До которого времени живет
    Object cachedValue;

    @Override
    public String toString() {
        return cachedValue.toString() + " " + lifeTime;
    }

    Result(Object cachedValue, Long lifeTime) {
        this.cachedValue = cachedValue;
        this.lifeTime = lifeTime;
    }
}
