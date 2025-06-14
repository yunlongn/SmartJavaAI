package cn.smartjavaai.common.pool;

import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.ZooModel;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * ZooModel 工厂类
 * @author lwx
 * @date 2025/6/06
 */
public class ZooModelFactory<I, O> extends BasePooledObjectFactory<ZooModel<I, O>> {
    private final ZooModel<I, O> model;

    public ZooModelFactory(ZooModel<I, O> model) {
        this.model = model;
    }

    @Override
    public ZooModel<I, O> create() {
        return model;
    }

    @Override
    public PooledObject<ZooModel<I, O>> wrap(ZooModel<I, O> predictor) {
        return new DefaultPooledObject<>(predictor);
    }

    @Override
    public void destroyObject(PooledObject<ZooModel<I, O>> p) {
        p.getObject().close();
    }
}
