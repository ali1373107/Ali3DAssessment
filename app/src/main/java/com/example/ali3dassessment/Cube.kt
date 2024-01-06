package com.example.ali3dassessment


import freemap.openglwrapper.GPUInterface
import freemap.openglwrapper.OpenGLUtils
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Cube(val x:Float, val y:Float, val z:Float) {
    val vertexBuf: FloatBuffer
    val indexBuf: ShortBuffer
    val vertexAndColourBuf: FloatBuffer

    init {
        vertexBuf = OpenGLUtils.makeFloatBuffer(
            floatArrayOf(
                0f, +0.5f, 0f,
                +0.5f, +0.5f, 0f,
                +0.5f, +0.5f, +0.5f,
                0f, +0.5f, +0.5f,
                0f, 0f, 0f,
                +0.5f, 0f, 0f,
                +0.5f, 0f, +0.5f,
                0f, 0f, +0.5f
            )
        )
        vertexAndColourBuf= OpenGLUtils.makeFloatBuffer(
            floatArrayOf(
                x,y+0.5f,z,
                0f,1f,0f,
                x+0.5f,y+0.5f,z,
                1f,1f,0f,
                x+0.5f,y+0.5f,z+0.5f,
                1f,1f,0f,
                x,y+0.5f,z+0.5f,
                1f,1f,0f,
                x,y,z,
                1f,0f,0f,
                x+0.5f,y,z,
                0f,1f,0f,
                x+0.5f,y,z+0.5f,
                0f,0f,1f,
                x,y,z+0.5f,
                0f,1f,1f
            )
        )

        indexBuf = OpenGLUtils.makeShortBuffer(
            shortArrayOf(
                0, 1, 2, 2, 3, 0,//top face
                4, 5, 6, 6, 7, 4,//bottom face
                0, 4, 7, 7, 3, 0,//left face
                1, 5, 6, 6, 2, 1,//right face
                3, 7, 6, 6, 2, 3,//back face
                4, 5, 1, 1, 0, 4  //front face
            ))
    }
    fun render(gpu: GPUInterface, refAttrib: Int){
        gpu.drawIndexedBufferedData(vertexBuf,indexBuf,0, refAttrib)
    }
    fun renderMulti(gpu:GPUInterface) {
        val stride = 24 //because one record contains vertices (12 bytes) and columns (12 bytes)
        val attrVarRef = gpu.getAttribLocation("aVertex")
        val colourVarRef = gpu.getAttribLocation("aColour")
        gpu.specifyBufferedDataFormat(attrVarRef,vertexAndColourBuf,stride,0)
        gpu.specifyBufferedDataFormat(colourVarRef,vertexAndColourBuf,stride,3)
        gpu.drawElements(indexBuf)
    }
}