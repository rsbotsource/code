package org.powerbot.game.loader.applet;

import java.net.URL;

/**
 * @author Timer
 */
public class AudioClip implements java.applet.AudioClip {
	public static final short STATE_STOPPED = 0;
	public static final short STATE_PLAYING = 1;
	public static final short STATE_LOOPING = 2;
	private final URL sourceURL;
	private short audioClipState;

	public AudioClip(final URL sourceURL) {
		this.sourceURL = sourceURL;
		audioClipState = 0;
	}

	public short getAudioClipState() {
		return audioClipState;
	}

	public URL getURL() {
		return sourceURL;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof AudioClip)) {
			return false;
		}
		final AudioClip ac = (AudioClip) obj;
		return ac.getAudioClipState() == audioClipState && ac.getURL().equals(sourceURL);
	}

	public void play() {
		audioClipState = STATE_PLAYING;
	}

	public void loop() {
		audioClipState = STATE_LOOPING;
	}

	public void stop() {
		audioClipState = STATE_STOPPED;
	}
}