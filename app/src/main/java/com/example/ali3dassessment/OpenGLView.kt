package com.example.ali3dassessment

import android.opengl.GLSurfaceView
import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.util.AttributeSet
import android.util.Log
import freemap.openglwrapper.Camera
import freemap.openglwrapper.GLMatrix
import freemap.openglwrapper.GPUInterface
import freemap.openglwrapper.OpenGLUtils
import java.io.IOException
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import javax.security.auth.callback.Callback

class OpenGLView(ctx:Context, val textureAvailableCallback: (SurfaceTexture) ->Unit) :GLSurfaceView(ctx),GLSurfaceView.Renderer{
    init {
        setEGLContextClientVersion(2)
        setRenderer(this)
    }

    val gpu = GPUInterface("default shader")
    var fbuf: FloatBuffer? = null
    var indexfbuf: ShortBuffer? = null

    val gpuTexture = GPUInterface("texture")
    val red = floatArrayOf(1f,0f,0f,1f)
    val pink = floatArrayOf(1f,0f,1f,1f)
    val blue = floatArrayOf(0f,0f,1f,1f)
    val yellow = floatArrayOf(1f,1f,0f,1f)
    val green = floatArrayOf(0f,1f,0f,1f)
    var viewMatrix = GLMatrix()
    val projectionMatrix = GLMatrix()
    val camera = Camera(0f,0f,0f)
    var cameraFeedSurfaceTexure: SurfaceTexture? = null
    // For representing the current orientation matrix that will be changed to the correct version
    // when a new sensor reading is obtained
    var orientationMatrix = GLMatrix()
    // setup code to run when the OpenGL view is first created.
    override fun onSurfaceCreated(unused: GL10, config: EGLConfig){
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
            val success = gpu.loadShaders(context.assets, "vertex.glsl", "fragment.glsl")
            if(!success){
                Log.d("opengl01",gpu.lastShaderError)
            }


            Log.d("OpenGLBasic","Shader status:$success")
            gpuTexture.select()
            val refTextureUnit = gpuTexture.getUniformLocation("uTexture")
            Log.d("OpenGLBasic", "refTxtureUnit=$refTextureUnit")
            gpuTexture.setUniformInt(refTextureUnit,0)
        } catch (e: IOException) {
            Log.e("OpenGLBasic", e.stackTraceToString())
        }
        val vertices = floatArrayOf(
            0f, 0f, -3f,
            1f, 0f, -3f,
            0.5f, 1f, -3f,
            -0.5f,0f,-6f,
            0.5f,0f,-6f,
            0f,1f,-6f
        )
        fbuf = OpenGLUtils.makeFloatBuffer(vertices)
        gpu.select()

        //val indices = shortArrayOf(0,1,2, 2,3,0)
        indexfbuf = OpenGLUtils.makeShortBuffer(shortArrayOf(0,1,2, 2,3,0))
        // val indices1 = shortArrayOf(0,1,2,2,3,0)
        //  indexfbuf1 = OpenGLUtils.makeShortBuffer(shortArrayOf(0,1,3,3,1,2))
    }
    override fun onDrawFrame(gl: GL10?){
        //clear any previuse setting from previouse frame
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        //GLES20.glDisable(GLES20.GL_DEPTH_TEST)

        gpuTexture.select()
        val ref_aVertext2 = gpuTexture.getAttribLocation("aVertex")


        //only run code below if buffer is not null
        if (fbuf != null){

            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
            cameraFeedSurfaceTexure?.updateTexImage()


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


            gpu.select()
            val refAttrib = gpu.getAttribLocation("aVertex")
            val refUColour = gpu.getUniformLocation("uColour")
            // Log.d("OpenGLBasic", "uniforms for gpu: $refAttrib $refUColour")
            val ref_uViewMatrix = gpu.getUniformLocation("uView")
            val ref_uProjMatrix = gpu.getUniformLocation("uProjection")
            gpu.setUniform4FloatArray(refUColour,blue)

            gpu.sendMatrix(ref_uProjMatrix, projectionMatrix)
            gpu.sendMatrix(ref_uViewMatrix, viewMatrix)
            //triangle
            gpu.specifyBufferedDataFormat(refAttrib,fbuf!!,0)
            gpu.setUniform4FloatArray(refUColour,blue)

        }
    }
    //its called whenever the resolution changes (on a mobile device this ill occur when the device i rotated
    override fun onSurfaceChanged(unused: GL10, w: Int, h:Int){
        GLES20.glViewport(0,0,w,h)
        val hfov =60.0f
        val aspect: Float = w.toFloat()/h
        projectionMatrix.setProjectionMatrix(hfov,aspect,0.001f, 100f)

    }
}