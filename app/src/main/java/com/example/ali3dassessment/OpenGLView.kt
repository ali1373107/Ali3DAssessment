package com.example.ali3dassessment
import android.app.Application
import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.ali3dassessment.geo.LonLat
import com.example.ali3dassessment.geo.SphericalMercatorProjection
import com.google.common.util.concurrent.ListenableFuture
import freemap.openglwrapper.Camera
import freemap.openglwrapper.GLMatrix
import freemap.openglwrapper.GPUInterface
import freemap.openglwrapper.OpenGLUtils
import java.io.IOException
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class OpenGLView(ctx:Context, val lifecycleOwner: LifecycleOwner,
                 val textureAvailableCallback: (SurfaceTexture) ->Unit) :GLSurfaceView(ctx),GLSurfaceView.Renderer{
    private var lat: Double = 0.0
    private var lon: Double = 0.0
    var proj = SphericalMercatorProjection()

    private lateinit var poiViewModel: PoiViewModel

    init {
        setEGLContextClientVersion(2)
        setRenderer(this)
        Log.d("OpemGLBasic"," ${lat}${lon}")

    }


    val gpu = GPUInterface("color shader")
    var fbuf :FloatBuffer? = null
    var indexfbuf: ShortBuffer? = null

    val gpuTexture = GPUInterface("texture")

    var traingle: Triangle = Triangle(2f,1f,1f)


    var viewMatrix = GLMatrix()
    val projectionMatrix = GLMatrix()
    val camera = Camera(95.33094371575862F,0f,107.97990605444647F)

    var cameraFeedSurfaceTexure: SurfaceTexture? = null
    // For representing the current orientation matrix that will be changed to the correct version
    // when a new sensor reading is obtained
    var orientationMatrix = GLMatrix()
    // setup code to run when the OpenGL view is first created.

    fun update() {
        Handler(Looper.getMainLooper()).post {
            if (!::poiViewModel.isInitialized) {
                poiViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(context.applicationContext as Application)
                    .create(PoiViewModel::class.java)

                poiViewModel.lat1.observe(lifecycleOwner, latObserver)
                poiViewModel.lon1.observe(lifecycleOwner, lonObserver)
            }
        }
    }

    fun getdata() {
        //getting live list from view model to display triangle based on the poi lat and lon anter unprojing
        Handler(Looper.getMainLooper()).post {
            poiViewModel.allPois.observe(context as LifecycleOwner) { pois ->
                for(poi in pois){
                    Log.d("POISD","${poi.name}")
                }
            }
        }
    }
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig){

        update()
        getdata()

        Log.d("OpemGLBasic"," ${lat}${lon}")

        GLES20.glClearColor(0.0f,0.0f,0.0f,1.0f)
        GLES20.glClearDepthf(1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        val textureId = OpenGLUtils.genTexture()
        Log.d("OpemGLBasic","Texture id = $textureId")


        if(textureId !=0){
            OpenGLUtils.bindTextureToTextureUnit(textureId,GLES20.GL_TEXTURE0, OpenGLUtils.GL_TEXTURE_EXTERNAL_OES)
            cameraFeedSurfaceTexure = SurfaceTexture(textureId)
            textureAvailableCallback(cameraFeedSurfaceTexure!!)
        }
        try {

            val success2 = gpu.loadShaders(context.assets, "vertex1.glsl", "fragment1.glsl")
            if(!success2){
                Log.d("opengl01",gpu.lastShaderError)
            }
            val success3 = gpuTexture.loadShaders(context.assets,"vertexTexture.glsl","fragmentTexture.glsl")
            if(!success3){
                Log.d("OpenGLBasic","Shader error: ${gpu.lastShaderError}")
            }
            Log.d("OpenGLBasic","Shader status: $success2 $success3")
            gpuTexture.select()
            val refTextureUnit = gpuTexture.getUniformLocation("uTexture")
            Log.d("OpenGLBasic", "refTxtureUnit=$refTextureUnit")
            gpuTexture.setUniformInt(refTextureUnit,0)
        } catch (e: IOException) {
            Log.e("OpenGLBasic", e.stackTraceToString())
        }

        val vertices2 = floatArrayOf(
            -1f, 1f, 0f,
            -1f, -1f, 0f,
            1f, -1f, 0f,
            1f, 1f, 0f
        )
        fbuf = OpenGLUtils.makeFloatBuffer(vertices2)
        //val indices = shortArrayOf(0,1,2, 2,3,0)
        // val indices1 = shortArrayOf(0,1,2,2,3,0)
        indexfbuf = OpenGLUtils.makeShortBuffer(shortArrayOf(0,1,2,2,3,0))
        //  indexfbuf1 = OpenGLUtils.makeShortBuffer(shortArrayOf(0,1,3,3,1,2))
    }
    fun unprojLatLon(){

//log lon and lat to ee we getting correct data
        val p = LonLat(-1.40038000000000, 50.90832)
        val en = proj.project(p)
        println("Easting: " + en.easting + " Northing: " + en.northing)
        Log.d("opengllon","${lon}")
        Log.d("opengllat","${lat}")

    }
    //actual scene drawing shoild go here
    override fun onDrawFrame(gl: GL10?){
        getdata()
        //clear any previuse setting from previouse frame
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        //GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        Log.d("OpemGLBasic"," ${lat}${lon}")

        gpuTexture.select()
        val ref_aVertext2 = gpuTexture.getAttribLocation("aVertex")


        //only run code below if buffer is not null
        if (fbuf!= null  &&indexfbuf !=null){

            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
            cameraFeedSurfaceTexure?.updateTexImage()


            gpuTexture.drawIndexedBufferedData(fbuf!!,indexfbuf!!,0,ref_aVertext2)
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            viewMatrix.setAsIdentityMatrix()
            viewMatrix.rotateAboutAxis(-camera.rotation,'y')
            //viewMatrix.translate(0.0f,0.0f, -2.0f)
            viewMatrix.translate(-camera.position.x,-camera.position.y,-camera.position.z)

            // setting the view matrix to the identity matrix so that it has no effect initially.
            viewMatrix.setAsIdentityMatrix()

            // Initialise the view matrix to a clone of the orientation matrix
            viewMatrix = orientationMatrix.clone()

            // Method call to set the view matrix to the correct sensor matrix
            viewMatrix.correctSensorMatrix()

            unprojLatLon()


            gpu.select()
            val refUProj2 = gpu.getUniformLocation("uPerspMtx")
            val refUView2 =gpu.getUniformLocation("uMvMtx")
            // Log.d("OpenGLBasic", "uniforms: $refUProj2 $refUView2")
            gpu.sendMatrix(refUProj2 , projectionMatrix)
            gpu.sendMatrix(refUView2, viewMatrix)

            traingle.renderMulti(gpu)





        }
    }
    private val latObserver = Observer<Double> { newLat ->
        lat = newLat
        Log.d("MyTag111", "Updated Latitude in Activity: $lat")
    }

    private val lonObserver = Observer<Double> { newLon ->
        lon = newLon
        Log.d("MyTag111", "Updated Longitude in Activity: $lon")
    }
    //its called whenever the resolution changes (on a mobile device this ill occur when the device i rotated
    override fun onSurfaceChanged(unused: GL10, w: Int, h:Int){
        getdata()
        GLES20.glViewport(0,0,w,h)
        val hfov =60.0f
        val aspect: Float = w.toFloat()/h
        projectionMatrix.setProjectionMatrix(hfov,aspect,0.001f, 100f)
        Log.d("OpemGLBasic"," ${lat}${lon}")


    }
}

