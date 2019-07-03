package com.steinwurf.opus;

import java.util.Arrays;

import static com.steinwurf.opus.ReturnValueCheck.check;

public class Decoder {
    static
    {
        System.loadLibrary("opus_decoder_jni");
    }

    /**
     * A long representing a pointer to the underlying native object.
     */
    private final long pointer;

    /**
     * Create decoder.
     * @param samplingRate Sampling rate of input signal (Hz)
     *                     This must be one of 8000, 12000, 16000, 24000, or 48000.
     * @param channels Number of channels (1 or 2) to decode
     */
    Decoder(int samplingRate, int channels)
    {
        if (!Arrays.asList(48000, 24000, 16000, 12000, 8000).contains(samplingRate))
        {
            throw new IllegalArgumentException(String.format("Invalid samling rate %d", samplingRate));
        }

        if (!Arrays.asList(1, 2).contains(channels))
        {
            throw new IllegalArgumentException(String.format("Invalid number of channels %d", channels));
        }

        pointer = init(samplingRate, channels);
    }
    private static native long init(int samplingRate, int channels);

    /**
     * Reset the state to be equivalent to a freshly initialized decoder.
     * This should be called when switching streams in order to prevent the back to back decoding
     * from giving different results from one at a time decoding.
     */
    public native void resetState();

    /**
     * Decode an Opus packet.
     * @param input Input payload. Use a null pointer to indicate packet loss.
     * @param inputOffset Input payload offset.
     * @param inputSize Input payload size.
     * @param pcm Output signal (interleaved if 2 channels).
     *               Length is frameSize * channels * sizeof(short)
     * @param pcmOffset Output signal offset.
     * @param pcmSize Output signal size.
     * @param frameSize Number of samples per channel of available space in output.
     *                  If this is less than the maximum packet duration (120ms; 5760 for 48kHz),
     *                  this function will not be capable of decoding some packets.
     *                  In the case of PLC (data==null) or FEC (decode_fec=1), then frame_size needs
     *                  to be exactly the duration of audio that is missing, otherwise the
     *                  decoder will not be in the optimal state to decode the next incoming packet.
     *                  For the PLC and FEC cases, frameSize <b>must</b> be a multiple of 2.5 ms.
     * @param decodeFEC request that any in-band forward error correction data be decoded.
     *                  If no such data is available, the frame is decoded as if it were lost.
     * @return Number of decoded samples
     */
    public int decode(byte[] input, int inputOffset, int inputSize, short[] pcm, int pcmOffset, int pcmSize, int frameSize, boolean decodeFEC)
    {
        if (input.length < (inputSize + inputOffset))
            throw new IllegalArgumentException("invalid input buffer arguments");
        if (pcm.length < (pcmSize + pcmOffset))
            throw new IllegalArgumentException("invalid pcm buffer arguments");

        return check(nativeDecode(input, inputOffset, inputSize, pcm, pcmOffset, pcmSize, frameSize, decodeFEC));
    }

    /**
     * Decode an Opus packet.
     * @param input Input payload. Use a null pointer to indicate packet loss.
     * @param pcm Output signal (interleaved if 2 channels).
     *               Length is frameSize * channels * sizeof(short)
     * @param frameSize Number of samples per channel of available space in output.
     *                  If this is less than the maximum packet duration (120ms; 5760 for 48kHz),
     *                  this function will not be capable of decoding some packets.
     *                  In the case of PLC (data==null) or FEC (decode_fec=1), then frame_size needs
     *                  to be exactly the duration of audio that is missing, otherwise the
     *                  decoder will not be in the optimal state to decode the next incoming packet.
     *                  For the PLC and FEC cases, frameSize <b>must</b> be a multiple of 2.5 ms.
     * @param decodeFEC request that any in-band forward error correction data be decoded.
     *                  If no such data is available, the frame is decoded as if it were lost.
     * @return Number of decoded samples
     */
    public int decode(byte[] input, short[] pcm, int frameSize, boolean decodeFEC)
    {
        return decode(input, 0, input.length, pcm, 0, pcm.length, frameSize, decodeFEC);
    }

    private native int nativeDecode(
            byte[] input,
            int inputOffset,
            int inputSize,
            short[] pcm,
            int pcmOffset,
            int pcmSize,
            int frameSize,
            boolean decodeFEC);

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
     * Get the sampling rate the decoder was initialized with.
     * @return Sampling rate of decoder
     */
    public native int getSampleRate();

    /**
     * Get the duration (in samples) of the last packet successfully decoded or concealed.
     * @return Number of samples (at current sampling rate).
     */
    public native int getLastPacketDuration();


    /**
     * Get the decoder's configured gain adjustment.
     * @return Amount to scale PCM signal by in Q8 dB units.
     */
    public native int getGain();

    /**
     * Configures decoder gain adjustment.
     * Scales the decoded output by a factor specified in Q8 dB units.
     * The default is zero indicating no adjustment.
     * This setting survives decoder reset.
     *
     * gain = pow(10, x / (20.0 * 256))
     *
     * @param gain Amount to scale PCM signal by in Q8 dB units.
     */
    public native void setGain(short gain);

    /**
     * Get the duration (in samples) of the last packet successfully decoded or concealed.
     * @return pitch period at 48 kHz (or 0 if not available)
     */
    public native int getPitch();

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
