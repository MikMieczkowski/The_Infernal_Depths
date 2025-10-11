#version 120

#ifdef GL_ES
    #define PRECISION mediump
    precision PRECISION float;
    precision PRECISION int;
#else
    #define PRECISION
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform float u_amount = 1.0;
uniform vec4 u_color;

void main()
{
	vec4 pixel = texture2D(u_texture, v_texCoords);
	gl_FragColor = vec4((u_color * u_amount + pixel * (1.0 - u_amount)).rgb, pixel.a);
}