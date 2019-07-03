package com.steinwurf.opus;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class DecoderInstrumentedTest {
    @Test
    public void initDecoder() {
        Decoder decoder = new Decoder(16000,2);

        Assert.assertNull(decoder.getBandwidth());
        Assert.assertEquals(0, decoder.getSampleRate());
        Assert.assertEquals(0, decoder.getLastPacketDuration());
        Assert.assertEquals(0, decoder.getGain());

        decoder.setGain((short)100);
        Assert.assertEquals(100, decoder.getGain());
        Assert.assertEquals(0, decoder.getPitch());

        decoder.resetState();
    }
}
