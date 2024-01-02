attribute vec4 aVertex;
uniform mat4 uView, uProjection;
void main(void)
{
    gl_Position = uProjection * uView * aVertex;
}