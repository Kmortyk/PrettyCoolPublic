#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D u_texture;

uniform float outLineSize  = 0.02;
uniform vec4  outLineColor = vec4(1.0, 1.0, 1.0, 1.0);

varying vec4 v_color;
varying vec2 v_texCoord;

void main() {
    vec4 total = vec4(0.0);
    vec4 grabPixel;

    vec2 size = textureSize2D(u_texture, 0);
    float width = float(size.x);
    float height = float(size.y);

    total += texture2D(u_texture, v_texCoord + vec2(-1.0 / width, -1.0 / height));
    total += texture2D(u_texture, v_texCoord + vec2(1.0 / width, -1.0 / height));
    total += texture2D(u_texture, v_texCoord + vec2(1.0 / width, 1.0 / height));
    total += texture2D(u_texture, v_texCoord + vec2(-1.0 / width, 1.0 / height));

    grabPixel = texture2D(u_texture, v_texCoord + vec2(0.0, -1.0 / height));
    total += grabPixel * 2.0;

    grabPixel = texture2D(u_texture, v_texCoord + vec2(0.0, 1.0 / height));
    total += grabPixel * 2.0;

    grabPixel = texture2D(u_texture, v_texCoord + vec2(-1.0 / width, 0.0));
    total += grabPixel * 2.0;

    grabPixel = texture2D(u_texture, v_texCoord + vec2(1.0 / width, 0.0));
    total += grabPixel * 2.0;

    grabPixel = texture2D(u_texture, v_texCoord);
    total += grabPixel * 4.0;

    total *= 1.0 / 16.0;

    gl_FragColor = total;
}