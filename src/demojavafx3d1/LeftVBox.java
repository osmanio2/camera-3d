package demojavafx3d1;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

class LeftVBox extends VBox {
	private Text x = new Text("x:");
	private Text y = new Text("y:");
	private Text z = new Text("z:");
	private Text sphereLabel = new Text("Sphere Radius");
	private Text originLabel = new Text("Override Origin");
	private Text fileNameLabel = new Text("No File Chosen");
	private Text fieldOfViewLabel = new Text("Field of View");
	private Text cameraDistanceLabel = new Text("Camera Distance");
	private Button openButton = new Button("Choose File...");
	private Button buildButton = new Button("Build");
	private Button updateButton = new Button("Update");
	private FileChooser fileChooser = new FileChooser();

	private HBox fileHbox = new HBox();
	private HBox setOriginHBox = new HBox();
	private Text title = new Text("Settings");
	private TextField xTextField = new TextField();
	private TextField yTextField = new TextField();
	private TextField zTextField = new TextField();
	private Slider sphereSlider = buildSlider();
	private Slider fieldOfViewSlider = buildSlider();
	private Slider cameraDistanceSlider = buildSlider();
	private CheckBox axesCheckBox = new CheckBox("Show Axes");
	private CheckBox setOriginCheckBox = new CheckBox(
			"Set Origin to Center Of Mass");

	public LeftVBox() {
		title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
		fileHbox.setSpacing(8);
		setOriginHBox.setSpacing(5);
		openButton.setMinWidth(105);
		axesCheckBox.setSelected(true);

		setSpacing(8);
		setPrefWidth(300);
		setPadding(new Insets(10));

		xTextField.setPrefColumnCount(3);
		yTextField.setPrefColumnCount(3);
		zTextField.setPrefColumnCount(3);

		setOriginHBox.getChildren().addAll(x, xTextField, y, yTextField, z,
				zTextField, updateButton);
		fileHbox.getChildren().addAll(openButton, fileNameLabel);
		getChildren().addAll(title, cameraDistanceLabel, cameraDistanceSlider,
				fieldOfViewLabel, fieldOfViewSlider, sphereLabel, sphereSlider,
				originLabel, setOriginHBox, setOriginCheckBox, axesCheckBox,
				fileHbox, buildButton);
	}

	private Slider buildSlider() {
		Slider slider = new Slider();
		slider.setMin(0.2);
		slider.setMax(8.2);
		slider.setValue(4.2);
		slider.setShowTickLabels(true);
		slider.setShowTickMarks(true);
		slider.setMajorTickUnit(1);
		slider.setMinorTickCount(5);
		slider.setBlockIncrement(0.2);

		slider.setLabelFormatter(new StringConverter<Double>() {
			@Override
			public String toString(Double n) {
				for (int i = 1; i < 5; i++) {
					if (n < i)
						return "1/" + String.valueOf(6 - i);
				}

				if (n < 5)
					return "1";

				for (int i = 6; i < 10; i++) {
					if (n < i)
						return String.valueOf(i - 4);
				}

				return "";
			}

			@Override
			public Double fromString(String s) {
				return Double.valueOf(s);
			}
		});

		return slider;
	}

	public void setCameraDistanceSliderValue(double v) {
		this.cameraDistanceSlider.setValue(v);
	}

	public void setFieldOfViewSliderValue(double v) {
		this.fieldOfViewSlider.setValue(v);
	}

	public void setSphereSliderValue(double v) {
		this.sphereSlider.setValue(v);
	}

	public void updateSetOriginCheckBox(boolean b) {
		this.setOriginCheckBox.setSelected(b);
	}

	public Slider getCameraDistanceSlider() {
		return this.cameraDistanceSlider;
	}

	public Slider getFieldOfViewSlider() {
		return this.fieldOfViewSlider;
	}

	public Slider getSphereSlider() {
		return this.sphereSlider;
	}

	public Button getBuildButton() {
		return this.buildButton;
	}

	public Button getUpdateButton() {
		return this.updateButton;
	}

	public double[] getNewOrigin() {
		double[] newOrigin = new double[3];
		try {
			newOrigin[0] = Double.parseDouble(xTextField.getText());
			newOrigin[1] = Double.parseDouble(yTextField.getText());
			newOrigin[2] = Double.parseDouble(zTextField.getText());
		} catch (NumberFormatException nfe) {
			return null;
		}

		return newOrigin;
	}

	public Button getOpenButton() {
		return this.openButton;
	}

	public Text getFileNameLabel() {
		return this.fileNameLabel;
	}

	public FileChooser getFileChooser() {
		return this.fileChooser;
	}

	public CheckBox getSetOriginCheckBox() {
		return this.setOriginCheckBox;
	}

	public CheckBox getAxesCheckBox() {
		return this.axesCheckBox;
	}
}
