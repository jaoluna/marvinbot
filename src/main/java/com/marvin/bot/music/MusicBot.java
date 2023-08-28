package com.marvin.bot.music;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.marvin.bot.utils.auth.Auth;
import com.marvin.bot.commands.AudioPlayerSendHandler;
import com.marvin.bot.manager.GuildMusicManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicBot extends ListenerAdapter {
    private final String YOUTUBE_API_KEY;


    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagerMap;

    private final YouTube youTube;



    public MusicBot(String youTubeAPIKey) {
        this.YOUTUBE_API_KEY = youTubeAPIKey;
        this.youTube = new YouTube.Builder(
                Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, request -> {
        }
        ).setApplicationName("MarvinBot").build();

        this.playerManager = new DefaultAudioPlayerManager();
        this.musicManagerMap = new HashMap<>();

        AudioSourceManagers.registerRemoteSources(playerManager);
    }



    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) {
            return;
        }

        String messageContent = event.getMessage().getContentRaw();

        if (messageContent.startsWith("!play ")) {
            String searchQuery = messageContent.substring(6);
            searchAndPlay(event, searchQuery);
        }
    }


    private synchronized GuildMusicManager getServerAudioPlayer(Guild guild){

        long guildId = Long.parseLong(guild.getId());

        GuildMusicManager musicManager = musicManagerMap.computeIfAbsent(guildId, obj -> new GuildMusicManager(playerManager));

//        GuildMusicManager musicManager = musicManagerMap.get(guildId);
//        if (musicManager == null){
//            musicManager = new GuildMusicManager(playerManager);
//            musicManagerMap.put(guildId, musicManager);
//        }

        AudioPlayer audioPlayer = musicManager.player;
        AudioPlayerSendHandler sendHandler = new AudioPlayerSendHandler(audioPlayer);

        guild.getAudioManager().setSendingHandler(sendHandler);


        return musicManager;

    }


    private void searchAndPlay(MessageReceivedEvent event, String searchString) {

        try {

            if(searchString.contains("youtube.com/watch?v=")){
                loadAndPlay(event.getChannel().asTextChannel(), searchString);
                return;
            }

            YouTube.Search.List search = youTube.search().list("id");
            search.setKey(YOUTUBE_API_KEY);
            search.setQ(searchString);
            search.setType("video");
            search.setMaxResults(1L);


            SearchListResponse searchListResponse = search.execute();
            List<SearchResult> searchResults = searchListResponse.getItems();

            if (searchResults != null && !searchResults.isEmpty()) {
                String videoId = searchResults.get(0).getId().getVideoId();
                String trackURL = "https://youtube.com/watch?v=" + videoId;


                loadAndPlay(event.getChannel().asTextChannel(), trackURL);


            }else {
                event.getChannel().sendMessage("No results").queue();
            }


        }catch (IOException exception){
            event.getChannel().sendMessage("An error occurred").queue();
            exception.printStackTrace();
        }


    }

    private void loadAndPlay(final TextChannel channel, final String trackUrl){
        GuildMusicManager guildMusicManager = getServerAudioPlayer(channel.getGuild());

        playerManager.loadItemOrdered(guildMusicManager, trackUrl, new AudioLoadResultHandler(){
            @Override
            public void trackLoaded(AudioTrack track){

                play(channel.getGuild(), guildMusicManager, track);
                channel.sendMessage("Adding to queue " + track.getInfo().title).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                AudioTrack firstTrack = audioPlaylist.getSelectedTrack();
                if(firstTrack == null){
                    firstTrack = audioPlaylist.getTracks().get(0);
                }

                channel.sendMessage("Adding to queue " + firstTrack.getInfo().title).queue();

                play(channel.getGuild(), guildMusicManager, firstTrack);
            }

            @Override
            public void noMatches() {

                channel.sendMessage("Not found").queue();

            }

            @Override
            public void loadFailed(FriendlyException e) {
                channel.sendMessage("Not found").queue();
            }
        });


    }

    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track){

        connectToFirstVoiceChannel(guild.getAudioManager());
        musicManager.scheduler.queue(track);

    }

    private static void connectToFirstVoiceChannel(AudioManager audioManager){
        if (!audioManager.isConnected()){
            for (VoiceChannel channel : audioManager.getGuild().getVoiceChannels()){
                audioManager.openAudioConnection(channel);
                break;
            }
        }
    }





}

