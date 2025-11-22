package com.example.qing.utils;

import ai.onnxruntime.*;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

public class YoloONNXUtil {
    // 模型文件路径（请确保文件在classpath下）
    private static final String MODEL_ONNX = "yolov/test.onnx";
    private static final String MODEL_CLASSES = "yolov/test.names";
    private static double CONFIDENCE_THRESHOLD = 0.5; // 调高阈值过滤误检
    private static final double NMS_THRESHOLD = 0.4;
    private static final int INPUT_WIDTH = 640;
    private static final int INPUT_HEIGHT = 640;

    private static OrtEnvironment ortEnv;
    private static OrtSession ortSession;
    private static List<String> classes;
    private static int numClasses;

    static {
        try {
            // 加载 OpenCV
            nu.pattern.OpenCV.loadLocally();

            // 初始化ONNX Runtime环境
            ortEnv = OrtEnvironment.getEnvironment();
            ortSession = loadModel();
            classes = loadClasses();
            numClasses = classes.size();

            System.out.println("✅ YOLO模型(ONNX Runtime)初始化成功");
            System.out.println("📋 类别数量: " + numClasses);
            System.out.println("📋 类别列表: " + classes);
            System.out.println("📋 置信度阈值: " + CONFIDENCE_THRESHOLD);

        } catch (Exception e) {
            System.err.println("❌ YOLO模型初始化失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("YOLO模型初始化失败", e);
        }
    }

    // 模型加载方法
    private static OrtSession loadModel() {
        try {
            System.out.println("🔧 开始加载模型: " + MODEL_ONNX);

            InputStream onnxStream = YoloONNXUtil.class.getClassLoader().getResourceAsStream(MODEL_ONNX);
            if (onnxStream == null) {
                throw new RuntimeException("模型文件未找到: " + MODEL_ONNX + "（请检查classpath路径）");
            }

            File onnxFile = File.createTempFile("yolov8", ".onnx");
            Files.copy(onnxStream, onnxFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            OrtSession.SessionOptions sessionOptions = new OrtSession.SessionOptions();
            sessionOptions.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);
            sessionOptions.setInterOpNumThreads(Runtime.getRuntime().availableProcessors() / 2);
            sessionOptions.setIntraOpNumThreads(Runtime.getRuntime().availableProcessors());

            OrtSession session = ortEnv.createSession(onnxFile.getAbsolutePath(), sessionOptions);

            onnxFile.deleteOnExit();
            System.out.println("✅ 模型加载成功");

            printModelInfo(session);
            return session;
        } catch (Exception e) {
            throw new RuntimeException("模型加载失败: " + e.getMessage(), e);
        }
    }

    // 打印模型信息
    private static void printModelInfo(OrtSession session) throws OrtException {
        System.out.println("=== 模型信息 ===");

        // 输入信息
        Map<String, NodeInfo> inputInfo = session.getInputInfo();
        for (Map.Entry<String, NodeInfo> entry : inputInfo.entrySet()) {
            NodeInfo nodeInfo = entry.getValue();
            TensorInfo tensorInfo = (TensorInfo) nodeInfo.getInfo();
            System.out.println("输入名称: " + entry.getKey());
            System.out.println("  形状: " + Arrays.toString(tensorInfo.getShape()));
            System.out.println("  类型: " + tensorInfo.type);
        }

        // 输出信息
        Map<String, NodeInfo> outputInfo = session.getOutputInfo();
        for (Map.Entry<String, NodeInfo> entry : outputInfo.entrySet()) {
            NodeInfo nodeInfo = entry.getValue();
            TensorInfo tensorInfo = (TensorInfo) nodeInfo.getInfo();
            System.out.println("输出名称: " + entry.getKey());
            System.out.println("  形状: " + Arrays.toString(tensorInfo.getShape()));
            System.out.println("  类型: " + tensorInfo.type);
        }
        System.out.println("================");
    }

    // 图片检测方法（对外接口）
    public static DetectionResult detect(byte[] imageBytes) {
        try {
            System.out.println("🔍 开始图片检测，图片大小: " + imageBytes.length + " bytes");

            // 1. 使用OpenCV读取图片
            Mat originalImage = Imgcodecs.imdecode(new MatOfByte(imageBytes), Imgcodecs.IMREAD_COLOR);
            if (originalImage.empty()) {
                throw new RuntimeException("图片解码失败（请检查图片格式是否正确）");
            }
            int originalWidth = originalImage.cols();
            int originalHeight = originalImage.rows();
            System.out.println("📷 图片解码成功，尺寸: " + originalWidth + "x" + originalHeight);

            // 2. 预处理图片（letterbox填充，保持长宽比）
            long preprocessStart = System.currentTimeMillis();
            float[] inputData = preprocessImage(originalImage);
            long preprocessEnd = System.currentTimeMillis();
            System.out.println("🔄 图片预处理完成，耗时: " + (preprocessEnd - preprocessStart) + "ms");

            // 3. 模型推理
            long inferenceStart = System.currentTimeMillis();
            Object output = runInference(inputData);
            long inferenceEnd = System.currentTimeMillis();
            System.out.println("🤖 模型推理完成，耗时: " + (inferenceEnd - inferenceStart) + "ms");

            // 4. 解析YOLOv8输出（修正坐标偏移）
            List<Detection> detections = parseYOLOv8Output(output, originalWidth, originalHeight);
            System.out.println("🎯 检测到对象数量: " + detections.size());

            // 5. 应用非极大值抑制
            applyNMS(detections);
            System.out.println("🎯 NMS后剩余对象: " + detections.size());

            // 6. 绘制检测结果（使用Java2D绘制中文）
            BufferedImage detectedImage = drawDetectionsWithJava2D(originalImage, detections);

            // 7. 转换为字节数组
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(detectedImage, "jpg", baos);
            byte[] detectedImageBytes = baos.toByteArray();

            // 8. 分析检测结果
            DetectionResult result = new DetectionResult();
            result.setDetectedImageBytes(detectedImageBytes);
            analyzeDetectionResult(result, detections);

            System.out.println("✅ 检测完成，结果: " + result.getDetectionResult());

            return result;

        } catch (Exception e) {
            System.err.println("❌ 目标检测失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("目标检测失败: " + e.getMessage(), e);
        }
    }

    // 图片预处理（修复：letterbox填充，保持长宽比，避免拉伸）
    private static float[] preprocessImage(Mat image) {
        int originalWidth = image.cols();
        int originalHeight = image.rows();

        // 计算缩放比例（取最小比例，确保图片完全包含在640x640内）
        float scale = Math.min((float) INPUT_WIDTH / originalWidth, (float) INPUT_HEIGHT / originalHeight);
        // 计算缩放后的尺寸（保持长宽比）
        int newWidth = Math.round(originalWidth * scale);
        int newHeight = Math.round(originalHeight * scale);

        // 缩放图片
        Mat resizedImage = new Mat();
        Imgproc.resize(image, resizedImage, new Size(newWidth, newHeight), 0, 0, Imgproc.INTER_AREA);

        // 创建640x640的空白画布（填充黑边）
        Mat paddedImage = new Mat(INPUT_HEIGHT, INPUT_WIDTH, CvType.CV_8UC3, new Scalar(0, 0, 0));
        // 计算填充位置（居中对齐）
        int xOffset = (INPUT_WIDTH - newWidth) / 2;
        int yOffset = (INPUT_HEIGHT - newHeight) / 2;
        // 将缩放后的图片复制到画布中央
        resizedImage.copyTo(paddedImage.submat(yOffset, yOffset + newHeight, xOffset, xOffset + newWidth));

        // 转换为RGB格式
        Mat rgbImage = new Mat();
        Imgproc.cvtColor(paddedImage, rgbImage, Imgproc.COLOR_BGR2RGB);

        // 归一化到0-1，转换为float类型
        Mat floatImage = new Mat();
        rgbImage.convertTo(floatImage, CvType.CV_32FC3, 1.0 / 255.0);

        // 提取像素数据（CHW格式：3x640x640）
        float[] data = new float[3 * INPUT_WIDTH * INPUT_HEIGHT];
        int pixelIndex = 0;

        for (int c = 0; c < 3; c++) { // 通道优先（R->G->B）
            for (int h = 0; h < INPUT_HEIGHT; h++) {
                for (int w = 0; w < INPUT_WIDTH; w++) {
                    double[] pixel = floatImage.get(h, w);
                    data[pixelIndex++] = (float) pixel[c];
                }
            }
        }

        return data;
    }

    // 模型推理
    private static Object runInference(float[] inputData) throws OrtException {
        // 创建输入张量（形状：1x3x640x640）
        long[] inputShape = new long[]{1, 3, INPUT_HEIGHT, INPUT_WIDTH};
        OnnxTensor inputTensor = OnnxTensor.createTensor(ortEnv, FloatBuffer.wrap(inputData), inputShape);

        // 运行推理
        Map<String, OnnxTensor> inputMap = Collections.singletonMap("images", inputTensor);
        OrtSession.Result outputResult = ortSession.run(inputMap);

        // 获取第一个输出（YOLOv8通常只有一个输出张量）
        OnnxTensor outputTensor = (OnnxTensor) outputResult.get(0);

        // 打印输出形状（方便调试）
        long[] outputShape = outputTensor.getInfo().getShape();
        System.out.println("📊 模型输出形状: " + Arrays.toString(outputShape));

        // 获取输出数据
        Object outputValue = outputTensor.getValue();

        // 释放资源
        inputTensor.close();
        outputResult.close();

        return outputValue;
    }

    // 解析YOLOv8输出（修复：动态适配类别数，修正坐标偏移）
    private static List<Detection> parseYOLOv8Output(Object output, int originalWidth, int originalHeight) {
        List<Detection> detections = new ArrayList<>();
        if (output == null) {
            return detections;
        }

        // 计算letterbox相关参数（用于修正检测框坐标）
        float scale = Math.min((float) INPUT_WIDTH / originalWidth, (float) INPUT_HEIGHT / originalHeight);
        int xOffset = (INPUT_WIDTH - Math.round(originalWidth * scale)) / 2;
        int yOffset = (INPUT_HEIGHT - Math.round(originalHeight * scale)) / 2;

        try {
            if (output instanceof float[][][]) {
                // 输出格式：[1, 4+numClasses, N]（YOLOv8导出时指定format=raw的格式）
                float[][][] outputArray = (float[][][]) output;
                int totalDimensions = outputArray[0].length;
                int numDetections = outputArray[0][0].length;

                // 校验输出维度是否匹配（4个坐标 + N个类别）
                if (totalDimensions != 4 + numClasses) {
                    throw new RuntimeException("模型输出维度与类别数不匹配！" +
                            "输出总维度：" + totalDimensions + "，预期维度：4 + " + numClasses +
                            "（请检查模型和" + MODEL_CLASSES + "文件是否匹配）");
                }

                System.out.println("🔍 解析3D输出格式: [1, " + totalDimensions + ", " + numDetections + "]");
                detections = parse3DOutput(outputArray, scale, xOffset, yOffset, originalWidth, originalHeight);

            } else if (output instanceof float[][]) {
                // 输出格式：[8400, 4+numClasses]（YOLOv8默认导出格式）
                float[][] outputArray = (float[][]) output;
                int totalDimensions = outputArray[0].length;

                // 校验输出维度
                if (totalDimensions != 4 + numClasses) {
                    throw new RuntimeException("模型输出维度与类别数不匹配！" +
                            "输出总维度：" + totalDimensions + "，预期维度：4 + " + numClasses +
                            "（请检查模型和" + MODEL_CLASSES + "文件是否匹配）");
                }

                System.out.println("🔍 解析2D输出格式: [" + outputArray.length + ", " + totalDimensions + "]");
                detections = parse2DOutput(outputArray, scale, xOffset, yOffset, originalWidth, originalHeight);

            } else {
                throw new RuntimeException("未知的模型输出格式: " + output.getClass().getName() +
                        "（仅支持float[][][]或float[][]格式）");
            }

        } catch (Exception e) {
            System.err.println("❌ 解析模型输出失败: " + e.getMessage());
            e.printStackTrace();
        }

        return detections;
    }

    // 解析3D输出格式 [1, 4+numClasses, N]
    private static List<Detection> parse3DOutput(float[][][] outputArray, float scale, int xOffset, int yOffset,
                                                 int originalWidth, int originalHeight) {
        List<Detection> detections = new ArrayList<>();
        int numDetections = outputArray[0][0].length;
        int validCount = 0;

        // 打印前3个检测框的原始数据（调试用）
        System.out.println("🔍 前3个检测框原始数据（cx, cy, w, h, 类别概率...）:");
        for (int i = 0; i < Math.min(3, numDetections); i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < Math.min(8, 4 + numClasses); j++) { // 最多打印8个维度
                sb.append(String.format("%.3f ", outputArray[0][j][i]));
            }
            System.out.println("  检测框" + i + ": " + sb);
        }

        for (int i = 0; i < numDetections; i++) {
            // 解析中心坐标、宽高（模型输出的是letterbox填充后的坐标）
            float cx = outputArray[0][0][i];
            float cy = outputArray[0][1][i];
            float w = outputArray[0][2][i];
            float h = outputArray[0][3][i];

            // 解析类别概率（动态适配numClasses，不再硬编码）
            float maxScore = 0;
            int classId = -1;
            for (int j = 4; j < 4 + numClasses; j++) {
                float score = outputArray[0][j][i];
                if (score > maxScore) {
                    maxScore = score;
                    classId = j - 4;
                }
            }

            // 过滤低置信度结果
            if (maxScore > CONFIDENCE_THRESHOLD && classId >= 0 && classId < numClasses) {
                // 修正坐标：减去letterbox偏移，再缩放回原始图片尺寸
                int left = Math.round((cx - w / 2 - xOffset) / scale);
                int top = Math.round((cy - h / 2 - yOffset) / scale);
                int width = Math.round(w / scale);
                int height = Math.round(h / scale);

                // 边界检查（避免超出图片范围）
                left = Math.max(0, left);
                top = Math.max(0, top);
                width = Math.min(originalWidth - left, width);
                height = Math.min(originalHeight - top, height);

                // 过滤极小检测框（避免噪点）
                if (width >= 10 && height >= 10) {
                    detections.add(new Detection(classId, maxScore, new BoundingBox(left, top, width, height)));
                    validCount++;

                    // 打印前5个有效检测结果
                    if (validCount <= 5) {
                        String className = classes.get(classId);
                        System.out.println("✅ 有效检测" + validCount + ": " + className +
                                " | 置信度: " + String.format("%.4f", maxScore) +
                                " | 坐标: (" + left + "," + top + "," + width + "," + height + ")");
                    }
                }
            }
        }

        System.out.println("🎯 3D输出解析完成: " + validCount + "个有效检测框");
        return detections;
    }

    // 解析2D输出格式 [8400, 4+numClasses]
    private static List<Detection> parse2DOutput(float[][] outputArray, float scale, int xOffset, int yOffset,
                                                 int originalWidth, int originalHeight) {
        List<Detection> detections = new ArrayList<>();
        int numDetections = outputArray.length;
        int validCount = 0;

        for (int i = 0; i < numDetections; i++) {
            float[] detection = outputArray[i];

            // 解析中心坐标、宽高
            float cx = detection[0];
            float cy = detection[1];
            float w = detection[2];
            float h = detection[3];

            // 解析类别概率（动态适配numClasses）
            float maxScore = 0;
            int classId = -1;
            for (int j = 4; j < 4 + numClasses; j++) {
                float score = detection[j];
                if (score > maxScore) {
                    maxScore = score;
                    classId = j - 4;
                }
            }

            // 过滤低置信度结果
            if (maxScore > CONFIDENCE_THRESHOLD && classId >= 0 && classId < numClasses) {
                // 修正坐标：减去letterbox偏移，缩放回原始尺寸
                int left = Math.round((cx - w / 2 - xOffset) / scale);
                int top = Math.round((cy - h / 2 - yOffset) / scale);
                int width = Math.round(w / scale);
                int height = Math.round(h / scale);

                // 边界检查
                left = Math.max(0, left);
                top = Math.max(0, top);
                width = Math.min(originalWidth - left, width);
                height = Math.min(originalHeight - top, height);

                // 过滤极小检测框
                if (width >= 10 && height >= 10) {
                    detections.add(new Detection(classId, maxScore, new BoundingBox(left, top, width, height)));
                    validCount++;
                }
            }
        }

        System.out.println("🎯 2D输出解析完成: " + validCount + "个有效检测框");
        return detections;
    }

    // 应用非极大值抑制（NMS）- 去除重叠检测框
    private static void applyNMS(List<Detection> detections) {
        if (detections.isEmpty()) return;

        // 按置信度降序排序
        detections.sort((a, b) -> Float.compare(b.confidence, a.confidence));

        List<Detection> filteredDetections = new ArrayList<>();
        boolean[] suppressed = new boolean[detections.size()];

        for (int i = 0; i < detections.size(); i++) {
            if (suppressed[i]) continue;

            Detection current = detections.get(i);
            filteredDetections.add(current);

            // 抑制与当前框重叠度过高的框
            for (int j = i + 1; j < detections.size(); j++) {
                if (suppressed[j]) continue;

                Detection other = detections.get(j);
                float iou = calculateIoU(current.bbox, other.bbox);
                if (iou > NMS_THRESHOLD) {
                    suppressed[j] = true;
                }
            }
        }

        detections.clear();
        detections.addAll(filteredDetections);
    }

    // 计算交并比（IoU）
    private static float calculateIoU(BoundingBox box1, BoundingBox box2) {
        int intersectLeft = Math.max(box1.x, box2.x);
        int intersectTop = Math.max(box1.y, box2.y);
        int intersectRight = Math.min(box1.x + box1.width, box2.x + box2.width);
        int intersectBottom = Math.min(box1.y + box1.height, box2.y + box2.height);

        // 计算交集面积
        int intersectArea = Math.max(0, intersectRight - intersectLeft) * Math.max(0, intersectBottom - intersectTop);
        if (intersectArea == 0) return 0;

        // 计算并集面积
        int box1Area = box1.width * box1.height;
        int box2Area = box2.width * box2.height;
        int unionArea = box1Area + box2Area - intersectArea;

        return (float) intersectArea / unionArea;
    }

    // 绘制检测结果（Java2D绘制中文，避免乱码）
    private static BufferedImage drawDetectionsWithJava2D(Mat image, List<Detection> detections) {
        try {
            // 将OpenCV的Mat转换为BufferedImage
            BufferedImage bufferedImage = matToBufferedImage(image);
            Graphics2D g2d = bufferedImage.createGraphics();

            // 设置抗锯齿（优化绘制效果）
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 设置中文字体（兼容Windows/Linux/Mac）
            Font font = getChineseFont(16);
            g2d.setFont(font);

            // 定义颜色（鸟类红色，其他类别蓝色）
            Color birdColor = new Color(255, 30, 30);    // 红色
            Color otherColor = new Color(30, 30, 255);   // 蓝色
            Color textBgColor = new Color(0, 0, 0, 180); // 半透明黑色（标签背景）

            // 绘制每个检测框和标签
            for (Detection detection : detections) {
                BoundingBox box = detection.bbox;
                String className = classes.get(detection.classId);
                String label = String.format("%s (%.2f)", className, detection.confidence);

                // 选择框的颜色
                Color boxColor = isBirdClass(className) ? birdColor : otherColor;

                // 绘制边界框（3px粗线）
                g2d.setColor(boxColor);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRect(box.x, box.y, box.width, box.height);

                // 绘制中文标签（带背景）
                drawChineseLabel(g2d, box, label, textBgColor);
            }

            g2d.dispose();
            return bufferedImage;

        } catch (Exception e) {
            System.err.println("❌ 绘制检测结果失败: " + e.getMessage());
            return matToBufferedImage(image); // 失败时返回原始图片
        }
    }

    // 获取支持中文的字体（兼容多系统）
    private static Font getChineseFont(int size) {
        String[] preferredFonts = {
                "微软雅黑", "Microsoft YaHei",
                "思源黑体", "Source Han Sans CN",
                "黑体", "SimHei",
                "宋体", "SimSun",
                "楷体", "KaiTi",
                "Arial Unicode MS",
                "Noto Sans CJK SC"
        };

        for (String fontName : preferredFonts) {
            Font font = new Font(fontName, Font.BOLD, size);
            // 验证字体是否有效（避免系统不存在该字体）
            if (font.getFamily() != null && !font.getFamily().equals("Dialog")) {
                System.out.println("✅ 使用中文字体: " + fontName);
                return font;
            }
        }

        //  fallback：使用系统默认字体
        System.err.println("⚠️  未找到最优中文字体，使用系统默认字体");
        return new Font(Font.SANS_SERIF, Font.BOLD, size);
    }

    // 绘制中文标签（带半透明背景）
    private static void drawChineseLabel(Graphics2D g2d, BoundingBox box, String label, Color bgColor) {
        FontMetrics metrics = g2d.getFontMetrics();
        int textWidth = metrics.stringWidth(label);
        int textHeight = metrics.getAscent(); // 字体高度（不含 descent）

        // 标签位置：框的上方居中（避免超出图片顶部）
        int labelX = box.x + (box.width - textWidth) / 2;
        int labelY = Math.max(box.y - 10, textHeight + 5);

        // 绘制半透明背景
        g2d.setColor(bgColor);
        g2d.fillRect(labelX - 4, labelY - textHeight - 3, textWidth + 8, textHeight + 6);

        // 绘制白色文字
        g2d.setColor(Color.WHITE);
        g2d.drawString(label, labelX, labelY - 3);
    }

    // OpenCV Mat 转换为 Java BufferedImage
    private static BufferedImage matToBufferedImage(Mat mat) {
        if (mat.empty()) {
            throw new IllegalArgumentException("输入Mat为空");
        }

        Mat rgbMat = new Mat();
        // 转换颜色空间（OpenCV默认BGR，Java默认RGB）
        if (mat.channels() == 3) {
            Imgproc.cvtColor(mat, rgbMat, Imgproc.COLOR_BGR2RGB);
        } else if (mat.channels() == 1) {
            rgbMat = mat.clone(); // 灰度图直接使用
        } else {
            throw new UnsupportedOperationException("不支持的图片通道数: " + mat.channels());
        }

        // 确保图片是8位无符号整数类型
        if (rgbMat.depth() != CvType.CV_8U) {
            rgbMat.convertTo(rgbMat, CvType.CV_8U, 255.0); // 归一化到0-255
        }

        // 创建BufferedImage并复制数据
        int imageType = rgbMat.channels() == 1 ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR;
        BufferedImage image = new BufferedImage(rgbMat.cols(), rgbMat.rows(), imageType);
        byte[] data = new byte[rgbMat.cols() * rgbMat.rows() * (int) rgbMat.elemSize()];
        rgbMat.get(0, 0, data);
        image.getRaster().setDataElements(0, 0, rgbMat.cols(), rgbMat.rows(), data);

        return image;
    }

    // 分析检测结果（封装返回信息）
    private static void analyzeDetectionResult(DetectionResult result, List<Detection> detections) {
        if (detections.isEmpty()) {
            result.setConfidence(0.0);
            result.setDetectionResult("未检测到任何目标");
            return;
        }

        // 获取最高置信度
        double maxConfidence = detections.stream()
                .mapToDouble(d -> d.confidence)
                .max()
                .orElse(0.0);
        result.setConfidence(maxConfidence);

        // 统计所有检测到的类别
        Map<String, Integer> classCount = new HashMap<>();
        for (Detection detection : detections) {
            String className = classes.get(detection.classId);
            classCount.put(className, classCount.getOrDefault(className, 0) + 1);
        }

        // 构建结果描述
        StringBuilder resultMsg = new StringBuilder("检测到 " + detections.size() + " 个目标：");
        for (Map.Entry<String, Integer> entry : classCount.entrySet()) {
            resultMsg.append(entry.getValue()).append("个").append(entry.getKey()).append("，");
        }
        resultMsg.setLength(resultMsg.length() - 1); // 移除最后一个逗号
        resultMsg.append("（最高置信度：").append(String.format("%.2f", maxConfidence)).append("）");

        result.setDetectionResult(resultMsg.toString());
    }

    // 判断是否为鸟类类别
    private static boolean isBirdClass(String className) {
        if (className == null || className.isEmpty()) return false;
        String lowerName = className.toLowerCase();
        // 中文鸟类关键词
        String[] birdKeywords = {"鸟", "鹇", "鸮", "鸠", "隼", "鹰", "雀", "雁", "鹤", "鸥"};
        // 英文鸟类关键词
        String[] birdKeywordsEn = {"bird", "eagle", "owl", "dove", "falcon", "sparrow", "goose", "crane"};

        for (String keyword : birdKeywords) {
            if (lowerName.contains(keyword)) return true;
        }
        for (String keyword : birdKeywordsEn) {
            if (lowerName.contains(keyword)) return true;
        }
        return false;
    }

    // 加载类别名称（从test.names文件）
    private static List<String> loadClasses() {
        List<String> classes = new ArrayList<>();
        try (InputStream inputStream = YoloONNXUtil.class.getClassLoader().getResourceAsStream(MODEL_CLASSES)) {
            if (inputStream == null) {
                throw new RuntimeException("类别文件未找到: " + MODEL_CLASSES + "（请检查classpath路径）");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                String className = line.trim();
                if (!className.isEmpty()) { // 跳过空行
                    classes.add(className);
                    System.out.println("📋 类别" + lineNum + ": " + className);
                    lineNum++;
                }
            }

            if (classes.isEmpty()) {
                throw new RuntimeException("类别文件" + MODEL_CLASSES + "为空");
            }
            System.out.println("✅ 成功加载 " + classes.size() + " 个类别");
        } catch (IOException e) {
            throw new RuntimeException("加载类别文件失败: " + e.getMessage(), e);
        }
        return classes;
    }

    // 动态调整置信度阈值（测试用）
    public static void setConfidenceThreshold(double threshold) {
        if (threshold < 0 || threshold > 1) {
            throw new IllegalArgumentException("置信度阈值必须在0-1之间");
        }
        CONFIDENCE_THRESHOLD = threshold;
        System.out.println("🔧 置信度阈值已调整为: " + threshold);
    }

    // 边界框实体类
    private static class BoundingBox {
        int x;      // 左上角x坐标
        int y;      // 左上角y坐标
        int width;  // 宽度
        int height; // 高度

        BoundingBox(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    // 检测结果实体类（单检测框）
    private static class Detection {
        int classId;       // 类别ID
        float confidence;  // 置信度
        BoundingBox bbox;  // 边界框

        Detection(int classId, float confidence, BoundingBox bbox) {
            this.classId = classId;
            this.confidence = confidence;
            this.bbox = bbox;
        }
    }

    // 对外返回的检测结果封装类
    public static class DetectionResult {
        private byte[] detectedImageBytes; // 绘制检测框后的图片字节数组
        private double confidence;         // 最高置信度
        private String detectionResult;    // 检测结果描述（中文）

        // Getter/Setter
        public byte[] getDetectedImageBytes() { return detectedImageBytes; }
        public void setDetectedImageBytes(byte[] detectedImageBytes) { this.detectedImageBytes = detectedImageBytes; }
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public String getDetectionResult() { return detectionResult; }
        public void setDetectionResult(String detectionResult) { this.detectionResult = detectionResult; }
    }

    // 测试方法（本地运行测试）
    public static void main(String[] args) {
        try {
            // 测试：读取本地图片文件
            File imageFile = new File("test.jpg"); // 替换为你的测试图片路径
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());

            // 调整阈值（可选）
            YoloONNXUtil.setConfidenceThreshold(0.5);

            // 执行检测
            DetectionResult result = YoloONNXUtil.detect(imageBytes);

            // 保存检测结果图片
            File outputFile = new File("detected_result.jpg");
            Files.write(outputFile.toPath(), result.getDetectedImageBytes());

            System.out.println("🎉 测试完成！检测结果已保存到: " + outputFile.getAbsolutePath());
            System.out.println("📋 检测结果描述: " + result.getDetectionResult());
            System.out.println("📋 最高置信度: " + String.format("%.2f", result.getConfidence()));

        } catch (Exception e) {
            System.err.println("❌ 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}