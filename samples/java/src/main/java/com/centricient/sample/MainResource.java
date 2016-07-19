package com.centricient.sample;

import com.centricient.sample.api.Conversation;
import com.centricient.sample.api.EventPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MainResource {
    // TODO: Never compile your secret into your code... read this from a secure location or pass it in securely!
    private static String OUR_HIDDEN_SECRET = "THE_SECRET_YOU_GOT_WHEN_YOU_REGISTERED";

    private ObjectMapper mapper = new ObjectMapper();

    public MainResource() {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @POST
    @Path("/handler/")
    public void handler(@HeaderParam("X-Centricient-Hook-Token") String secret, EventPayload event) throws IOException {
        if (!OUR_HIDDEN_SECRET.equals(secret))
            throw new WebApplicationException("Invalid", Response.Status.FORBIDDEN);

        if (event.getEventType().equals("test")){
            System.out.println("Test was called");
        } else if (event.getEventType().equals("ConversationStatusChanged")) {
            Conversation conversation = mapper.treeToValue(event.getData(), Conversation.class);
            System.out.println("Conversation status changed to: " + conversation.getStatus());
            prettyPrintConversation(conversation);
        }
    }

    private void prettyPrintConversation(Conversation conversation) throws JsonProcessingException {
        String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(conversation);
        System.out.print(json);
    }
}
