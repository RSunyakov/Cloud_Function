package ru.kpfu.itis;

import org.json.JSONException;

import javax.jms.JMSException;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, JSONException, JMSException {
        FaceDetector faceDetector = new FaceDetector();

        faceDetector.getFaces("fake_ai_faces.jpeg");
    }
}
