package com.marvin.bot;

import com.marvin.bot.music.MusicBot;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class BotApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().load();
        String botToken = dotenv.get("PUBLIC_KEY_DISCORD");
        String youTubeKey = dotenv.get("YOUTUBE_KEY");


        JDABuilder.createDefault(botToken)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new MusicBot(youTubeKey))
                .build();
    }


}
