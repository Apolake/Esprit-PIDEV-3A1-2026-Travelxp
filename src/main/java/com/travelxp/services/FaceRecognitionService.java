package com.travelxp.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import static org.bytedeco.opencv.global.opencv_core.CV_32SC1;
import static org.bytedeco.opencv.global.opencv_imgcodecs.IMREAD_GRAYSCALE;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imread;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_GRAY2BGR;
import static org.bytedeco.opencv.global.opencv_imgproc.LINE_8;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

/**
 * Service for face detection and recognition using OpenCV (via JavaCV).
 * Uses Haar Cascade for face detection and LBPH for face recognition.
 * Face images are stored in the "faces/{userId}/" directory.
 */
public class FaceRecognitionService {

    private static final String FACE_DATA_DIR = "faces";
    private static final String CASCADE_FILE = "haarcascade_frontalface_default.xml";
    private static final String CASCADE_URL =
            "https://raw.githubusercontent.com/opencv/opencv/4.x/data/haarcascades/haarcascade_frontalface_default.xml";
    private static final String MODEL_FILE = "face_model.yml";
    private static final int FACE_WIDTH = 200;
    private static final int FACE_HEIGHT = 200;
    private static final double CONFIDENCE_THRESHOLD = 85.0;

    private CascadeClassifier faceDetector;
    private LBPHFaceRecognizer faceRecognizer;
    private OpenCVFrameGrabber grabber;
    private final OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

    /**
     * Initialize the face recognition service.
     * Downloads the Haar cascade file if not present and loads any existing trained model.
     */
    public FaceRecognitionService() throws Exception {
        Files.createDirectories(Paths.get(FACE_DATA_DIR));
        initCascade();
        loadModel();
    }

