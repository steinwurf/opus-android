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

jlong Java_com_steinwurf_opus_Encoder_init(
    JNIEnv* /*env*/,
    jclass /*clazz*/,
    jint samplingRate,
    jint channels,
    jint application)
{
    int error = 0;
    auto encoder = opus_encoder_create(samplingRate, channels, application, &error);

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
        LOGF << "An encoder structure is invalid or already freed";
        break;
    case OPUS_ALLOC_FAIL:
        LOGF << "Memory allocation has failed";
        break;
    default:
        break;
    }

    return reinterpret_cast<jlong>(encoder);
}

jint Java_com_steinwurf_opus_Encoder_nativeEncode(
    JNIEnv* env,
    jobject thiz,
    jshortArray jpcm,
    jint pcm_offset,
    jint pcm_size,
    jint frameSize,
    jbyteArray joutput,
    jint output_offset,
    jint output_size)
{
    auto encoder = jutils::get_native_pointer<OpusEncoder>(env, thiz);

	jshort* const pcm = env->GetShortArrayElements(jpcm, 0);
	jint pcm_length = env->GetArrayLength(jpcm);
    assert(pcm_length >= (pcm_size + pcm_offset));

	jint output_length = env->GetArrayLength(joutput);
	jbyte* const output = env->GetByteArrayElements(joutput, 0);
    assert(output_length >= (output_size + output_offset));

	int encoded = opus_encode(
        encoder,
        pcm + pcm_offset,
        frameSize,
        (uint8_t*)output + output_offset,
        output_size);

    assert(pcm_size >= encoded);

	env->ReleaseShortArrayElements(jpcm, pcm, JNI_ABORT);
	env->ReleaseByteArrayElements(joutput, output, 0);

	return encoded;
}

void Java_com_steinwurf_opus_Encoder_resetState(JNIEnv* env, jobject thiz)
{
    auto encoder = jutils::get_native_pointer<OpusEncoder>(env, thiz);
    opus_encoder_ctl(encoder, OPUS_RESET_STATE);
}

jint Java_com_steinwurf_opus_Encoder_getSampleRate(JNIEnv* env, jobject thiz)
{
    auto encoder = jutils::get_native_pointer<OpusEncoder>(env, thiz);
    int32_t sample_rate = 0;
    opus_encoder_ctl(encoder, OPUS_GET_SAMPLE_RATE(&sample_rate));
    return sample_rate;
}

void Java_com_steinwurf_opus_Encoder_setBitrate(JNIEnv* env, jobject thiz, jint bitrate)
{
    auto encoder = jutils::get_native_pointer<OpusEncoder>(env, thiz);
    opus_encoder_ctl(encoder, OPUS_SET_BITRATE(bitrate));
}

jint Java_com_steinwurf_opus_Encoder_getBitrate(JNIEnv* env, jobject thiz)
{
    auto encoder = jutils::get_native_pointer<OpusEncoder>(env, thiz);
    int32_t bitrate = 0;
    opus_encoder_ctl(encoder, OPUS_GET_BITRATE(&bitrate));
    return bitrate;
}

void Java_com_steinwurf_opus_Encoder_setComplexity(JNIEnv* env, jobject thiz, jint complexity)
{
    auto encoder = jutils::get_native_pointer<OpusEncoder>(env, thiz);
    opus_encoder_ctl(encoder, OPUS_SET_COMPLEXITY(complexity));
}

jint Java_com_steinwurf_opus_Encoder_getComplexity(JNIEnv* env, jobject thiz)
{
    auto encoder = jutils::get_native_pointer<OpusEncoder>(env, thiz);
    int32_t complexity = 0;
    opus_encoder_ctl(encoder, OPUS_GET_COMPLEXITY(&complexity));
    return complexity;
}

void Java_com_steinwurf_opus_Encoder_nativeSetBandwidth(JNIEnv* env, jobject thiz, jint value)
{
    auto encoder = jutils::get_native_pointer<OpusEncoder>(env, thiz);
    opus_encoder_ctl(encoder, OPUS_SET_BANDWIDTH(value));
}

jint Java_com_steinwurf_opus_Encoder_nativeGetBandwidth(JNIEnv* env, jobject thiz)
{
    auto encoder = jutils::get_native_pointer<OpusEncoder>(env, thiz);
    int32_t bandwidth = 0;
    opus_encoder_ctl(encoder, OPUS_GET_BANDWIDTH(&bandwidth));
    return bandwidth;
}

