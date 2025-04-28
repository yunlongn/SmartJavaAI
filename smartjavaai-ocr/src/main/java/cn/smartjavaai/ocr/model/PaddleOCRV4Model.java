package cn.smartjavaai.ocr.model;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import cn.smartjavaai.ocr.translator.PaddleOCRV4DetectionTranslator;
import org.opencv.core.Mat;

import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author dwj
 * @date 2025/4/21
 */
public class PaddleOCRV4Model {


    public void loadModel(){

    }
}
