package ru.kpfu.itis;

import com.amazon.sqs.javamessaging.AmazonSQSMessagingClientWrapper;
import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import org.json.JSONException;
import org.json.JSONObject;
import yandex.cloud.api.ai.vision.v1.FaceDetection;
import yandex.cloud.api.ai.vision.v1.Primitives;
import yandex.cloud.api.ai.vision.v1.VisionServiceGrpc;
import yandex.cloud.api.ai.vision.v1.VisionServiceOuterClass;
import yandex.cloud.sdk.ServiceFactory;
import yandex.cloud.sdk.auth.Auth;
import yandex.cloud.sdk.auth.provider.CredentialProvider;


import javax.jms.*;
import javax.jms.Message;
import javax.imageio.ImageIO;
import javax.jms.JMSException;
import javax.jms.Session;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FaceDetector {
    public void getFaces(String filename) throws IOException, JSONException, JMSException {
        AmazonS3 s3 = InitConnection.getInstance();
        S3Object photo = s3.getObject("vvot", filename);
        S3ObjectInputStream inputStream = photo.getObjectContent();
        byte[] photoData = IOUtils.toByteArray(inputStream);
        CredentialProvider defaultComputeEngine = Auth.computeEngineBuilder().build();
        ServiceFactory factory = ServiceFactory.builder()
                .credentialProvider(defaultComputeEngine
                )
                .build();
        VisionServiceGrpc.VisionServiceBlockingStub visionService = factory.create(VisionServiceGrpc.VisionServiceBlockingStub.class, VisionServiceGrpc::newBlockingStub);
        VisionServiceOuterClass.BatchAnalyzeRequest request = VisionServiceOuterClass.BatchAnalyzeRequest.newBuilder()
                .setFolderId("b1gp2r48phv2a71qfpq8")
                .addAnalyzeSpecs(VisionServiceOuterClass.AnalyzeSpec.newBuilder()
                        .addFeatures(VisionServiceOuterClass.Feature.newBuilder().setType(VisionServiceOuterClass.Feature.Type.FACE_DETECTION).build())
                        .setContent(ByteString.copyFrom(photoData))
                        .build())
                .build();
        VisionServiceOuterClass.BatchAnalyzeResponse response = visionService.batchAnalyze(request);
        VisionServiceOuterClass.AnalyzeResult result = response.getResults(0);
        List<VisionServiceOuterClass.FeatureResult> featureResult = result.getResultsList();
        for (VisionServiceOuterClass.FeatureResult value : featureResult) {
            List<String> cropNames = new ArrayList<>();
            List<FaceDetection.Face> facesList = value.getFaceDetection().getFacesList();
            for (int j = 0; j < facesList.size(); j++) {
                List<Primitives.Vertex> vertexList = facesList.get(j).getBoundingBox().getVerticesList();
                List<Long> x = new ArrayList<>();
                List<Long> y = new ArrayList<>();
                System.out.println(j);
                for (Primitives.Vertex vertex : vertexList) {
                    x.add(vertex.getX());
                    y.add(vertex.getY());
                }
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(photoData));
                BufferedImage crop = image.getSubimage(x.get(0).intValue(), y.get(0).intValue(), x.get(2).intValue() - x.get(0).intValue(), y.get(2).intValue() - y.get(0).intValue());
                x.forEach(System.out::println);
                y.forEach(System.out::println);
                String fileNameWithoutExtension = filename.substring(0, filename.lastIndexOf("."));
                String crops = "[FACE]" + fileNameWithoutExtension + j + ".jpeg";
                saveToBucket(filename, crop, crops);
                cropNames.add(crops);
            }
            tiePhotoAndFace(cropNames, filename);
            sendToMessageQueue(cropNames);
        }
    }


    public void saveToBucket(String originalName, BufferedImage crop, String cropName) throws IOException, JSONException {
        AmazonS3 s3 = InitConnection.getInstance();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(crop, "jpeg", os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength((long) IOUtils.toByteArray(is).length);
        ByteArrayOutputStream os1 = new ByteArrayOutputStream();
        ImageIO.write(crop, "jpeg", os1);
        InputStream is1 = new ByteArrayInputStream(os.toByteArray());
        s3.putObject("vvot", cropName, is1, objectMetadata);
        //привязка лиц к фото
    }

    public void tiePhotoAndFace(List<String> faces, String photo) throws IOException, JSONException {
        AmazonS3 s3 = InitConnection.getInstance();
        S3Object faceObject = s3.getObject("vvot", "faces.json");
        S3ObjectInputStream s3ObjectInputStream = faceObject.getObjectContent();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(s3ObjectInputStream));
        String jsonText = readAll(bufferedReader);
        if (jsonText.isEmpty()) {
            JSONObject json = new JSONObject();
            json.put(photo, faces);
            s3.putObject("vvot", "faces.json", json.toString());
        } else {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, List<String>> jsonMap = objectMapper.readValue(jsonText, new TypeReference<Map<String, List<String>>>() {
            });
            if (!jsonMap.containsKey(photo)) {
                jsonMap.put(photo, faces);
                s3.putObject("vvot", "faces.json", new ObjectMapper().writeValueAsString(jsonMap));
            } else {
                for (String face : faces) {
                    jsonMap.get(photo).add(face);
                }
                s3.putObject("vvot", "faces.json", new ObjectMapper().writeValueAsString(jsonMap));
            }
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public void sendToMessageQueue(List<String> cropNames) throws JMSException {
        String queueName = "face_queue";
        BasicAWSCredentials awsCreds = new BasicAWSCredentials("9HW2D4GFTrYHB-TJNqax", "0IyIbaoYdBS-09ffHMOONeYw-zfQTwG71qgSIDiZ");
        SQSConnectionFactory connectionFactory = new SQSConnectionFactory(
                new ProviderConfiguration(),
                AmazonSQSClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                                "https://message-queue.api.cloud.yandex.net",
                                "ru-central1"
                        ))
        );
        SQSConnection connection = connectionFactory.createConnection();

        AmazonSQSMessagingClientWrapper client = connection.getWrappedAmazonSQSClient();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue(queueName);
        MessageProducer producer = session.createProducer(queue);
        StringBuilder sb = new StringBuilder();
        for (String cropName : cropNames) {
            sb.append(cropName).append(" ");
        }
        Message message = session.createTextMessage(sb.toString());
        producer.send(message);
    }
}
