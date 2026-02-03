package my.utils;

import my.common.vo.MyApplicationVO;
import my.utils.annotation.Ref;
import org.springframework.stereotype.Component;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class RelationResolver {

    public Map<MyApplicationVO, MyApplicationVO> resolve(List<MyApplicationVO> entities) {
        Map<MyApplicationVO, Class<? extends MyApplicationVO>> refMap = buildRefMap(entities);
        return convertToInstanceMap(refMap, entities);
    }

    private Map<MyApplicationVO, Class<? extends MyApplicationVO>> buildRefMap(List<MyApplicationVO> entities) {
        Map<MyApplicationVO, Class<? extends MyApplicationVO>> refMap = new HashMap<>();

        for (MyApplicationVO entity : entities) {
            Class<? extends MyApplicationVO> clazz = entity.getClass();
            Field[] declaredFields = clazz.getDeclaredFields();

            for (Field declaredField : declaredFields) {
                Ref ref = declaredField.getAnnotation(Ref.class);
                if (ref != null) {
                    refMap.put(entity, ref.reference());
                }
            }
        }
        return refMap;
    }

    private Map<MyApplicationVO, MyApplicationVO> convertToInstanceMap(
            Map<MyApplicationVO, Class<? extends MyApplicationVO>> refMap,
            List<MyApplicationVO> entities) {

        Map<Class<? extends MyApplicationVO>, MyApplicationVO> classToInstance = new HashMap<>();
        for (MyApplicationVO entity : entities) {
            classToInstance.put(entity.getClass(), entity);
        }

        Map<MyApplicationVO, MyApplicationVO> resultMap = new HashMap<>();
        for (MyApplicationVO child : refMap.keySet()) {
            MyApplicationVO parent = classToInstance.get(refMap.get(child));
            resultMap.put(child, parent);
        }

        return resultMap;
    }






}
