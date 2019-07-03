package com.steinwurf.opus;

import java.util.Arrays;

import static com.steinwurf.opus.ReturnValueCheck.check;

/**
 * Missing
 * OPUS_GET_APPLICATION
 * OPUS_GET_FORCE_CHANNELS
 * OPUS_GET_DTX
 * OPUS_GET_VBR
 * OPUS_GET_VBR_CONSTRAINT
 * OPUS_GET_LOOKAHEAD
 * OPUS_GET_LSB_DEPTH
 * OPUS_GET_EXPERT_FRAME_DURATION
 * CELT_GET_MODE
 *
 * OPUS_GET_VOICE_RATIO
 * OPUS_SET_APPLICATION
 * OPUS_SET_FORCE_CHANNELS
 * OPUS_SET_DTX
 * OPUS_SET_VBR
 * OPUS_SET_VOICE_RATIO
 * OPUS_SET_VBR_CONSTRAINT
 * OPUS_SET_LSB_DEPTH
 * OPUS_SET_EXPERT_FRAME_DURATION
 * OPUS_SET_FORCE_MODE
 * OPUS_SET_ENERGY_MASK
 * OPUS_SET_LFE
 */
public class Encoder {
    static
    {
        System.loadLibrary("opus_encoder_jni");
    }

    /**
     * A long representing a pointer to the underlying native object.
     */
    private final long pointer;

    /**
     * Coding modes
     */
    public enum Application
    {
        /**
         * Gives best quality at a given bitrate for voice signals.
         * It enhances the input signal by high-pass filtering and emphasizing formats and
         * harmonics.
         * Optionally it includes in-band forward error correction to protect against packet loss.
         * Use this mode for typical VoIP applications.
         * Because of the enhancement, even at high bitrates the output may sound different from
         * the input.
         **/
        VOIP(2048),
        /**
         * Gives best quality at a given bitrate for most non-voice signals like music.
         * Use this mode for music and mixed (music/voice) content, broadcast, and applications
         * requiring less than 15 ms of coding delay.
         */
        AUDIO(2049),
        /**
         * Configures low-delay mode that disables the speech-optimized mode in exchange for
         * slightly reduced delay.
         * This mode can only be set on an newly initialized or freshly reset encoder because
         * it changes the codec delay.
         */
        RESTRICTED_LOW_DELAY(2051);

        final int value;

        Application(int value) {
            this.value = value;
        }
    }

    /**
     * Create encoder.
     * Note, regardless of the sampling rate and number channels selected, the Opus encoder
     * can switch to a lower audio bandwidth or number of channels if the bitrate
     * selected is too low. This also means that it is safe to always use 48 kHz stereo input
     * and let the encoder optimize the encoding.
     * @param samplingRate Sampling rate of input signal (Hz)
     *                     This must be one of 8000, 12000, 16000, 24000, or 48000.
     * @param channels Number of channels (1 or 2) in input signal.
     * @param application Coding mode.
     */
    Encoder(int samplingRate, int channels, Application application)
    {
        if (!Arrays.asList(48000, 24000, 16000, 12000, 8000).contains(samplingRate))
        {
            throw new IllegalArgumentException("Invalid samling rate");
        }

        if (!Arrays.asList(1, 2).contains(channels))
        {
            throw new IllegalArgumentException("Invalid number of channels");
        }

        pointer = init(samplingRate, channels, application.value);
    }
    private static native long init(int samplingRate, int channels, int applicationType);

    /** Encode an Opus frame.
     * @param pcm Input signal (interleaved if 2 channels).
     *            length is frame_size * channels * sizeof(short)
     * @param frameSize Number of samples per channel in the input signal.
     *                  This must be an Opus frame size for the encoder's sampling rate.
     *
     *                  For example, at 48 kHz the permitted values are:
     *                      48 * 2.5ms = 120,
     *                      48 * 5ms = 240,
     *                      48 * 10ms = 480,
     *                      48 * 20ms = 960,
     *                      48 * 40ms = 1920, and
     *                      48 * 60ms = 2880.
     *                  Passing in a duration of less than 10 ms (480 samples at 48 kHz)
     *                  will prevent the encoder from using the LPC or hybrid modes.
     * @param output Output payload. The size of the allocated memory may be used to impose an
     *               upper limit on the instant bitrate, but should not be used as the only
     *               bitrate control. Use {@link #setBitrate(int)} to control the bitrate.
     *               A size of 4000 bytes is recommended.
     * @return The length of the encoded packet (in bytes)
     */
    public int encode(short[] pcm, int frameSize, byte[] output)
    {
        return encode(pcm, 0, pcm.length, frameSize, output, 0, output.length);
    }

