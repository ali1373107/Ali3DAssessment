package com.example.ali3dassessment

import android.Manifest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PoiFragment : Fragment(R.layout.poi_item) {

    val poiViewModel: PoiViewModel by viewModels()
    private lateinit var poiAdapter: POIAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_poi, container, false)

        val poiRecyclerView = view.findViewById<RecyclerView>(R.id.poiRecyclerView)
        poiAdapter = POIAdapter(emptyList())
        poiRecyclerView.adapter = poiAdapter
        poiRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        val filterButton = view.findViewById<Button>(R.id.filterButton)
        val filterEditText = view.findViewById<EditText>(R.id.filterEditText)

        filterButton.setOnClickListener {
            val userInput = filterEditText.text.toString()
            loadAndDisplayPoisFromDatabase(userInput)
        }

        // Load and display all POIs initially
        loadAndDisplayPoisFromDatabase("")

        return view
    }

    private fun loadAndDisplayPoisFromDatabase(type: String) {
        poiViewModel.getPoisByType(type).observe(viewLifecycleOwner, { pois ->
            // Update RecyclerView with data from the database
            poiAdapter.updateData(pois)
        })
    }
}
