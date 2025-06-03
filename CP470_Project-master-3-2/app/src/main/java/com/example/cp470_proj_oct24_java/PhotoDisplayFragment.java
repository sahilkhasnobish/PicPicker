package com.example.cp470_proj_oct24_java;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PhotoDisplayFragment extends Fragment {

    private static final String TAG= PhotoDisplayFragment.class.getSimpleName();
    private static final String ARG_VALID_PHOTOS = "validPhotos";
    private ArrayList<Uri> validPhotos;

    public PhotoDisplayFragment() {
        // Required empty public constructor
    }

    public static PhotoDisplayFragment newInstance(ArrayList<Uri> validPhotos) {
        PhotoDisplayFragment fragment = new PhotoDisplayFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_VALID_PHOTOS, validPhotos);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"inside onCreate");

        if (getArguments() != null) {
            validPhotos = getArguments().getParcelableArrayList(ARG_VALID_PHOTOS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_display, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.photos_recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        PhotosAdapter adapter = new PhotosAdapter(getContext(), validPhotos);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
