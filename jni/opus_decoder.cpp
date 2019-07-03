// Copyright (c) 2016 Steinwurf ApS
// All Rights Reserved
//
// THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF STEINWURF
// The copyright notice above does not evidence any
// actual or intended publication of such source code.

#include <cassert>
#include <memory>

#include <opus.h>

#include <jni.h>

#include <jutils/utils.hpp>
#include <jutils/logging.hpp>

jint JNI_OnLoad(JavaVM* vm, void* /*reserved*/)
{
    jutils::init(vm);
    return JNI_VERSION_1_4;
}

// To allow for overloading of functions, C++ uses something called name
// mangling.
// This means that function names are not the same in C++ as in plain C.
// To inhibit this name mangling, you have to declare functions as extern "C"
#ifdef __cplusplus
extern "C" {
#endif

jlong Java_com_steinwurf_opus_Decoder_init(
    JNIEnv* /*env*/,
    jclass /*clazz*/,
    jint samplingRate,
    jint channels)
{
    int error = 0;
    auto decoder = opus_decoder_create(samplingRate, channels, &error);

    switch (error)
    {
    case OPUS_OK:
        LOGI << "No error";
        break;
    case OPUS_BAD_ARG:
        LOGF << "One or more invalid/out of range arguments";
        break;
    case OPUS_BUFFER_TOO_SMALL:
        LOGF << "The mode struct passed is invalid";
        break;
    case OPUS_INTERNAL_ERROR:
        LOGF << "An internal error was detected";
        break;
    case OPUS_INVALID_PACKET:
        LOGE << "The compressed data passed is corrupted";
        break;
    case OPUS_UNIMPLEMENTED:
        LOGE << "Invalid/unsupported request number";
        break;
    case OPUS_INVALID_STATE:
        LOGF << "An decoder structure is invalid or already freed";
        break;
    case OPUS_ALLOC_FAIL:
        LOGF << "Memory allocation has failed";
        break;
    default:
        break;
    }

    return reinterpret_cast<jlong>(decoder);
}

jint Java_com_steinwurf_opus_Decoder_nativeDecode(
    JNIEnv* env,
    jobject thiz,
    jbyteArray jinput,
    jint input_offset,
    jint input_size,
    jshortArray jpcm,
    jint pcm_offset,
    jint pcm_size,
    jint frame_size,
    jboolean decode_fec)
{
    auto decoder = jutils::get_native_pointer<OpusDecoder>(env, thiz);

	jbyte* const input = env->GetByteArrayElements(jinput, 0);
	jint input_length = env->GetArrayLength(jinput);
    assert(input_length >= (input_size + input_offset));

	jint pcm_length = env->GetArrayLength(jpcm);
	jshort* const pcm = env->GetShortArrayElements(jpcm, 0);
    assert(pcm_length >= (pcm_size + pcm_offset));

	int decoded = opus_decode(
        decoder,
        (uint8_t*)input + input_offset,
        input_size,
        pcm + pcm_offset,
        frame_size,
        decode_fec ? 1 : 0);

    assert(pcm_size >= decoded);

	env->ReleaseByteArrayElements(jinput, input, JNI_ABORT);
	env->ReleaseShortArrayElements(jpcm, pcm, 0);

	return decoded;
}

jint Java_com_steinwurf_opus_Decoder_nativeGetBandwidth(JNIEnv* env, jobject thiz)
{
    auto decoder = jutils::get_native_pointer<OpusDecoder>(env, thiz);
    int32_t bandwidth = 0;
    opus_decoder_ctl(decoder, OPUS_GET_BANDWIDTH(&bandwidth));
    return bandwidth;
}

jint Java_com_steinwurf_opus_Decoder_getSampleRate(JNIEnv* env, jobject thiz)
{
    auto decoder = jutils::get_native_pointer<OpusDecoder>(env, thiz);
    int32_t sample_rate = 0;
    opus_decoder_ctl(decoder, OPUS_GET_SAMPLE_RATE(&sample_rate));
    return sample_rate;
}

jint Java_com_steinwurf_opus_Decoder_getLastPacketDuration(
    JNIEnv* env, jobject thiz)
{
    auto decoder = jutils::get_native_pointer<OpusDecoder>(env, thiz);
    int32_t last_packet_duration = 0;
    opus_decoder_ctl(decoder, OPUS_GET_LAST_PACKET_DURATION(&last_packet_duration));
    return last_packet_duration;
}

jint Java_com_steinwurf_opus_Decoder_getGain(
    JNIEnv* env, jobject thiz)
{
    auto decoder = jutils::get_native_pointer<OpusDecoder>(env, thiz);
    int32_t gain = 0;
    opus_decoder_ctl(decoder, OPUS_GET_GAIN(&gain));
    return gain;
}

void Java_com_steinwurf_opus_Decoder_setGain(JNIEnv* env, jobject thiz, jshort gain)
{
    auto decoder = jutils::get_native_pointer<OpusDecoder>(env, thiz);
    opus_decoder_ctl(decoder, OPUS_SET_GAIN(gain));
}

jint Java_com_steinwurf_opus_Decoder_getPitch(
    JNIEnv* env, jobject thiz)
{
    auto decoder = jutils::get_native_pointer<OpusDecoder>(env, thiz);
    int32_t pitch = 0;
    opus_decoder_ctl(decoder, OPUS_GET_PITCH(&pitch));
    return pitch;
}

void Java_com_steinwurf_opus_Decoder_resetState(JNIEnv* env, jobject thiz)
{
    auto decoder = jutils::get_native_pointer<OpusDecoder>(env, thiz);
    opus_decoder_ctl(decoder, OPUS_RESET_STATE);
}

void Java_com_steinwurf_opus_Decoder_finalize(
    JNIEnv* /*env*/, jobject /*thiz*/, jlong pointer)
{
    auto decoder = reinterpret_cast<OpusDecoder*>(pointer);
    opus_decoder_destroy(decoder);
}

#ifdef __cplusplus
}
#endif
