package ru.kpfu.itis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import ru.kpfu.itis.models.Messages;
import yandex.cloud.sdk.ServiceFactory;
import yandex.cloud.sdk.auth.Auth;
import yandex.cloud.sdk.auth.provider.CredentialProvider;

import javax.jms.JMSException;
import java.io.IOException;
import java.util.function.Function;

class Request {
    public String httpMethod;
    public String body;
}

class Response {
    public int statusCode;
    public String body;

    public Response(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }
}

public class Handler implements Function<String, Response> {
    @Override
    public Response apply(String body) {
        // Авторизация в SDK при помощи сервисного аккаунта
        CredentialProvider defaultComputeEngine = Auth.computeEngineBuilder().build();
        ServiceFactory factory = ServiceFactory.builder()
                .credentialProvider(defaultComputeEngine)
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        Messages messages = null;
        System.out.println(body);
        try {
            messages = objectMapper.readValue(body, Messages.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String objectId = messages.getMessages().get(0).getDetails().getObjectID();
        System.out.println(objectId);
        FaceDetector faceDetector = new FaceDetector();
        if (objectId.contains("[FACE]")) return new Response(200, "This is face");
        else {
            try {
                faceDetector.getFaces(objectId);
            } catch (IOException | JSONException | JMSException e) {
                e.printStackTrace();
            }
        }
        return new Response(200, objectId);
    }
}
