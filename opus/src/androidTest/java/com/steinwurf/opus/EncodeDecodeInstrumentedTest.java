package com.steinwurf.opus;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class EncodeDecodeInstrumentedTest {

    private ShortBuffer getTestData(String filename) throws IOException
    {
        InputStream is = getClass().getResourceAsStream(filename);
        Assert.assertNotNull(is);
        int size = is.available();
        byte[] buffer = new byte[size];
        int offset = 0;
        int length = buffer.length;
        while (is.available() != 0)
        {
            int bytes = is.read(buffer, offset, length);
            offset += bytes;
            length -= bytes;
        }
        Assert.assertEquals(size, offset);
        Assert.assertEquals(0, is.available());
        return ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
    }

    @Test
    public void encodeDecode() {

        ShortBuffer audio = null;
        try {
            audio = getTestData("pcm_mono_16_bit_16kHz.dat");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(audio);

        int sampleRate = 16000;
        Decoder decoder = new Decoder(sampleRate,1);
        Encoder encoder = new Encoder(sampleRate,1, Encoder.Application.AUDIO);

        byte[] encoded = new byte[4000];
        short[] result = new short[audio.remaining()];
        int offset = 0;

        int frameSize = encoder.calculateFrameSize(2500);
        short[] frame = new short[frameSize];

        while (audio.hasRemaining())
        {
            if (audio.remaining() < frameSize) {
                break;
            }
            audio.get(frame);
            int encodedSize = encoder.encode(frame, frameSize, encoded);

            int decoded = decoder.decode(encoded, 0, encodedSize, result, offset, result.length - offset, frameSize, false);

            Assert.assertNotEquals(0, decoded);
            offset += decoded;
        }
        int not0s = 0;
        for (short sample : result) {
            if (sample != 0)
            {
                not0s += 1;
            }
        }
        Assert.assertEquals(119434, not0s);
    }
}
