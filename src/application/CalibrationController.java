package application;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point3;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class CalibrationController {
	
	@FXML
	private Button cameraButton;
	
	@FXML
	private Button applyButton;
	
	@FXML
	private Button snapshotButton;
	
	@FXML
	private Button saveParamsButton;
	
	@FXML
	private ImageView originalFrame;
	
	@FXML
	private ImageView calibratedFrame;
	
	@FXML
	private TextField numBoards;
	
	@FXML
	private TextField numHorCorners;
	
	@FXML
	private TextField numVertCorners;
	
	@FXML
	private TextField numOfSnaps;
	
	private Timer timer;
	
	private VideoCapture capture;
	
	private boolean CameraActive;
	
	private Mat savedImage;
	
	private Image undistoredImage, CamStream;
	
	private List<Mat> imagePoints;
	
	private List<Mat> objectPoints;
	
	private MatOfPoint3f obj;
	
	private MatOfPoint2f imageCorners;
	
	private int boardsNumber;
	
	private int numCornersHor;
	
	private int numCornersVer;
	
	private int successes;
	
	private Mat intrinsic;
	
	private Mat distCoeffs;
	
	private boolean isCalibrated;
	
	private static Logger logger = Logger.getLogger("application.FXController");
	
	protected void init() {
		
		this.capture = new VideoCapture();
		
		this.CameraActive = false;
		
		this.obj = new MatOfPoint3f();
		
		this.imageCorners = new MatOfPoint2f();
		
		this.savedImage = new Mat();
		
		this.undistoredImage = null;
		
		this.imagePoints = new ArrayList<>();
		
		this.objectPoints = new ArrayList<>();
		
		this.intrinsic = new Mat(3, 3, CvType.CV_32FC1);
		
		this.distCoeffs = new Mat();
		
		this.successes = 0;
		
		this.isCalibrated = false;
	}
	
	
	@FXML
	protected void updateSettings() {
		
		this.boardsNumber = Integer.parseInt(this.numBoards.getText());
		
		this.numCornersHor = Integer.parseInt(this.numHorCorners.getText());
		
		this.numCornersVer = Integer.parseInt(this.numVertCorners.getText());
		
		int numSquares = this.numCornersHor * this.numCornersVer;
		
		for(int j = 0; j < numSquares; j++) {
			
			obj.push_back( new MatOfPoint3f(new Point3(j / this.numCornersHor, j % this.numCornersVer, 0.0f)));
			
			this.cameraButton.setDisable(false);
			
		}
		
		//System.out.println(obj.dump());
	}
	
	@FXML
	protected void startCamera() {
		
		if(!this.CameraActive) {
			
			this.capture.open(1);
			
			if(this.capture.isOpened()) {
				
				this.CameraActive = true;
				
				TimerTask frameGrabber = new TimerTask() {

					@Override
					public void run() {

						CamStream = grabFrame();
						
						Platform.runLater( new Runnable() {

							@Override
							public void run() {
								originalFrame.setImage(CamStream);
								
								originalFrame.setFitWidth(380);
								
								originalFrame.setPreserveRatio(true);
								
								calibratedFrame.setImage(undistoredImage);
								
								calibratedFrame.setFitWidth(380);
								
								calibratedFrame.setPreserveRatio(true);
								
							}
							
						});
						
					}
					
				};
				
				this.timer = new Timer();
				
				this.timer.schedule(frameGrabber, 0, 33);
				
				this.cameraButton.setText("Stop Camera");
			
			} else {
				
				System.err.println("Impossible to open the camera connection...");
			}
		} else {
			
			this.CameraActive = false;
			
			this.cameraButton.setText("Start Camera");
			
			if(this.timer != null) {
				
				this.timer.cancel();
				
				this.timer = null;
			}
			
			this.capture.release();
			
			originalFrame.setImage(null);
			
			calibratedFrame.setImage(null);
		}
	}
	
	private Image grabFrame() {
		
		Image imageToShow = null;
		
		Mat frame = new Mat();
		
		if(this.capture.isOpened()) {
			
			try {
				
				this.capture.read(frame);
				
				if(!frame.empty()) {
					
					this.findAndDrawPoints(frame);
				
					if(this.isCalibrated) {
						
						Mat undistored = new Mat();
						
						Imgproc.undistort(frame, undistored, intrinsic, distCoeffs);
						
						undistoredImage = mat2Image(undistored);
						
					}
					
					imageToShow = mat2Image(frame);
				}
				
			} catch (Exception e) {
				
				System.err.println("Error");
				e.printStackTrace();
			}
		}
		
		return imageToShow;
	}
	
	@FXML
	protected void takeSnapshot() {
		
		if(this.successes < this.boardsNumber) {
			
			this.imagePoints.add(imageCorners);
			
			this.objectPoints.add(obj);
			
			this.successes++;
			
			this.numOfSnaps.setText(Integer.toString(this.successes));
		}
		
		if(this.successes == this.boardsNumber) {
			
			this.calibrateCamera();
		}
		
	}
	
	private void calibrateCamera() {
		
		List<Mat> rvecs = new ArrayList<>();
		
		List<Mat> tvecs = new ArrayList<>();
		
		intrinsic.put(0, 0, 1);
		intrinsic.put(1, 1, 1);
		
		Calib3d.calibrateCamera(objectPoints, imagePoints, savedImage.size(), intrinsic, distCoeffs, rvecs, tvecs);
		
		this.isCalibrated = true;
		
		this.snapshotButton.setDisable(true);
		
		this.saveParamsButton.setDisable(false);
	}
	
	private Image mat2Image(Mat frame) {
		
		MatOfByte buffer = new MatOfByte();
		
		Highgui.imencode(".png", frame, buffer);
		
		return new Image(new ByteArrayInputStream(buffer.toArray()));
	}
	
	private void findAndDrawPoints(Mat frame) {
		
		Mat grayImage = new Mat();
		
		if(this.successes < this.boardsNumber) {
			
			Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);
			
			Size boardSize = new Size(this.numCornersHor, this.numCornersVer);
			
			boolean found = Calib3d.findChessboardCorners(grayImage, boardSize, imageCorners, Calib3d.CALIB_CB_ADAPTIVE_THRESH + Calib3d.CALIB_CB_NORMALIZE_IMAGE
					+ Calib3d.CALIB_CB_FAST_CHECK);
			
			if(found) {
				
				TermCriteria term = new TermCriteria(TermCriteria.EPS | TermCriteria.MAX_ITER, 30, 0.1);
				
				Imgproc.cornerSubPix(grayImage, imageCorners, new Size(11, 11), new Size(-1, -1), term);
				
				grayImage.copyTo(this.savedImage);
				
				Calib3d.drawChessboardCorners(frame, boardSize, imageCorners, found);
				
				this.snapshotButton.setDisable(false);
				this.takeSnapshot();
				
			} else {
				
				this.snapshotButton.setDisable(true);
			}
			
		}
	}
	
	@FXML
	protected void outputCameraParams() {
		
		JsonObject obj = new JsonObject();

	    if(intrinsic.isContinuous()){
	        int cols = intrinsic.cols();
	        int rows = intrinsic.rows();
	        int type = intrinsic.type();

	        MatOfByte buffer = new MatOfByte();
			
			Highgui.imencode(".png", intrinsic, buffer);

	        byte[] data = buffer.toArray();

	        obj.addProperty("rows", rows); 
	        obj.addProperty("cols", cols); 
	        obj.addProperty("type", type);

	        // We cannot set binary data to a json object, so:
	        // Encoding data byte array to Base64.
	        String dataString = new String(Base64.getEncoder().encodeToString(data));

	        obj.addProperty("data", dataString);            

	        Gson gson = new Gson();
	        
	        String json = gson.toJson(obj);
	       
	        File file = new File("intrinsic.json");
	        
	        try {
				
	        	PrintWriter writer = new PrintWriter(new FileOutputStream(file));
	        	
	        	writer.println(json);
	        	
	        	writer.flush();
	        	
	        	writer.close();
	        	
			
	        } catch (FileNotFoundException e) {
				
				logger.log(Level.SEVERE, "Ouput JSON file failed", e);
				
			}
	    } else {
	        
	    	logger.log(Level.WARNING, "Mat not continuous.");
	    }
		
	}
	
	
	
	// 1- Take a snapshot 
	// 2- Take another snapshot
	// 3- Calculate the disparity 
	// 4- display the disparity map 
	// 5- reconstruct the 3D image projection
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
