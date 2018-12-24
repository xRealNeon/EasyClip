package me.NeoCode.easyclip;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;

public class Controller {

	@FXML
	TextField formatting;

	@FXML
	Button dlclip;
	String name = "";

	@FXML
	private void initialize() {
		formatting.setText(EasyClip.format);
		dlclip.setOnAction((event -> {

			try {
				EasyClip.format = formatting.getText();
				EasyClip.saveConfig();
			} catch (IOException e2) {
				e2.printStackTrace();
			}

			if (getClipName() == null) {
				System.err.println("Clip not found!");
				showDialog("EasyClip", "Error", AlertType.ERROR,
						"Clip not found make sure to copy the link of the clip");
				return;
			}

			dlclip.setDisable(true);
			new Thread(new Runnable() {

				@Override
				public void run() {
					System.out.println("Loading Clip " + getClipName());
					JSONObject rootURL = new JSONObject(getVideoUrl());

					try {
						JSONObject rootClip = new JSONObject(getClip(getClipName()));

						String format = formatting.getText();

						if (format.contains("%SLUG%")) {
							format = format.replace("%SLUG%", rootClip.getString("slug"));
						}
						if (format.contains("%NAME%")) {
							format = format.replace("%NAME%", rootClip.getString("title"));
						}
						if (format.contains("%BROADCASTER%")) {
							format = format.replace("%BROADCASTER%",
									rootClip.getJSONObject("broadcaster").getString("name"));
						}
						if (format.contains("%CREATOR%")) {
							format = format.replace("%CREATOR%", rootClip.getJSONObject("curator").getString("name"));
						}
						if (format.contains("%GAME%")) {
							format = format.replace("%GAME%", rootClip.getString("game"));
						}
						if (format.contains("%DATE%")) {
							format = format.replace("%DATE%", rootClip.getString("created_at"));
						}
						if (format.contains("%ID%")) {
							format = format.replace("%ID%", rootClip.getString("tracking_id"));
						}

						if (format.contains(":")) {
							format = format.replace(":", "_");
						}
						if (format.contains("*")) {
							format = format.replace("*", "_");
						}
						if (format.contains(";")) {
							format = format.replace(";", "_");
						}
						if (format.contains("/")) {
							format = format.replace("/", "_");
						}
						if (format.contains("|")) {
							format = format.replace("|", "_");
						}
						if (format.contains("?")) {
							format = format.replace("?", "_");
						}

						name = format + ".mp4";
					} catch (JSONException | IOException e1) {
						e1.printStackTrace();
					}

					try {
						System.out.println("Downloading Clip...");
						saveUrl(System.getProperty("user.home") + "\\Videos\\EasyClip\\" + name,
								rootURL.getJSONArray("quality_options").getJSONObject(0).getString("source"));
						System.out.println("Clip saved as " + name);
						Platform.runLater(() -> {
							showDialog("EasyClip", "Succsessfull", AlertType.INFORMATION, "Clip saved as " + name);
							dlclip.setDisable(false);
						});
					} catch (JSONException | IOException e) {
						e.printStackTrace();
					}
				}
			}).start();

		}));
	}

	private void showDialog(String title, String header, AlertType type, String text) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(text);
		alert.getDialogPane().getScene().getWindow();
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(getClass().getResourceAsStream("icon.png")));
		alert.showAndWait().ifPresent(rs -> {
		});
	}

	private String getClip(String name) throws IOException {
		String url = "https://api.twitch.tv/kraken/clips/" + name;

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("GET");

		con.setRequestProperty("Accept", "application/vnd.twitchtv.v5+json");
		con.setRequestProperty("Client-ID", "b2ekx1xj2n294i52er7c2n1vcgjevs");

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		return response.toString();
	}

	private String getClipName() {
		try {
			String raw = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);

			if (!raw.contains("https://clips.twitch.tv/")) {
				return null;
			}

			return raw.replaceAll("https://clips.twitch.tv/", "");
		} catch (HeadlessException | UnsupportedFlavorException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getVideoUrl() {
		try {
			URL link = new URL("https://clips.twitch.tv/api/v2/clips/" + getClipName() + "/status");
			BufferedReader in = new BufferedReader(new InputStreamReader(link.openStream()));
			String line;

			while ((line = in.readLine()) != null) {
				return line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void saveUrl(final String filename, final String urlString)
			throws MalformedURLException, IOException {
		BufferedInputStream in = null;
		FileOutputStream fout = null;
		try {
			in = new BufferedInputStream(new URL(urlString).openStream());
			fout = new FileOutputStream(filename);

			final byte data[] = new byte[1024];
			int count;
			while ((count = in.read(data, 0, 1024)) != -1) {
				fout.write(data, 0, count);
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (fout != null) {
				fout.close();
			}
		}
	}
}
