package application;
	
import org.opencv.core.Core;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/FXML_UI.fxml"));
			
			BorderPane rootElement = (BorderPane) loader.load();
			
			rootElement.setStyle("-fx-background-color: whitesmoke");
			
			Scene scene = new Scene(rootElement,800,600);
			
			scene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());
			
			primaryStage.setTitle("Camera Calibration");
			
			primaryStage.setScene(scene);
		
			CalibrationController controller = loader.getController();
			
			controller.init();
			
			primaryStage.show();
		
		} catch(Exception e) {
			
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		launch(args);
	}
}
