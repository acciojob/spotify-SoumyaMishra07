package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User newUser = new User(name, mobile);
        users.add(newUser);
        return newUser;
    }

    public Artist createArtist(String name) {
        Artist newArtist = new Artist(name);
        artists.add(newArtist);
        return newArtist;
    }

    public Album createAlbum(String title, String artistName) {
        Artist artist = artists.stream()
                .filter(a -> a.getName().equals(artistName))
                .findFirst()
                .orElseGet(() -> createArtist(artistName));
        Album newAlbum = new Album(title);
        artistAlbumMap.computeIfAbsent(artist, k -> new ArrayList<>()).add(newAlbum);
        albums.add(newAlbum);
        return newAlbum;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        Album album = albums.stream()
                .filter(a -> a.getTitle().equals(albumName))
                .findFirst()
                .orElseThrow(() -> new Exception("Album does not exist"));
        Song newSong = new Song(title, length);
        albumSongMap.computeIfAbsent(album, k -> new ArrayList<>()).add(newSong);
        songs.add(newSong);
        return newSong;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = users.stream()
                .filter(u -> u.getMobile().equals(mobile))
                .findFirst()
                .orElseThrow(() -> new Exception("User does not exist"));
        List<Song> songsWithLength = songs.stream()
                .filter(s -> s.getLength() == length)
                .collect(Collectors.toList());
        Playlist newPlaylist = new Playlist(title);
        playlistSongMap.put(newPlaylist, songsWithLength);
        playlistListenerMap.put(newPlaylist, Collections.singletonList(user));
        creatorPlaylistMap.put(user, newPlaylist);
        userPlaylistMap.computeIfAbsent(user, k -> new ArrayList<>()).add(newPlaylist);
        playlists.add(newPlaylist);
        return newPlaylist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = users.stream()
                .filter(u -> u.getMobile().equals(mobile))
                .findFirst()
                .orElseThrow(() -> new Exception("User does not exist"));
        List<Song> songsWithTitles = songs.stream()
                .filter(s -> songTitles.contains(s.getTitle()))
                .collect(Collectors.toList());
        Playlist newPlaylist = new Playlist(title);
        playlistSongMap.put(newPlaylist, songsWithTitles);
        playlistListenerMap.put(newPlaylist, Collections.singletonList(user));
        creatorPlaylistMap.put(user, newPlaylist);
        userPlaylistMap.computeIfAbsent(user, k -> new ArrayList<>()).add(newPlaylist);
        playlists.add(newPlaylist);
        return newPlaylist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = users.stream()
                .filter(u -> u.getMobile().equals(mobile))
                .findFirst()
                .orElseThrow(() -> new Exception("User does not exist"));
        Playlist playlist = playlists.stream()
                .filter(p -> p.getTitle().equals(playlistTitle))
                .findFirst()
                .orElseThrow(() -> new Exception("Playlist does not exist"));
        List<User> listeners = playlistListenerMap.getOrDefault(playlist, new ArrayList<>());
        if (!listeners.contains(user)) {
            listeners.add(user);
            playlistListenerMap.put(playlist, listeners);
        }
        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = users.stream()
                .filter(u -> u.getMobile().equals(mobile))
                .findFirst()
                .orElseThrow(() -> new Exception("User does not exist"));
        Song song = songs.stream()
                .filter(s -> s.getTitle().equals(songTitle))
                .findFirst()
                .orElseThrow(() -> new Exception("Song does not exist"));
        if (!songLikeMap.containsKey(song) || !songLikeMap.get(song).contains(user)) {
            song.setLikes(song.getLikes() + 1);
            songLikeMap.computeIfAbsent(song, k -> new ArrayList<>()).add(user);
            Artist artist = artists.stream()
                    .filter(a -> a.getName().equals(song.getArtist()))
                    .findFirst()
                    .orElse(null);
            if (artist != null) {
                artist.setLikes(artist.getLikes() + 1);
            }
        }
        return song;
    }

    public String mostPopularArtist() {
        Artist popularArtist = artists.stream()
                .max(Comparator.comparingInt(Artist::getLikes))
                .orElse(null);
        return popularArtist != null ? popularArtist.getName() : null;
    }

    public String mostPopularSong() {
        Song popularSong = songs.stream()
                .max(Comparator.comparingInt(Song::getLikes))
                .orElse(null);
        return popularSong != null ? popularSong.getTitle() : null;
    }
}
