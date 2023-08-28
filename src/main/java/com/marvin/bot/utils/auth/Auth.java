package com.marvin.bot.utils.auth;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class Auth {

    public static final HttpTransport HTTP_TRANSPORT;
    public static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();


    private Auth() throws IllegalAccessException {
        throw new IllegalAccessException("RODRIGO GOES OUT");
    }



    static {
        HttpTransport httpTransport = null;
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        }catch (GeneralSecurityException | IOException exception){
            exception.printStackTrace();
        }
        HTTP_TRANSPORT = httpTransport;
    }


}
