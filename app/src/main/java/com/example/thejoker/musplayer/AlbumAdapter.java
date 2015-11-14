package com.example.thejoker.musplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by The Joker on 31/10/2015.
 */
public class AlbumAdapter extends BaseAdapter
{
    private ArrayList<Song> songs;
    private LayoutInflater songInf;
    public AlbumAdapter(Context c, ArrayList<Song> theSongs)
    {
        songs = theSongs;
        songInf = LayoutInflater.from(c);
    }
    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout songLay = (LinearLayout)songInf.inflate(R.layout.album, parent, false);
        TextView songView = (TextView)songLay.findViewById(R.id.song);
        Song currSong = songs.get(position);
        songView.setText(currSong.getAlbum());
        songLay.setTag(position);
        return songLay;
    }
}
