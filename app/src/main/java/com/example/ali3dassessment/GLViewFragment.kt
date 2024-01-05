package com.example.ali3dassessment

import android.hardware.Sensor
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.osmdroid.config.Configuration


class GLViewFragment: Fragment(R.layout.frag_glview) {

    lateinit var glView: OpenGLView

    // Declaring our sensor variables as attributes of the Main Activity
    private var accel: Sensor? = null
    private var magField: Sensor? = null

    // Declaring the FloatArrays and a Matrix for usage
    private var accelValues = FloatArray(3)
    private var magValue = FloatArray(3)
    private val orientationMatrix = FloatArray(16)
    private val remappedMatrix = FloatArray(16)


    private var radion: Double = 0.0
    private var negativeDZ: Float = 0.0f
    private var negativeDX: Float = 0.0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Configuration.getInstance()
            .load(context, PreferenceManager.getDefaultSharedPreferences(requireContext()));
        glView = OpenGLView(requireContext())

        return glView
    }


}
