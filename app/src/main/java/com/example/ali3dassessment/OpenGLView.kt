package com.example.ali3dassessment
import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import freemap.openglwrapper.Camera
import freemap.openglwrapper.GLMatrix
import freemap.openglwrapper.GPUInterface
import freemap.openglwrapper.OpenGLUtils
import java.io.IOException
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

// Our GLSurfaceView for rendering the 3D world.

class OpenGLView(ctx:Context) :GLSurfaceView(ctx), GLSurfaceView.Renderer {

    init {
        setEGLContextClientVersion(2) // use GL ES version 2
        setRenderer(this)
    }
    val gpu = GPUInterface("default shader")
    var fbuf: FloatBuffer?=null
    val blue = floatArrayOf(0f,0f,1f,1f)
    val yellow = floatArrayOf(1f,1f,0f,1f)
    val viewMatrix = GLMatrix()
    val projectionMatrix = GLMatrix()
    val camera = Camera(0f,0f,0f)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.1f, 0.0f, 1.0f, 1.0f)
        GLES20.glClearDepthf(1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        // Sets the background colour

        try {
            val success = gpu.loadShaders(context.assets, "vertex.glsl", "fragment.glsl")
            if (!success) {
                Log.d("opengl01Load", gpu.lastShaderError)
            }
        }catch (e: IOException) {
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
    }
    override fun onDrawFrame(gl: GL10?) {
        // Clears any previous settings from the previous frame
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        val refAttrib = gpu.getAttribLocation("aVertex")
        val refUColour = gpu.getUniformLocation("uColour")
        val ref_uViewMatrix = gpu.getUniformLocation("uView")
        val ref_uProjMatrix = gpu.getUniformLocation("uProjection")

        fbuf?.apply {
            viewMatrix.setAsIdentityMatrix()
            viewMatrix.rotateAboutAxis(-camera.rotation,'y')
            //viewMatrix.translate(0.0f,0.0f, -2.0f)
            viewMatrix.translate(-camera.position.x,-camera.position.y,-camera.position.z)
            gpu.setUniform4FloatArray(refUColour,blue)
            gpu.specifyBufferedDataFormat(refAttrib,this,0)
            gpu.drawBufferedTriangles(0,3)

            gpu.setUniform4FloatArray(refUColour,yellow)
            gpu.drawBufferedTriangles(3,3)

            gpu.sendMatrix(ref_uProjMatrix, projectionMatrix)
            gpu.sendMatrix(ref_uViewMatrix, viewMatrix)


        }
    }
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // Setting up the view port
        GLES20.glViewport(0, 0, width, height)

        // Calculating the projection matrix each time the dimensions of the 'GLSurfaceView' change
        val hfov = 60.0f // Horizontal field of view
        val aspect : Float = width.toFloat() / height
        projectionMatrix.setProjectionMatrix(hfov, aspect, 0.001f, 100f)
    }
}

