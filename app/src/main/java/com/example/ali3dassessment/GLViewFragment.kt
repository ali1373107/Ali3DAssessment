package com.example.ali3dassessment
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.VolumeShaper
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import freemap.openglwrapper.GLMatrix

class GLViewFragment : Fragment(R.layout.frag_glview), SensorEventListener {
    private var cameraPermission = false
    private var surfaceTexture: SurfaceTexture? = null
    private lateinit var glView: OpenGLView
    private var accel: Sensor? = null
    private var magField: Sensor? = null

    // Arrays to hold the current acceleration and magnetic field sensor values
    var accelValues = FloatArray(3)
    var magFieldValues = FloatArray(3)
    var orientationMatrix = FloatArray(16)
    var remappedMatrix = FloatArray(16)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        glView = OpenGLView(requireContext()){
            surfaceTexture = it
        }
        return glView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sMgr = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        accel = sMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magField = sMgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        sMgr.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI)
        sMgr.registerListener(this, magField, SensorManager.SENSOR_DELAY_UI)
        requestPermissions()

    }

    private fun startCamera(): Boolean {
        if (cameraPermission) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
            cameraProviderFuture.addListener({
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {

                    val surfaceProvider: (SurfaceRequest) -> Unit = { request ->
                        val resolution = request.resolution
                        surfaceTexture?.apply {
                            setDefaultBufferSize(resolution.width, resolution.height)
                            val surface = Surface(this)
                            request.provideSurface(
                                surface,
                                ContextCompat.getMainExecutor(requireContext())
                            ) { }

                        }
                    }
                    it.setSurfaceProvider(surfaceProvider)

                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview)

                } catch (e: Exception) {
                    Log.e("OpenGL01Log", e.stackTraceToString())
                }
            }, ContextCompat.getMainExecutor(requireContext()))
            return true
        } else {
            return false
        }
    }

    private fun requestPermissions() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                0
            )
        } else {
            cameraPermission = true
            startCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0 && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            cameraPermission = true
            startCamera()
        } else {
            AlertDialog.Builder(requireContext()).setPositiveButton("OK", null)
                .setMessage("CAMERA permission denied").show()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Leave blank
    }

    override fun onSensorChanged(ev: SensorEvent) {
        // Test which sensor has been detected.
        if (ev.sensor == accel) {
            // Copy the current values into the acceleration array
            accelValues = ev.values.copyOf()
        } else if (ev.sensor == magField) {
            // Copy the current values into the magnetic field array
            magFieldValues = ev.values.copyOf()
        }

        // Calculate the matrix
        SensorManager.getRotationMatrix(orientationMatrix, null, accelValues, magFieldValues)
        SensorManager.remapCoordinateSystem(
            orientationMatrix,
            SensorManager.AXIS_Y,
            SensorManager.AXIS_MINUS_X,
            remappedMatrix
        )
        glView.orientationMatrix = GLMatrix(remappedMatrix)
    }
}