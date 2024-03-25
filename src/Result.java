public class Result {
    Long lifeTime;
    Object cachedValue;

    Result(Object cachedValue, Long lifeTime) {
        this.cachedValue = cachedValue;
        this.lifeTime = lifeTime;
    }
}
