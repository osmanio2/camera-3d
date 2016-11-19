package demojavafx3d1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

class dataReader {
	private Button button;
	private Text label;
	private FileChooser fileChooser;
	private Stage stage;
	private List<point> points;

	public dataReader(FileChooser fileChooser, Button button, Text label,
			Stage stage) {
		this.fileChooser = fileChooser;
		this.button = button;
		this.label = label;
		this.stage = stage;

		bindListenerToButton();
	}

	public dataReader(String filename) {
		openFile(new File(filename));
	}

	public List<point> getPoints() {
		return points;
	}

	private void bindListenerToButton() {
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent e) {
				configureFileChooser(fileChooser);
				File file = fileChooser.showOpenDialog(stage);
				if (file != null) {
					label.setText(file.getName());
					openFile(file);
				}
			}
		});
	}

	private void configureFileChooser(final FileChooser fileChooser) {
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("Point Cloud Data(PCD)",
						"*.pcd"));
	}

	private void openFile(File file) {
		points = new ArrayList<point>();
		;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			int numOfPoints = 0;

			// read number of points and skip other unused header entries
			for (int i = 0; i < 12; i++) {
				String[] temp = reader.readLine().split(" ");
				if (temp[0].equals("POINTS")) {
					numOfPoints = Integer.parseInt(temp[1]);
				} else if (temp[0].equals("DATA")) {
					break;
				}
			}

			for (int i = 0; i < numOfPoints; i++) {
				String[] coordinates = reader.readLine().split(" ");
				double x = Double.parseDouble(coordinates[0]);
				double y = Double.parseDouble(coordinates[1]);
				double z = Double.parseDouble(coordinates[2]);

				if (coordinates.length == 6) {
					double normal_x = Double.parseDouble(coordinates[3]);
					double normal_y = Double.parseDouble(coordinates[4]);
					double normal_z = Double.parseDouble(coordinates[5]);
					points.add(new point(x, y, z, normal_x, normal_y, normal_z));
				} else if (coordinates.length == 4) {
					int color = (int) Double.parseDouble(coordinates[3]);
					points.add(new point(x, y, z, color));
				} else {
					points.add(new point(x, y, z));
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
