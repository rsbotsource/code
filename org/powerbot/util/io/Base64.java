package org.powerbot.util.io;

import java.util.Arrays;

/**
 * An efficient, lightweight Base64 utility for encoding and decoding byte arrays.
 *
 * @author Timer
 */
public class Base64 {
	private static final char[] CHAR_ARRAY = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
	private static final int[] INT_ARRAY = new int[256];

	static {
		Arrays.fill(INT_ARRAY, -1);
		for (int i = 0, iS = CHAR_ARRAY.length; i < iS; i++) {
			INT_ARRAY[CHAR_ARRAY[i]] = i;
		}
		INT_ARRAY['='] = 0;
	}

	public static byte[] encode(final byte[] bytes) {
		final int inputLength = bytes != null ? bytes.length : 0;
		if (inputLength == 0) {
			return new byte[0];
		}
		final int evenLength = inputLength / 3 * 3;
		final int charLength = (inputLength - 1) / 3 + 1 << 2;
		final int outputLength = charLength + ((charLength - 1) / 76 << 1);
		final byte[] encodedBytes = new byte[outputLength];
		for (int decodedIndex = 0, encodedIndex = 0, entrySize = 0; decodedIndex < evenLength; ) {
			final int assembledInteger = (bytes[decodedIndex++] & 0xff) << 16 | (bytes[decodedIndex++] & 0xff) << 8 | bytes[decodedIndex++] & 0xff;
			encodedBytes[encodedIndex++] = (byte) CHAR_ARRAY[assembledInteger >>> 18 & 0x3f];
			encodedBytes[encodedIndex++] = (byte) CHAR_ARRAY[assembledInteger >>> 12 & 0x3f];
			encodedBytes[encodedIndex++] = (byte) CHAR_ARRAY[assembledInteger >>> 6 & 0x3f];
			encodedBytes[encodedIndex++] = (byte) CHAR_ARRAY[assembledInteger & 0x3f];
			if (++entrySize == 19 && encodedIndex < outputLength - 2) {
				encodedBytes[encodedIndex++] = '\r';
				encodedBytes[encodedIndex++] = '\n';
				entrySize = 0;
			}
		}
		final int padding = inputLength - evenLength;
		if (padding > 0) {
			final int assembledInteger = (bytes[evenLength] & 0xff) << 10 | (padding == 2 ? (bytes[inputLength - 1] & 0xff) << 2 : 0);
			encodedBytes[outputLength - 4] = (byte) CHAR_ARRAY[assembledInteger >> 12];
			encodedBytes[outputLength - 3] = (byte) CHAR_ARRAY[assembledInteger >>> 6 & 0x3f];
			encodedBytes[outputLength - 2] = padding == 2 ? (byte) CHAR_ARRAY[assembledInteger & 0x3f] : (byte) '=';
			encodedBytes[outputLength - 1] = '=';
		}
		return encodedBytes;
	}

	public static byte[] decode(final byte[] encodedBytes) {
		final int inputLength = encodedBytes.length;
		int separatorCount = 0;
		for (final byte encodedByte : encodedBytes) {
			if (INT_ARRAY[encodedByte & 0xff] < 0) {
				separatorCount++;
			}
		}
		if ((inputLength - separatorCount) % 4 != 0) {
			return null;
		}
		int padding = 0;
		for (int i = inputLength; i > 1 && INT_ARRAY[encodedBytes[--i] & 0xff] <= 0; ) {
			if (encodedBytes[i] == '=') {
				padding++;
			}
		}
		final int outputLength = ((inputLength - separatorCount) * 6 >> 3) - padding;
		final byte[] decodedBytes = new byte[outputLength];
		for (int encodeIndex = 0, decodeIndex = 0; decodeIndex < outputLength; ) {
			int assembledInt = 0;
			for (int validIndex = 0; validIndex < 4; validIndex++) {
				final int c = INT_ARRAY[encodedBytes[encodeIndex++] & 0xff];
				if (c >= 0) {
					assembledInt |= c << 18 - validIndex * 6;
				} else {
					validIndex--;
				}
			}
			decodedBytes[decodeIndex++] = (byte) (assembledInt >> 16);
			if (decodeIndex < outputLength) {
				decodedBytes[decodeIndex++] = (byte) (assembledInt >> 8);
				if (decodeIndex < outputLength) {
					decodedBytes[decodeIndex++] = (byte) assembledInt;
				}
			}
		}
		return decodedBytes;
	}
}
