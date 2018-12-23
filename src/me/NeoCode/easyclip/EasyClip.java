package me.NeoCode.easyclip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Scanner;

import org.json.JSONObject;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;

public class EasyClip extends Application {

	public static double version = 1.0;

	public static String format;

	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = (BorderPane) FXMLLoader.load(getClass().getResource("Layout.fxml"));
			Scene scene = new Scene(root, 425, 106);
			scene.getStylesheets().add(getClass().getResource("Style.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.setTitle("EasyClip v" + version);
			primaryStage.setResizable(false);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		File dir = new File(System.getProperty("user.home") + "\\Videos\\EasyClip");
		if (!dir.exists()) {
			dir.mkdirs();
		}

		File confDir = new File(System.getProperty("user.home") + "\\AppData\\Roaming\\EasyClip");
		if (!confDir.exists()) {
			confDir.mkdirs();
		}
		try {
			readConfig();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		launch(args);
	}

	public static void saveConfig() throws IOException {
		File conf = new File(System.getProperty("user.home") + "\\AppData\\Roaming\\EasyClip\\config.json");
		if (!conf.exists()) {
			conf.createNewFile();
		}

		JSONObject root = new JSONObject();
		root.put("format", format);

		PrintWriter pw = new PrintWriter(new FileWriter(conf));
		pw.println(root.toString());
		pw.flush();
		pw.close();

	}

	public static void readConfig() throws FileNotFoundException {
		File conf = new File(System.getProperty("user.home") + "\\AppData\\Roaming\\EasyClip\\config.json");
		if (!conf.exists()) {
			format = "%NAME%_%ID%";
			return;
		}
		InputStream i = new FileInputStream(conf);
		Scanner s = new Scanner(i);

		String l = "";
		while (s.hasNext()) {
			l = l + s.nextLine();
		}
		s.close();

		JSONObject root = new JSONObject(l);
		format = root.getString("format");
	}

}
