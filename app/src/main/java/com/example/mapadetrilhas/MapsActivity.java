package com.example.mapadetrilhas;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.mapadetrilhas.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button botaoVoltar = findViewById(R.id.button_voltar_maps);
        botaoVoltar.setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        MarkerOptions suaPosicao_markerOptions = new MarkerOptions();
        suaPosicao_markerOptions.position(new LatLng(-12.94825, -38.41334));
        suaPosicao_markerOptions.title("Sua Posição");
        suaPosicao_markerOptions.snippet("");

        Marker myMarker=mMap.addMarker(suaPosicao_markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom
                (new LatLng(-12.94825, -38.41334),15));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.button_voltar_maps) {
            finish();
        }
    }
}