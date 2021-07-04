#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform sampler2D u_texture;

const float offset = 6.0 / 128.0;

varying vec4 v_color;
varying vec2 v_texCoord;

void main() {

    vec4 col = texture2D(u_texture, v_texCoord);

    if (col.a > 0.5) {
        gl_FragColor = col;
    } else {
        float a = texture2D(u_texture, vec2(v_texCoord.x + offset, v_texCoord.y)).a +
        texture2D(u_texture, vec2(v_texCoord.x, v_texCoord.y - offset)).a +
        texture2D(u_texture, vec2(v_texCoord.x - offset, v_texCoord.y)).a +
        texture2D(u_texture, vec2(v_texCoord.x, v_texCoord.y + offset)).a;
        if (col.a < 1.0 && a > 0.0)
             { gl_FragColor = vec4(245.0 / 255.0, 55.0 / 255.0, 77.0 / 255.0, 1); }
        else { gl_FragColor = col; }
    }

}