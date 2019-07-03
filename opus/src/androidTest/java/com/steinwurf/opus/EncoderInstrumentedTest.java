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
public class EncoderInstrumentedTest {
    @Test
    public void initEncoder() {
        Encoder encoder = new Encoder(16000,2, Encoder.Application.VOIP);

        Assert.assertEquals(Bandwidth.FULL_BAND, encoder.getBandwidth());
        Assert.assertEquals(Bandwidth.FULL_BAND, encoder.getMaxBandwidth());
        encoder.setMaxBandwidth(Bandwidth.MEDIUM_BAND);
        Assert.assertEquals(Bandwidth.MEDIUM_BAND, encoder.getMaxBandwidth());

        encoder.setBandwidth(Bandwidth.MEDIUM_BAND);
        // Not sure why bandwidth still FULL_BAND?
        Assert.assertEquals(Bandwidth.FULL_BAND, encoder.getBandwidth());

        Assert.assertEquals(Encoder.Signal.AUTO, encoder.getSignal());
        encoder.setSignal(Encoder.Signal.VOICE);
        Assert.assertEquals(Encoder.Signal.VOICE, encoder.getSignal());

        Assert.assertEquals(56000, encoder.getBitrate());
        encoder.setBitrate(28000);
        Assert.assertEquals(28000, encoder.getBitrate());

        Assert.assertEquals(9, encoder.getComplexity());
        encoder.setComplexity(4);
        Assert.assertEquals(4, encoder.getComplexity());

        Assert.assertFalse(encoder.hasInBandFEC());
        encoder.enableInBandFEC(true);
        Assert.assertTrue(encoder.hasInBandFEC());

        Assert.assertEquals(0, encoder.getPacketLossPercentage());
        encoder.setPacketLossPercentage(50);
        Assert.assertEquals(50, encoder.getPacketLossPercentage());

        Assert.assertTrue(encoder.isPredictionEnabled());
        encoder.enablePrediction(false);
        Assert.assertFalse(encoder.isPredictionEnabled());

        Assert.assertFalse(encoder.inDTX());

        encoder.resetState();
    }
}