    /** Encode an Opus frame.
     * @param pcm Input signal (interleaved if 2 channels).
     *            length is frame_size * channels * sizeof(short)
     * @param pcmOffset PCM buffer offset.
     * @param pcmSize PCM buffer size.
     * @param frameSize Number of samples per channel in the input signal.
     *                  This must be an Opus frame size for the encoder's sampling rate.
     *
     *                  For example, at 48 kHz the permitted values are:
     *                      48 * 2.5ms = 120,
     *                      48 * 5ms = 240,
     *                      48 * 10ms = 480,
     *                      48 * 20ms = 960,
     *                      48 * 40ms = 1920, and
     *                      48 * 60ms = 2880.
     *                  Passing in a duration of less than 10 ms (480 samples at 48 kHz)
     *                  will prevent the encoder from using the LPC or hybrid modes.
     * @param output Output payload. The size of the allocated memory may be used to impose an
     *               upper limit on the instant bitrate, but should not be used as the only
     *               bitrate control. Use {@link #setBitrate(int)} to control the bitrate.
     *               A size of 4000 bytes is recommended.
     * @param outputOffset output buffer offset.
     * @param outputSize output buffer size.
     * @return The length of the encoded packet (in bytes)
     */
    public int encode(short[] pcm, int pcmOffset, int pcmSize, int frameSize, byte[] output, int outputOffset, int outputSize)
    {

        if (pcm.length < (pcmSize + pcmOffset))
            throw new IllegalArgumentException("invalid PCM buffer arguments");
        if (output.length < (outputSize + outputOffset))
            throw new IllegalArgumentException("invalid output buffer arguments");

        return check(nativeEncode(pcm, pcmOffset, pcmSize, frameSize, output, outputOffset, outputSize));
    }
    private native int nativeEncode(short[] pcm, int pcmOffset, int pcmSize, int frameSize, byte[] output, int outputOffset, int outputSize);

    /**
     * Calculate the frame size based on a given frame duration.
     * @param frameTimeUs The frame duration specified un microseconds (μs).
     *                    Only the following input is valid:<ul>
     *                    <li>2500</li>
     *                    <li>5000</li>
     *                    <li>10000</li>
     *                    <li>20000</li>
     *                    <li>40000</li>
     *                    <li>60000</li>
     *                    <li>80000</li>
     *                    <li>100000</li>
     *                    <li>120000</li></ul>
     * @return The number of samples in the frame.
     */
    public int calculateFrameSize(int frameTimeUs)
    {
        switch(frameTimeUs) {
            case 2500:
            case 5000:
            case 10000:
            case 20000:
            case 40000:
            case 60000:
            case 80000:
            case 100000:
            case 120000:
                break;
            default:
                throw new IllegalArgumentException("Unsupported frame time");
        }
        return (frameTimeUs * getSampleRate()) / 1000000;
    }


    /**
     * Reset the state to be equivalent to a freshly initialized encoder.
     * This should be called when switching streams in order to prevent the back to back decoding
     * from giving different results from one at a time decoding.
     */
    public native void resetState();

    /**
     * Get the sampling rate the encoder was initialized with.
     * @return Sampling rate of encoder
     */
    public native int getSampleRate();


    /**
     * Configures the encoder's use of in-band forward error correction (FEC).
     * @param enable if true, enable in-band FEC otherwise disable it.
     */
    public native void enableInBandFEC(boolean enable);

    /**
     * Get encoder's configured use of in-band forward error correction.
     * @return true if in-band FEC is enabled, otherwise false
     */
    public native boolean hasInBandFEC();

    /**
     * Configure the encoder's expected packet loss percentage.
     * Higher values trigger progressively more loss resistant behavior in the encoder
     * at the expense of quality at a given bitrate in the absence of packet loss, but
     * greater quality under loss.
     * @param percentage Loss percentage in the range 0-100, inclusive (default: 0)
     */
    public native void setPacketLossPercentage(int percentage);

