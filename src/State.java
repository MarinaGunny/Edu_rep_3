import lombok.EqualsAndHashCode;
import java.util.HashMap;

//Тут храним состояние полей объекта
@EqualsAndHashCode
public class State {
    private HashMap<String, Object> param; //Пары имя параметра- значение

    State(HashMap<String, Object> param){
        this.param = param;
    }
    @Override
    public String toString() {
        return param.toString();
    }
}