    private void initCascade() throws Exception {
        Path cascadePath = Paths.get(FACE_DATA_DIR, CASCADE_FILE);
        if (!Files.exists(cascadePath)) {
            System.out.println("Downloading face detection model...");
            try (InputStream in = URI.create(CASCADE_URL).toURL().openStream()) {
                Files.copy(in, cascadePath, StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("Face detection model downloaded successfully.");
        }
        faceDetector = new CascadeClassifier(cascadePath.toString());
        if (faceDetector.empty()) {
            throw new RuntimeException("Failed to load face detection cascade file.");
        }
    }

    private void loadModel() {
        Path modelPath = Paths.get(FACE_DATA_DIR, MODEL_FILE);
        if (Files.exists(modelPath)) {
            faceRecognizer = LBPHFaceRecognizer.create();
            faceRecognizer.read(modelPath.toString());
        }
    }

    // ==================== Webcam Operations ====================

    /**
     * Start the webcam capture.
     */
    public void startWebcam() throws FrameGrabber.Exception {
        grabber = new OpenCVFrameGrabber(0);
        grabber.setImageWidth(640);
        grabber.setImageHeight(480);
        grabber.start();
    }

    /**
     * Stop the webcam and release resources.
     */
    public void stopWebcam() {
        if (grabber != null) {
            try {
                grabber.stop();
                grabber.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            grabber = null;
        }
    }

    /**
     * Grab a single frame from the webcam as an OpenCV Mat.
     * The returned Mat is a clone (safe to keep across frames).
     */
    public Mat grabFrame() throws FrameGrabber.Exception {
        if (grabber == null) return null;
        Frame frame = grabber.grab();
        if (frame != null && frame.image != null) {
            Mat mat = converter.convert(frame);
            return mat != null ? mat.clone() : null;
        }
        return null;
    }

    // ==================== Face Detection ====================

    /**
     * Detect all faces in the given image.
     * @return RectVector containing bounding rectangles for each detected face.
     */
    public RectVector detectFaces(Mat image) {
        Mat gray = new Mat();
        if (image.channels() > 1) {
            cvtColor(image, gray, COLOR_BGR2GRAY);
        } else {
            gray = image.clone();
        }
        RectVector faces = new RectVector();
        faceDetector.detectMultiScale(gray, faces, 1.1, 5, 0, new Size(80, 80), new Size());
        return faces;
    }

    /**
     * Extract and normalize a single face from the image.
     * Returns a grayscale, resized face image suitable for recognition.
     * @return The normalized face Mat, or null if no face detected.
     */
    public Mat extractFace(Mat image) {
        RectVector faces = detectFaces(image);
        if (faces.size() > 0) {
            Rect faceRect = faces.get(0);
            Mat gray = new Mat();
            if (image.channels() > 1) {
                cvtColor(image, gray, COLOR_BGR2GRAY);
            } else {
                gray = image.clone();
            }
            Mat croppedFace = new Mat(gray, faceRect);
            Mat resizedFace = new Mat();
            resize(croppedFace, resizedFace, new Size(FACE_WIDTH, FACE_HEIGHT));
            return resizedFace;
        }
        return null;
    }

    /**
     * Draw green rectangles around detected faces on the image.
     */
    public void drawFaceRects(Mat image, RectVector faces) {
        for (long i = 0; i < faces.size(); i++) {
            Rect face = faces.get(i);
            rectangle(image, face, new Scalar(0, 255, 0, 0), 2, LINE_8, 0);
        }
    }

    // ==================== Face Enrollment ====================

    /**
     * Enroll a user's face by saving face images and retraining the model.
     * @param userId The user's database ID (used as the recognition label).
     * @param faceImages List of normalized face Mat images.
     */
    public boolean enrollFace(int userId, List<Mat> faceImages) throws IOException {
        Path userDir = Paths.get(FACE_DATA_DIR, String.valueOf(userId));
        Files.createDirectories(userDir);

        // Clear existing face images for this user
        try (var stream = Files.list(userDir)) {
            stream.filter(p -> p.toString().endsWith(".png"))
                  .forEach(p -> {
                      try { Files.delete(p); } catch (IOException e) { /* ignore */ }
                  });
        }

        // Save new face images
        for (int i = 0; i < faceImages.size(); i++) {
            String path = userDir.resolve("face_" + i + ".png").toString();
            imwrite(path, faceImages.get(i));
        }

        // Retrain the recognition model with all enrolled faces
        trainModel();
        return true;
    }

    /**
     * Train the LBPH face recognizer with all enrolled face images.
     * Scans the faces/ directory for user subdirectories containing face PNGs.
     */
    public void trainModel() throws IOException {
        Path faceDataPath = Paths.get(FACE_DATA_DIR);
        List<Mat> imageList = new ArrayList<>();
        List<Integer> labelList = new ArrayList<>();

        try (var userDirs = Files.list(faceDataPath)) {
            userDirs.filter(Files::isDirectory).forEach(userDir -> {
                try {
                    int userId = Integer.parseInt(userDir.getFileName().toString());
                    try (var faceFiles = Files.list(userDir)) {
                        faceFiles.filter(f -> f.toString().endsWith(".png")).forEach(f -> {
                            Mat img = imread(f.toString(), IMREAD_GRAYSCALE);
                            if (img != null && !img.empty()) {
                                Mat resized = new Mat();
                                resize(img, resized, new Size(FACE_WIDTH, FACE_HEIGHT));
                                imageList.add(resized);
                                labelList.add(userId);
                            }
                        });
                    }
                } catch (NumberFormatException | IOException e) {
                    // Skip non-numeric directories (e.g., model file)
                }
            });
        }

        if (imageList.isEmpty()) return;

        MatVector images = new MatVector(imageList.size());
        for (int i = 0; i < imageList.size(); i++) {
            images.put(i, imageList.get(i));
        }

        int[] labelsArray = labelList.stream().mapToInt(Integer::intValue).toArray();
        IntPointer labelsPtr = new IntPointer(labelsArray);
        Mat labels = new Mat(labelsArray.length, 1, CV_32SC1, labelsPtr);

        faceRecognizer = LBPHFaceRecognizer.create();
        faceRecognizer.train(images, labels);

        Path modelPath = Paths.get(FACE_DATA_DIR, MODEL_FILE);
        faceRecognizer.save(modelPath.toString());
        System.out.println("Face recognition model trained with " + imageList.size() + " images.");
    }

    // ==================== Face Recognition ====================

    /**
     * Recognize a face image against the trained model.
     * @param faceImage A normalized grayscale face image.
     * @return The user ID if recognized (confidence below threshold), or -1 if not recognized.
     */
    public int recognizeFace(Mat faceImage) {
        if (faceRecognizer == null) return -1;

        Path modelPath = Paths.get(FACE_DATA_DIR, MODEL_FILE);
        if (!Files.exists(modelPath)) return -1;

        int[] label = new int[1];
        double[] confidence = new double[1];
        faceRecognizer.predict(faceImage, label, confidence);

        System.out.println("Face recognition - Label: " + label[0] + ", Confidence: " + confidence[0]);

        if (confidence[0] < CONFIDENCE_THRESHOLD) {
            return label[0];
        }
        return -1;
    }

    // ==================== Image Conversion ====================

    /**
     * Convert an OpenCV Mat (BGR or grayscale) to a JavaFX Image.
     * Performs BGR→RGB conversion and handles stride/padding.
     * Does NOT require javafx.swing module.
     */
    public static Image matToImage(Mat mat) {
        int width = mat.cols();
        int height = mat.rows();
        int channels = mat.channels();

        Mat bgr = mat;
        if (channels == 1) {
            bgr = new Mat();
            cvtColor(mat, bgr, COLOR_GRAY2BGR);
        }

        int step = (int) bgr.step();
        byte[] sourcePixels = new byte[step * height];
        bgr.data().get(sourcePixels);

        byte[] rgbPixels = new byte[width * height * 3];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int srcIdx = y * step + x * 3;
                int dstIdx = (y * width + x) * 3;
                rgbPixels[dstIdx]     = sourcePixels[srcIdx + 2]; // R (from B)
                rgbPixels[dstIdx + 1] = sourcePixels[srcIdx + 1]; // G
                rgbPixels[dstIdx + 2] = sourcePixels[srcIdx];     // B (from R)
            }
        }

        WritableImage image = new WritableImage(width, height);
        image.getPixelWriter().setPixels(0, 0, width, height,
                PixelFormat.getByteRgbInstance(), rgbPixels, 0, width * 3);
        return image;
    }

    // ==================== Utility ====================

    /**
     * Check if any face recognition model has been trained (i.e., any user has enrolled).
     */
    public boolean hasEnrolledFaces() {
        Path modelPath = Paths.get(FACE_DATA_DIR, MODEL_FILE);
        return Files.exists(modelPath);
    }

    /**
     * Check if a specific user has enrolled face images.
     */
    public boolean isUserEnrolled(int userId) {
        Path userDir = Paths.get(FACE_DATA_DIR, String.valueOf(userId));
        if (!Files.exists(userDir)) return false;
        try (var files = Files.list(userDir)) {
            return files.anyMatch(f -> f.toString().endsWith(".png"));
        } catch (IOException e) {
            return false;
        }
    }
}