void Java_com_steinwurf_opus_Encoder_nativeSetMaxBandwidth(JNIEnv* env, jobject thiz, jint value)
{
    auto encoder = jutils::get_native_pointer<OpusEncoder>(env, thiz);
    opus_encoder_ctl(encoder, OPUS_SET_MAX_BANDWIDTH(value));
}

jint Java_com_steinwurf_opus_Encoder_nativeGetMaxBandwidth(JNIEnv* env, jobject thiz)
{
    auto encoder = jutils::get_native_pointer<OpusEncoder>(env, thiz);
    int32_t bandwidth = 0;
    opus_encoder_ctl(encoder, OPUS_GET_MAX_BANDWIDTH(&bandwidth));
    return bandwidth;
}

void Java_com_steinwurf_opus_Encoder_nativeSetSignal(
    JNIEnv* env, jobject thiz, jint value)
{
    auto encoder = jutils::get_native_pointer<OpusEncoder>(env, thiz);
    opus_encoder_ctl(encoder, OPUS_SET_SIGNAL(value));
}

jint Java_com_steinwurf_opus_Encoder_nativeGetSignal(JNIEnv* env, jobject thiz)
{
    auto encoder = jutils::get_native_pointer<OpusEncoder>(env, thiz);
    int32_t signal = 0;
    opus_encoder_ctl(encoder, OPUS_GET_SIGNAL(&signal));
    return signal;
}

void  Java_com_steinwurf_opus_Encoder_enableInBandFEC(
    JNIEnv* env,
    jobject thiz,
    jboolean enable)
{
    auto encoder = jutils::get_native_pointer<OpusEncoder>(env, thiz);
    opus_encoder_ctl(encoder, OPUS_SET_INBAND_FEC(enable ? 1 : 0));
}

jboolean Java_com_steinwurf_opus_Encoder_hasInBandFEC(JNIEnv* env, jobject thiz)
{
    auto encoder = jutils::get_native_pointer<OpusEncoder>(env, thiz);
    int32_t has_in_band_fec = 0;
    opus_encoder_ctl(encoder, OPUS_GET_INBAND_FEC(&has_in_band_fec));
    return has_in_band_fec == 1;
}

void  Java_com_steinwurf_opus_Encoder_enablePrediction(
    JNIEnv* env,
    jobject thiz,
    jboolean enable)
{
    auto encoder = jutils::get_native_pointer<OpusEncoder>(env, thiz);
    opus_encoder_ctl(encoder, OPUS_SET_PREDICTION_DISABLED(enable ? 0 : 1));
}

jboolean Java_com_steinwurf_opus_Encoder_isPredictionEnabled(
    JNIEnv* env, jobject thiz)
{
    auto encoder = jutils::get_native_pointer<OpusEncoder>(env, thiz);
    int32_t disabled = 0;
    opus_encoder_ctl(encoder, OPUS_GET_PREDICTION_DISABLED(&disabled));
    return disabled == 0;
}

jboolean Java_com_steinwurf_opus_Encoder_inDTX(JNIEnv* env, jobject thiz)
{
    auto encoder = jutils::get_native_pointer<OpusEncoder>(env, thiz);
    int32_t in_dtx = 0;
    opus_encoder_ctl(encoder, OPUS_GET_IN_DTX(&in_dtx));
    return in_dtx == 1;
}

void  Java_com_steinwurf_opus_Encoder_setPacketLossPercentage(
    JNIEnv* env,
    jobject thiz,
    jint percentage)
{
    auto encoder = jutils::get_native_pointer<OpusEncoder>(env, thiz);
    opus_encoder_ctl(encoder, OPUS_SET_PACKET_LOSS_PERC(percentage));
}

jint Java_com_steinwurf_opus_Encoder_getPacketLossPercentage(
    JNIEnv* env, jobject thiz)
{
    auto encoder = jutils::get_native_pointer<OpusEncoder>(env, thiz);
    int32_t percentage = 0;
    opus_encoder_ctl(encoder, OPUS_GET_PACKET_LOSS_PERC(&percentage));
    return percentage;
}

void Java_com_steinwurf_opus_Encoder_finalize(
    JNIEnv* /*env*/, jobject /*thiz*/, jlong pointer)
{
    auto encoder = reinterpret_cast<OpusEncoder*>(pointer);
    opus_encoder_destroy(encoder);
}

#ifdef __cplusplus
}
#endif
