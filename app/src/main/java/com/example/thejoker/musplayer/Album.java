package com.example.thejoker.musplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by The Joker on 27/10/2015.
 */
public class Album extends Fragment
{
    private ArrayList<Song> songList;
    private ListView songView;
    private Context context;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.albums, container, false);
        songView = (ListView)rootView.findViewById(R.id.albumtextView);
        songList = new ArrayList<Song>();
        context = getActivity();
        getSongList(context);
        Collections.sort(songList, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getAlbum().compareTo(b.getAlbum());
            }
        });
        AlbumAdapter albumAdt = new AlbumAdapter(context,songList);
        songView.setAdapter(albumAdt);
        return rootView;
    }
    public void getSongList(Context context)
    {
        ContentResolver musicResolver = context.getContentResolver();
        Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if(musicCursor!=null && musicCursor.moveToFirst())
        {
            int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);
            int albumtitle = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ALBUM);
            do {
                long thisId = musicCursor.getLong(idColumn);
                String thisTitle = musicCursor.getString(titleColumn);
                String thisArtist = musicCursor.getString(artistColumn);
                String thisAlbum = musicCursor.getString(albumtitle);
                int i, f = 0;
                for (i = 0; i < songList.size(); i++) {
                    if (thisAlbum.equals(songList.get(i).getAlbum())) {
                        f = 1;
                    }
                }
                if (f == 0) {
                    songList.add(new Song(thisId, thisTitle, thisArtist, thisAlbum));
                }
            }
            while (musicCursor.moveToNext());
        }
    }
}
