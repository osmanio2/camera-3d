package demojavafx3d1;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.shape.Sphere;
import javafx.stage.Stage;

public class DemoJavaFX3D1 extends Application {

   @Override
   public void start(Stage primaryStage) {

      Sphere sphere=new Sphere(100);
      Group root=new Group(sphere);

      Scene scene = new Scene(root, 600, 600);

      primaryStage.setTitle("3D JavaFX");
      primaryStage.setScene(scene);
      primaryStage.show();
   }

   public static void main(String[] args) {
      Application.launch(args);
   }

}