package cn.smartjavaai.face.factory;

import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.ZooModel;
import cn.smartjavaai.face.model.SeetaFace6Model;
import com.seetaface.SeetaFace6JNI;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * Predictor 工厂类
 * @author dwj
 * @date 2025/4/8
 */
public class SeetaFace6Factory extends BasePooledObjectFactory<SeetaFace6JNI> {

    @Override
    public SeetaFace6JNI create() {
        return new SeetaFace6JNI();
    }

    @Override
    public PooledObject<SeetaFace6JNI> wrap(SeetaFace6JNI obj) {
        return new DefaultPooledObject<>(obj);
    }

    @Override
    public void destroyObject(PooledObject<SeetaFace6JNI> p) {
        //p.getObject().dispose(); // 如果需要释放 native 资源
        SeetaFace6JNI object = p.getObject();
        object = null;
    }
}
