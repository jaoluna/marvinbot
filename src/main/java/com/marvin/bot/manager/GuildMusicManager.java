package com.marvin.bot.manager;

import com.marvin.bot.music.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

public class GuildMusicManager {

    public final AudioPlayer player;
    public final TrackScheduler scheduler;


    public GuildMusicManager(AudioPlayerManager playerManager){
        player = playerManager.createPlayer();
        scheduler = new TrackScheduler(player);
        player.addListener(scheduler);
    }


}
