@require {
	vec4 f_Color;
}

const vec3 ISQRT3 = vec3(0.57735, 0.57735, 0.57735);

@mixin celShade {

	float brightness = 1.0;
	
	if (f_Color.a < .77)
		brightness = 0.85;
	if (f_Color.a < .75)
		brightness = 0.8;
	if (f_Color.a < .55)
		brightness = 0.70;
	
	float h = brightness - 0.8;
	float cosHue = cos(h);
	
	vec3 celShadeColor = f_Color.xyz;
	celShadeColor = vec3(celShadeColor * cosHue + cross(ISQRT3, celShadeColor) * sin(h) + ISQRT3 * dot(ISQRT3, celShadeColor) * (1.0 - cosHue));
	
	celShadeColor *= brightness;
}