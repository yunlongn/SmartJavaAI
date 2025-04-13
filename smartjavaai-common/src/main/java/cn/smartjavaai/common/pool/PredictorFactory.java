package cn.smartjavaai.common.pool;

import ai.djl.inference.Predictor;
import ai.djl.repository.zoo.ZooModel;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * Predictor 工厂类
 * @author dwj
 * @date 2025/4/8
 */
public class PredictorFactory<I, O> extends BasePooledObjectFactory<Predictor<I, O>> {
    private final ZooModel<I, O> model;

    public PredictorFactory(ZooModel<I, O> model) {
        this.model = model;
    }

    @Override
    public Predictor<I, O> create() {
        return model.newPredictor();
    }

    @Override
    public PooledObject<Predictor<I, O>> wrap(Predictor<I, O> predictor) {
        return new DefaultPooledObject<>(predictor);
    }

    @Override
    public void destroyObject(PooledObject<Predictor<I, O>> p) {
        p.getObject().close();
    }
}