    /**
     * Get the encoder's configured packet loss percentage.
     * @return the configured loss percentage in the range 0-100, inclusive (default: 0).
     */
    public native int getPacketLossPercentage();

    /**
     * Enable or disable prediction.
     * When disabled almost all use of prediction is disabled,
     * making frames almost completely independent.
     * This reduces quality.
     * @param enable if true, enable prediction otherwise disable it.
     */
    public native void enablePrediction(boolean enable);

    /**
     * Get the encoder's configured prediction status.
     * @return if true prediction is enabled (default), if false prediction is disabled.
     */
    public native boolean isPredictionEnabled();

    /**
     * Gets the DTX state of the encoder.
     * @return  whether the last encoded frame was either a comfort noise update during DTX or
     * not encoded because of DTX.
     */
    public native boolean inDTX();

    /**
     * Set the bitrate
     * @param bitrate bitrate in bits per second (b/s)
     */
    public native void setBitrate(int bitrate);

    /**
     * Get the bitrate.
     * @return bitrate in bits per second (b/s)
     */
    public native int getBitrate();

    /**
     * Set the complexity
     * @param complexity value from 1 to 10, where 1 is the lowest complexity and 10 is the highest
     */
    public native void setComplexity(int complexity);

    /**
     * Get the complexity
     * @return value from 1 to 10, where 1 is the lowest complexity and 10 is the highest
     */
    public native int getComplexity();

    /**
     * Set the audio bandwidth
     * @param bandwidth the audio bandwidth to set
     */
    public void setBandwidth(Bandwidth bandwidth)
    {
        nativeSetBandwidth(bandwidth.value);
    }
    private native void nativeSetBandwidth(int value);

    /**
     * Get the audio bandwidth
     * @return the audio bandwidth
     */
    public Bandwidth getBandwidth()
    {
        return Bandwidth.get(nativeGetBandwidth());
    }
    private native int nativeGetBandwidth();

    /**
     * Configures the maximum bandpass that the encoder will select automatically.
     * Applications should normally use this instead of {@link #setBandwidth(Bandwidth)}
     * (leaving that set to the default).
     * This allows the application to set an upper bound based on the type of input it is providing,
     * but still gives the encoder the freedom to reduce the bandpass when the bitrate becomes
     * too low, for better overall quality.
     * @param maxBandwidth the maximum audio bandwidth to set
     */
    public void setMaxBandwidth(Bandwidth maxBandwidth)
    {
        nativeSetMaxBandwidth(maxBandwidth.value);
    }
    private native void nativeSetMaxBandwidth(int value);

    /**
     * Get the audio bandwidth
     * @return the audio bandwidth
     */
    public Bandwidth getMaxBandwidth()
    {
        return Bandwidth.get(nativeGetMaxBandwidth());
    }
    private native int nativeGetMaxBandwidth();

    public enum Signal
    {
        /**
         * Auto/default setting
         */
        AUTO(-1000),
        /**
         * Signal being encoded is voice
         */
        VOICE(3001),
        /**
         * Signal being encoded is music
         */
        MUSIC(3002);

        final int value;

        Signal(int value) {
            this.value = value;
        }

        static Signal get(int value)
        {
            switch (value)
            {
                case -1000:
                    return AUTO;
                case 3001:
                    return VOICE;
                case 3002:
                    return MUSIC;
            }
            return null;
        }
    }

    /**
     * Set the input signal
     * @param signal the input signal to set
     */
    public void setSignal(Signal signal)
    {
        nativeSetSignal(signal.value);
    }
    private native void nativeSetSignal(int value);


    /**
     * Get the input signal
     * @return the input signal
     */
    public Signal getSignal()
    {
        return Signal.get(nativeGetSignal());
    }
    private native int nativeGetSignal();

    /**
     * Finalizes the object and it's underlying native part.
     */
    @Override
    protected void finalize() throws Throwable
    {
        finalize(pointer);
        super.finalize();
    }

    /**
     * Finalizes the underlying native part.
     * @param pointer A long representing a pointer to the underlying native object.
     */
    private native void finalize(long pointer);
}
